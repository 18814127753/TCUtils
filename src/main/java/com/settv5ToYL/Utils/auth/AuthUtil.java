package com.settv5ToYL.Utils.auth;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.tc.ArgsUtil;
import com.tc.LogUtil;


//import net.sf.json.JSONObject;

//不少是百度找到的，大概有一半左右的函数没有用到。如果有需要通过authController去查找
public class AuthUtil {
	static ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	static RepositoryService repositoryService = processEngine.getRepositoryService();
	static RuntimeService runtimeService = processEngine.getRuntimeService();
	static TaskService taskService = processEngine.getTaskService();
	static HistoryService historyService = processEngine.getHistoryService();
	static IdentityService identityService = processEngine.getIdentityService();

	public static void deleteTask(String taskID) {
		historyService.deleteHistoricTaskInstance(taskID);
		LogUtil.info("删除任务实例taskId:"+taskID+"成功!");
		
	}
	
	public static String getTaskDate(String taskId) {
		HistoricTaskInstance hi = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		Date date;
		if(hi!=null) {
			date = hi.getStartTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			return sdf.format(date);
		}
		else {
			LogUtil.error("找不到taskID为"+taskId+"的任务");
			return null;
		}
			
		
	}
	
	
	/**
	 * 		找出当天的所有instance的第一条task，查出批注，然后匹配检查。
	 * 		如果匹配，则表示这条审批流程已经审批通过。
	 * 		备款、调账、回款只验证金额，只进行2步审批
	 * 		单笔结算、批量结算要进行4步审批,验证全部要素
	 */


	
	
	/**
	 * 根据taskID，返回批注
	 */
	public static String getCommentFromTask(String taskID) {
		StringBuilder sb = new StringBuilder();
		List<Comment> commentList = taskService.getTaskComments(taskID);
		for (Comment comment : commentList) {
			sb.append(comment.getFullMessage() + "|");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	/**
	 * 根据instance获取第一条任务的批注comment
	 */
	public static String getFirstTaskCommentFromInstance(String instanceId) {
		List<HistoricTaskInstance> HistoricTaskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).list();
		
		HistoricTaskInstance hi = HistoricTaskInstanceList.get(0);
		if(hi!=null) {
			return AuthUtil.getCommentFromTask(hi.getId());
		}
		return null;
		
	}
	/**
	 *  获取任务链的第一条任务批注comment
	 */
	public static String getFirstTaskCommentFromTaskId(String taskId) {
		HistoricTaskInstance hi = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(hi.getProcessInstanceId()).singleResult();
		return getFirstTaskCommentFromInstance(instance.getId());
	}
	
	/**
	 *  删除（正在运行的）流程,删除成功后“是否结束”会显示已删除
	 */
	public static String deleteProcessInstanceByTaskId(String username, String taskId, String reason) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//		Authentication.setAuthenticatedUserId(username);// 批注人的名称 一定要写，不然查看的时候不知道人物信息
		taskService.addComment(taskId, task.getProcessInstanceId(), reason);// comment为批注内容
		claim(username, taskId);
		task.setAssignee(username);
		LogUtil.info("删除流程taskId:"+taskId+",task.getProcessInstanceId():"+task.getProcessInstanceId());
		return deleteProcessInstance(username, task.getProcessInstanceId(), reason);
	}
	/**
	 *  删除流程（正在运行的）,删除成功后“是否结束”会显示已删除
	 *  然后还需要手动删除qingsuan_auth_waitingQueue里的该条数据
	 */
	public static String deleteProcessInstance(String username, String instanceId, String reason) {
		runtimeService.deleteProcessInstance(instanceId, reason);
		LogUtil.info("用户["+username+"]删除instantId["+instanceId+"]成功！理由："+reason);
		return "删除成功";
	}

	/**
	 * 权限管理，看看用户和步骤执行者是否匹配
	 */
	
	public static boolean permissionCheck(String username, String taskId) {
		//查找正在运行的task。如果是已经运行完毕的task，再点击通过/驳回，也返回不匹配。如果有需要再改返回值为string写详情
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if(task==null) {
			return false;
		}
		LogUtil.info("权限检查,username:"+username+",taskId:"+taskId+",task.getName():"+task.getName());
		if (task != null) {
			if ((username.indexOf("jingb") >= 0) && (task.getName().equals("经办岗申请"))) {
				return true;
			} else if ((username.indexOf("fuhe") >= 0) && (task.getName().equals("复核岗审批"))) {
				return true;
			} else if ((username.indexOf("shenpi") >= 0) && (task.getName().equals("审批岗审批"))) {
				return true;
			} else if ((username.indexOf("caiwu") >= 0) && (task.getName().equals("财务岗审批"))) {
				return true;
			} 
		}
		return false;
	}

	/**
	 * 根据taskId返回HistoricTaskInstance
	 */
	public static HistoricTaskInstance findHistoryTaskInstance(String taskId) {
		return historyService.createHistoricTaskInstanceQuery().taskId(taskId).orderByTaskCreateTime().desc().singleResult();
	}
	
	/**
	 * 根据processInstantce的Id返回这个instance第一个task的ID,作为显示用
	 */
	public static String findFirstTaskIdByProcessInstanceId(String processInstantceId) {
		return historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstantceId).singleResult().getId();
	}

	/**
	 * 根据输入ACT_RU_TASK的taskID获取批注comment
	 */
	public static List<Comment> getComments(String taskId) {
		String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
		return taskService.getProcessInstanceComments(processInstanceId);
	}

	/**
	 * 获取上一条的批注
	 */
//	public static String findPreviousComment(String taskId) {
//		Task task = null;
//
//		task = taskService.createTaskQuery()//
//				.taskId(taskId)// 使用任务ID查询
//				.singleResult();
//
//		if (task == null) {
//			return null;
//		}
//		String processInstanceId = task.getProcessInstanceId();
//		List<HistoricTaskInstance> list = historyService// 与历史数据（历史表）相关的service
//				.createHistoricTaskInstanceQuery()// 创建历史任务实例查询
//				.processInstanceId(processInstanceId).list();
//
//		List<Comment> commentList = taskService.getTaskComments(list.get(list.size() - 2).getId());
//		if (commentList.size() > 0) {
//			return commentList.get(0).getFullMessage();
//		} else {
//			return "";
//		}
//	}
	
	/**
	 * instance是否已经完成
	 */
	public static String instanceStatus(String procId) {
		boolean isFinishedBoolean = isFinished(procId);
		HistoricProcessInstance pi = processEngine.getHistoryService()// 与历史数据（历史表）相关的Service
				.createHistoricProcessInstanceQuery()// 创建历史流程实例查询
				.processInstanceId(procId)// 使用流程实例ID查询
				.singleResult();
		String resultString = isFinishedBoolean ? "已结束" : "未结束";
		if (pi.getDeleteReason() != null) {
			resultString = "已删除";
		}
		return resultString;
	}

	/**
	 * 查询hi_procinst，根据endtime查看是否该任务结束，结束的话则表示授权成功。
	 */
	public static boolean isFinished(String procId) {
		HistoricProcessInstance pi = processEngine.getHistoryService()// 与历史数据（历史表）相关的Service
				.createHistoricProcessInstanceQuery()// 创建历史流程实例查询
				.processInstanceId(procId)// 使用流程实例ID查询
				.singleResult();

		if (null == pi) {
			 LogUtil.error("查询不到proc流程实例:"+procId);
			return false;
		}

		else if (null == pi.getEndTime() && null != pi.getStartTime()) {
			return false;
		} else if (null != pi.getEndTime() && null != pi.getStartTime()) {
			return true;
		} else
			return false;
	}
	/**
	 * 查询hi_procinst，根据endtime查看是否该任务结束，结束的话则表示授权成功。
	 */
	public static boolean isFinishedByTaskId(String taskId) {
		HistoricTaskInstance hi = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		String procId = hi.getProcessInstanceId();
		HistoricProcessInstance pi = processEngine.getHistoryService()// 与历史数据（历史表）相关的Service
				.createHistoricProcessInstanceQuery()// 创建历史流程实例查询
				.processInstanceId(procId)// 使用流程实例ID查询
				.singleResult();
		
		if (null == pi) {
			LogUtil.error("查询不到proc流程实例:"+procId);
			return false;
		}
		
		else if (null == pi.getEndTime() && null != pi.getStartTime()) {
			return false;
		} else if (null != pi.getEndTime() && null != pi.getStartTime()) {
			return true;
		} else
			return false;
	}

	/**
	 * 接手任务后，把这个任务放回“我的小组任务”中，让其他人接手。目前前端界面没有提供这个函数的操作
	 */
	public static void claimBack(String userId, String taskId) {
		processEngine.getTaskService()//
				.setAssignee(taskId, null);
		LogUtil.info(userId + "回退了任务" + taskId + "回到组任务");
	}

	/**
	 * 接手组内任务
	 */
	public static void claim(String userId, String taskId) {
		processEngine.getTaskService().claim(taskId, userId);
		LogUtil.info(userId + "接手了任务" + taskId);
	}

	/**
	 * 查找用户下可接手的组任务task 1.查找可接手任务task 2.找到该task对应的instance 3.取这个instance的第一条记录，并返回
	 */
	public static String findMyGroupTask(String username) {
		List<Task> list = taskService.createTaskQuery()// 创建任务查询对象
				.taskCandidateUser(username)// 组任务的办理人查询
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				.list();
		JSONObject jo = new JSONObject(new LinkedHashMap<String, Object>());
		List<Map<String, String>> list_map = new ArrayList<Map<String, String>>();
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				HistoricProcessInstance hi = historyService// 与历史数据（历史表）相关的Service
						.createHistoricProcessInstanceQuery()// 创建历史任务实例查询
						.processInstanceId(task.getProcessInstanceId()).singleResult();
				if (hi != null) {
					List<HistoricTaskInstance> taskChain = historyService.createHistoricTaskInstanceQuery()
							.processInstanceId(hi.getId()).list();
					HistoricTaskInstance firstTask = taskChain.get(0);
					String comment = getCommentFromTask(firstTask.getId());
					map.put("taskId", firstTask.getId());
					map.put("任务的办理人", firstTask.getAssignee());
					map.put("备注", comment);
					map.put("任务的创建时间", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(firstTask.getCreateTime()));
					if(AuthConfig.getArgsFromCommentString(comment).get("type").equals("批量结算")) {
						String createDate = new SimpleDateFormat("yyyyMMdd").format(firstTask.getCreateTime());
						map.put("批量文件路径", getBatchFilePath(createDate,comment));
					}
					else {
						map.put("批量文件路径", "");
					}
					
					
					list_map.add(ArgsUtil.setNullValueToEmpty(map));
				}

			}
		}
		jo.put("resultList", list_map);
		return jo.toString();
	}

	/**
	 * 查找用户下的任务
	 */
	public static String findMyTask(String username) {
		List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				/** 查询条件（where部分） */
				.taskAssignee(username)// 任务的办理人查询
				/** 排序 */
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				/** 返回结果集 */
				.list();// 返回列表
		JSONObject jo = new JSONObject(new LinkedHashMap<String, Object>());
		List<Map<String, String>> list_map = new ArrayList<Map<String, String>>();
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("missionID", task.getId());
				map.put("任务的创建时间", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(task.getCreateTime()));
				map.put("任务的办理人", task.getAssignee());
				map.put("任务名称:", task.getName());
				list_map.add(ArgsUtil.setNullValueToEmpty(map));

			}
		}
		jo.put("resultList", list_map);
		return jo.toString();
	}


	/**
	 * 查询act_hi_taskinst return 这个任务关联的流程ID 参与过的任务
	 */
//	public static List<String> findHistoryTask(String assignee) {
//		List<String> resultList = new ArrayList<String>();
//		List<HistoricTaskInstance> list = processEngine.getHistoryService()// 与历史数据（历史表）相关的Service
//				.createHistoricTaskInstanceQuery()// 创建历史任务实例查询
//				.taskAssignee(assignee)// 指定历史任务的办理人
//				.orderByTaskCreateTime().asc().list();
//		if (list != null && list.size() > 0) {
//			for (HistoricTaskInstance pi : list) {
//				resultList.add(pi.getProcessInstanceId());
//			}
//		}
//		return resultList;
//	}

	
	public static String rollback(String username, String taskId, String comment) {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = engine.getRepositoryService();
		RuntimeService runtimeService = engine.getRuntimeService();
		TaskService taskService = engine.getTaskService();
		HistoryService historyService = engine.getHistoryService();
		LogUtil.info("--------------rollback流程开始------------------");
		LogUtil.info("username:"+username+",taskId:"+taskId+",comment:"+comment);
		try {
			Map<String, Object> variables;
			// 取得当前任务.当前任务节点
			HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId)
					.singleResult();
//			String orignAssignee = currTask.getAssignee();
			// 取得流程实例，流程实例
			ProcessInstance instance = runtimeService.createProcessInstanceQuery()
					.processInstanceId(currTask.getProcessInstanceId()).singleResult();
			if (instance == null) {
				LogUtil.error("流程结束");
				LogUtil.error("出错啦！流程已经结束");
				// return "ERROR";
			}
			variables = instance.getProcessVariables();
			// 取得流程定义
			ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
					.getDeployedProcessDefinition(currTask.getProcessDefinitionId());
			if (definition == null) {
				LogUtil.error("流程定义未找到");
				LogUtil.info("出错啦！流程定义未找到");
				return "writeErrorLog错啦！流程定义未找到";
			}
			// 取得上一步活动
			ActivityImpl currActivity = ((ProcessDefinitionImpl) definition)
					.findActivity(currTask.getTaskDefinitionKey());
//			LogUtil.info("初始上一步"+currActivity.getIncomingTransitions().get(0).getSource().getId());
//			LogUtil.info("初始活动"+currActivity.getId());
//			LogUtil.info("初始下一步"+currActivity.getOutgoingTransitions().get(0).getSource().getId());
			// 也就是节点间的连线
			List<PvmTransition> nextTransitionList = currActivity.getIncomingTransitions();
			// 清除当前活动的出口
			List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
			// 新建一个节点连线关系集合
			List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
			for (PvmTransition pvmTransition : pvmTransitionList) {
				
				oriPvmTransitionList.add(pvmTransition);
			}
			pvmTransitionList.clear();

//			// 建立新出口
			List<TransitionImpl> newTransitions = new ArrayList<TransitionImpl>();
			PvmActivity nextActivity = nextTransitionList.get(0).getSource();
			ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition).findActivity(nextActivity.getId());
			TransitionImpl newTransition = currActivity.createOutgoingTransition();
			newTransition.setDestination(nextActivityImpl);
			newTransitions.add(newTransition);
//			LogUtil.info("操作之后的下一步:"+nextActivityImpl.getId());
//			for (PvmTransition nextTransition : nextTransitionList) {
//				PvmActivity nextActivity = nextTransition.getSource();
//				ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition).findActivity(nextActivity.getId());
//				TransitionImpl newTransition = currActivity.createOutgoingTransition();
//				newTransition.setDestination(nextActivityImpl);
//				newTransitions.add(newTransition);
//				LogUtil.info("操作之后的下一步:"+nextActivityImpl.getId());
//			}
//			// 完成任务
			List<Task> tasks = taskService.createTaskQuery().processInstanceId(instance.getId())
					.taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
			
			for (Task task : tasks) {
				Authentication.setAuthenticatedUserId(username);// 批注人的名称 一定要写，不然查看的时候不知道人物信息
				taskService.addComment(taskId, task.getProcessInstanceId(), comment);// comment为批注内容
				task.setAssignee(username);
				taskService.complete(task.getId(), variables);
			}
//			// 恢复方向
			for (TransitionImpl temp : newTransitions) {
//				LogUtil.info("temp.getDestination().getId()"+temp.getDestination().getId());
//				LogUtil.info(""+currActivity.getOutgoingTransitions().size());
//				LogUtil.info("currActivity.getOutgoingTransitions()"+currActivity.getOutgoingTransitions().
//						get(0).getSource().getId());
				currActivity.getOutgoingTransitions().remove(temp);
				
			}
			for (PvmTransition temp : oriPvmTransitionList) {
				pvmTransitionList.add(temp);
				LogUtil.info("temp.getId()"+temp.getSource().getId());
			}
			LogUtil.info("--------------rollback流程结束------------------\n");

		} catch (Exception e) {
			LogUtil.error("驳回失败");
			LogUtil.error(e.getMessage());
			return "驳回失败";
		}
		return "驳回成功";
	}
	/**
	 * 完成任务，即授权
	 * 
	 * @param username
	 * @param taskId
	 */
	public static void exetask(String username, String taskId, String comment) {
		Authentication.setAuthenticatedUserId(username);
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		taskService.addComment(taskId, task.getProcessInstanceId(), comment);
		taskService.complete(taskId);
		LogUtil.info(username + "完成任务" + taskId+",comment:"+comment+"\n");
	}

	/**
	 * 部署一个项目
	 * 
	 * @param projectName
	 */
	public static void deploytask(String projectName) {
		repositoryService.createDeployment().addClasspathResource("diagrams/" + projectName + ".bpmn")
				.addClasspathResource("diagrams/" + projectName + ".png").deploy();
//		System.out.println("步骤5部署工程"+projectName+"完成");
	}

	/**
	 * 开始一个流程
	 * 
	 * @param projectName
	 */
	public static String starttask(String projectName) {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(projectName);
		return pi.getId();
	}

	// 查询任务的下个人
//	public static void showNextPerson(String procId) {
//		List<Task> tasks = taskService.createTaskQuery().processInstanceId(procId).list();
//		for (Task task : tasks) {
//			ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
//					.getDeployedProcessDefinition(task.getProcessDefinitionId());
//			List<ActivityImpl> activitiList = def.getActivities(); // rs是指RepositoryService的实例
//
//			String excId = task.getExecutionId();
//			ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(excId)
//					.singleResult();
//			String activitiId = execution.getActivityId();
//			for (ActivityImpl activityImpl : activitiList) {
//				String id = activityImpl.getId();
//				if (activitiId.equals(id)) {
//
//					List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();// 获取从某个节点出来的所有线路
//					for (PvmTransition tr : outTransitions) {
//						PvmActivity ac = tr.getDestination(); // 获取线路的终点节点
//						LogUtil.info("当前任务：" + activityImpl.getProperty("name") + "下一步任务任务：" + ac.getProperty("name"));
//					}
//					break;
//				}
//			}
//		}
//	}


	/*
	 * 删除流程定义 id为ACT_RE_DEPLOYMENT的ID
	 */
	public static void deleteProcessDefinition(String id) {
		processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
				.deleteDeployment(id, true);

		System.out.println("删除成功部署项目" + id);
	}
	
	/**
	 * 删除
	 * @param id
	 */
	public static void deleteProcessInstance(String id) {
		runtimeService.deleteProcessInstance(id, "删除原因");
		System.out.println("删除成功任务" + id);
	}
	/**
	 * 界面上点击起始任务taskID，返回该task对应的instance任务链json
	 * @param taskId
	 * @return resultList
	 */
	public static String showDetail(String taskId) {
		JSONObject jo = new JSONObject(new LinkedHashMap<String, Object>());
		List<LinkedHashMap<String, String>> list_map = new ArrayList<LinkedHashMap<String, String>>();
		HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId()).singleResult();
		List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().orderByTaskCreateTime().asc()
				.processInstanceId(processInstance.getId()).list();
		for (HistoricTaskInstance hi : list) {
			System.out.println(hi.getId());
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			map.put("taskId", hi.getId());
			map.put("经手人", (null == hi.getAssignee() ? "" : hi.getAssignee()));
			map.put("comment", getCommentFromTask(hi.getId()));
			map.put("名称", hi.getName());
			map.put("开始时间", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(hi.getStartTime()));
			map.put("结束时间",(null != hi.getEndTime()) ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(hi.getEndTime()) : "");
			list_map.add(map);
		}

		jo.put("resultList", list_map);
		return jo.toJSONString();
	}

	//判断是否这个task关联的process是否是最后一步
	public static boolean isTaskIsLastTask(String taskId) {
		HistoricTaskInstance hi = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
		String procId = hi.getProcessInstanceId();
		HistoricProcessInstance pi = processEngine.getHistoryService()// 与历史数据（历史表）相关的Service
				.createHistoricProcessInstanceQuery()// 创建历史流程实例查询
				.processInstanceId(procId)// 使用流程实例ID查询
				.singleResult();
		
		String instanceId = pi.getId();
		
		List<HistoricTaskInstance> historyTaskList = historyService.createHistoricTaskInstanceQuery()
				.processInstanceId(instanceId).list();
//		HistoricTaskInstance firstHi = historyTaskList.get(0);
		HistoricTaskInstance lastHi = historyTaskList.get(historyTaskList.size()-1);
		String lastName = lastHi.getName();
//		String comment = getCommentFromTask(firstHi.getId());
//		Map<String, Object> argsMap = AuthUtil.getArgsFromCommentString(comment);
//		String type = (String) argsMap.get("type");
		
		//20190929 写死成"财务岗审批"
		if(lastName.equals("财务岗审批")) {
			return true;
		}
		else {
			LogUtil.info("不是最后一步。当前步骤为"+lastName);
		}
		//20190625，全部类型都要改成4步
//		if(type.equals("备款")||type.equals("回款")||type.equals("调账")||type.equals("单笔结算")||type.equals("批量结算")) {
//			if(lastName.equals("财务岗审批")) {
//				return true;
//			}
//		}
//		if(type.equals("备款")||type.equals("回款")||type.equals("调账")) {
//			if(lastName.equals("复核岗审批")||lastName.equals("财务岗审批")) {
//				return true;
//			}
//		}
//		else if (type.equals("单笔结算")||type.equals("批量结算")) {
//			if(lastName.equals("财务岗审批")) {
//				return true;
//			}
//		}
//		else {
//			LogUtil.error("于ifTaskIsLastTask发生异常。type:"+type+",匹配不上");
//			
//		}
		return false;
	}
	
//		
	
	
	
	public static String getIPAddress(HttpServletRequest request) {
	    String ip = null;

	    //X-Forwarded-For：Squid 服务代理
	    String ipAddresses = request.getHeader("X-Forwarded-For");

	    if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
	        //Proxy-Client-IP：apache 服务代理
	        ipAddresses = request.getHeader("Proxy-Client-IP");
	    }

	    if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
	        //WL-Proxy-Client-IP：weblogic 服务代理
	        ipAddresses = request.getHeader("WL-Proxy-Client-IP");
	    }

	    if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
	        //HTTP_CLIENT_IP：有些代理服务器
	        ipAddresses = request.getHeader("HTTP_CLIENT_IP");
	    }

	    if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
	        //X-Real-IP：nginx服务代理
	        ipAddresses = request.getHeader("X-Real-IP");
	    }

	    //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
	    if (ipAddresses != null && ipAddresses.length() != 0) {
	        ip = ipAddresses.split(",")[0];
	    }

	    //还是不能获取到，最后再通过request.getRemoteAddr();获取
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
//	    	LogUtil.YEWU_LOGGER.info("最终还是通过getRemoteAddr");
	        ip = request.getRemoteAddr();
	    }
	    return ip;
	}
	
	
	public static Set<Class<?>> getClassSet(String packageName) {
		Set<Class<?>> classSet = new HashSet<Class<?>>();
		try {
			Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (url != null) {
					String protocol = url.getProtocol();
					if (protocol.equals("file")) {
						String packagePath = url.getPath().replaceAll("%20", " ");
						addClass(classSet, packagePath, packageName);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return classSet;
	}

	private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {
		File[] files = new File(packagePath).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
			}
		});
		for (File file : files) {
			String fileName = file.getName();
			if (file.isFile()) {
				String className = fileName.substring(0, fileName.lastIndexOf("."));
				if (StringUtil.isNotEmpty(packageName)) {
					className = packageName + "." + className;
				}
				doAddClass(classSet, className);
			} else {
				String subPackagePath = fileName;
				if (StringUtil.isNotEmpty(packagePath)) {
					subPackagePath = packagePath + "/" + subPackagePath;
				}
				String subPackageName = fileName;
				if (StringUtil.isNotEmpty(packageName)) {
					subPackageName = packageName + "." + subPackageName;
				}
				addClass(classSet, subPackagePath, subPackageName);
			}
		}
	}

	private static void doAddClass(Set<Class<?>> classSet, String className) {
		Class<?> cls = loadClass(className, false);
		classSet.add(cls);
	}
	
	 public static Class<?> loadClass(String className, boolean isInitialized) {
	        Class<?> cls;
	        try {
	            cls = Class.forName(className, isInitialized, getClassLoader());
	        } catch (ClassNotFoundException e) {
	            throw new RuntimeException(e);
	        }
	        return cls;
	    }

	public static Class<?> loadClass(String className) {
		return loadClass(className, true);
	}
	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
	/*
	 * 根据NAME_CLASS_MAP，调用对应functionName函数，并获取返回值
	 */
	public String invoke(String name,String functionName) {
		BaseClass baseClz = AuthConfig.getNAME_CLASS_MAP().get(name);
		if(null == baseClz)
			return "";
		Method method;
		try {
			method = baseClz.getClass().getMethod(functionName);
			Object obj = method.invoke(baseClz);
			return (String) obj;
		} catch (Exception e) {
			LogUtil.error("",e);
		}
		return "";
	}
	
}