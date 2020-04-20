package com.settv5ToYL.Utils.auth;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;

//一般不需要执行，在添加用户、迁移数据库创表时用
//根据activiti.cfg.xml创建数据库，初始化数据
public class InitData_1 {
	public static void main(String[] args) throws SQLException {
	
		
		//1
//		initDataBase();
		//2
		addUser();
		//3
//		addGroup();
		//4
//		addRelationShip();
		//5
//		deploy();
		
		System.out.println("初始化数据完成");
	}
	
	public static void deploy() {
		
		AuthUtil.deploytask("fund");
		AuthUtil.deploytask("prepareMoney");
		
	}
	
	public static void addRelationShip() {
		UserUtil.saveMembership("jingb1", "jingbanGroup");
		UserUtil.saveMembership("jingb2", "jingbanGroup");
		UserUtil.saveMembership("fuhe1", "fuheGroup");
		UserUtil.saveMembership("fuhe2", "fuheGroup");
		UserUtil.saveMembership("shenpi1", "shenpiGroup");
		UserUtil.saveMembership("shenpi2", "shenpiGroup");
		UserUtil.saveMembership("caiwu1", "caiwuGroup");
		UserUtil.saveMembership("caiwu2", "caiwuGroup");
		System.out.println("步骤4添加用户-组关系");
	}
	
	public static void addGroup() {
		UserUtil.saveGroup("jingbanGroup");
		UserUtil.saveGroup("fuheGroup");
		UserUtil.saveGroup("shenpiGroup");
		UserUtil.saveGroup("caiwuGroup");
		System.out.println("步骤3添加组完成");
	}
	
	public static void addUser() throws SQLException {
		UserUtil.saveUser("jingb1");
		UserUtil.saveUser("jingb2");
		UserUtil.saveUser("fuhe1");
		UserUtil.saveUser("fuhe2");
		UserUtil.saveUser("shenpi1");
		UserUtil.saveUser("shenpi2");
		UserUtil.saveUser("caiwu1");
		UserUtil.saveUser("caiwu2");
		UserUtil.saveUser("admin");
		UserUtil.saveUser("chaxun1");
		UserUtil.saveUser("chaxun2");
	
		System.out.println("步骤2添加用户完成");
	}
	
	public static void initDataBase() {
		// 1。 创建Activiti配置对象的实例
				ProcessEngineConfiguration configuration = ProcessEngineConfiguration
						.createStandaloneProcessEngineConfiguration();
				// 2. 设置数据库连接信息
				// 设置数据库地址
				ResourceBundle resource = ResourceBundle.getBundle("jdbc");
				String jdbc_url = resource.getString("jdbc.url"); 
				String jdbc_driver = resource.getString("jdbc.driverClassName"); 
				String jdbc_username = resource.getString("jdbc.username"); 
				String jdbc_password = resource.getString("jdbc.password");
				configuration.setJdbcUrl(jdbc_url);
//				configuration.setJdbcUrl("jdbc:sqlserver://127.0.0.1:1433;databaseName=activiti;autoReconnect=true");
//				configuration.setJdbcUrl("jdbc:mysql://localhost:3306/activiti?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8");
				// 数据库驱动
				configuration.setJdbcDriver(jdbc_driver);
//				configuration.setJdbcDriver("net.sourceforge.jtds.jdbc.Driver");
//				configuration.setJdbcDriver("com.mysql.jdbc.Driver");
				// 用户名
				configuration.setJdbcUsername(jdbc_username);
				// 密码
				configuration.setJdbcPassword(jdbc_password);
				// 设置数据库建表策略(默认不会建表)
				configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
				// 3. 使用配置对象创建流程引擎实例（检查数据库连接等环境信息是否正确）
				System.out.println("======:"+configuration);
				ProcessEngine processEngine = configuration.buildProcessEngine();
				
				System.out.println("======:"+processEngine);
				try {
				  // 1。 加载classpath下名为activiti.cfg.xml文件，创建核心流程引擎对象
//					  ProcessEngine processEngine2 = ProcessEngineConfiguration.
//							  createProcessEngineConfigurationFromResource("activiti.cfg.xml").
//							  buildProcessEngine();
					UserUtil.init();
					  System.out.println("步骤1初始化数据库完成");
				 //或者 private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();此方法默认和上面一样
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
}
