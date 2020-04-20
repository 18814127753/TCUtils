package com.tc;


import java.sql.SQLException;
import java.util.ResourceBundle;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;

/**
 * 从settV5ToYL挪过来的，其实本程序中指用到了登录
 * @date 20190819
 * @author tc 
 *
 */
public class UserUtil{
    /**
     * 获取默认流程引擎实例，会自动读取activiti.cfg.xml文件
     */
    static ProcessEngine processEngine;
    static ResourceBundle resourceActive;
    public static void init(){
    	if(processEngine == null) {
    		ProcessEngineConfiguration configuration = ProcessEngineConfiguration
					.createStandaloneProcessEngineConfiguration();
			// 2. 设置数据库连接信息
			// 设置数据库地址
        	ResourceBundle resource = ResourceBundle.getBundle("application");
        	String conf = resource.getString("spring.profiles.active"); 
        	resourceActive = ResourceBundle.getBundle("application"+"-"+conf);
        	
			String jdbc_url = resourceActive.getString("spring.datasource.url"); 
			String jdbc_driver = resourceActive.getString("spring.datasource.driverClassName"); 
			String jdbc_username = resourceActive.getString("spring.datasource.username"); 
			String jdbc_password = resourceActive.getString("spring.datasource.password");
			configuration.setJdbcUrl(jdbc_url);
			configuration.setJdbcDriver(jdbc_driver);
			configuration.setJdbcUsername(jdbc_username);
			configuration.setJdbcPassword(jdbc_password);
			// 设置数据库建表策略(默认不会建表)
			configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
			// 3. 使用配置对象创建流程引擎实例（检查数据库连接等环境信息是否正确）
			processEngine = configuration.buildProcessEngine();
    	}
    }
    
    public static void modifyPassword(String username,String password) {
    	if(processEngine == null) {
    		init();
    	}
    	IdentityService identityService =  processEngine.getIdentityService();
    	User user=identityService.createUserQuery().userId(username).singleResult();
		user.setPassword(password);
		identityService.saveUser(user);
		System.out.println("用户:"+username+"修改密码成功");
    }
    
    public static boolean findUserById(String userId,String password) {
    	if(processEngine == null) {
    		init();
    	}
    	System.out.println(processEngine);
    	IdentityService identityService =  processEngine.getIdentityService();
    	boolean result = identityService.checkPassword(userId,password);
    	LogUtil.info("验证用户名密码"+result);
    	return result;
    }
    
    /**
     * 添加用户测试
     */
    public static void saveUser(String username) throws SQLException {
//    	System.out.println(processEngine);
    	if(processEngine == null) {
    		init();
    	}
    	try {
    		IdentityService identityService = processEngine.getIdentityService();
            User user1=new UserEntity();
            user1.setId(username);
            String password = username.equals("admin")?"syladmin":"1";
            user1.setPassword(password);
            user1.setEmail("");
            user1.setFirstName("");
            user1.setLastName("");
            identityService.saveUser(user1);
    	}
		
        catch (Exception e) {
        	System.out.println("用户名:"+username+"已经存在;"+e.toString());
		}
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(String username) {
    	if(processEngine == null) {
    		init();
    	}
        IdentityService identityService=processEngine.getIdentityService();
        identityService.deleteUser(username);
    }
    
      /**
     * 测试添加组（角色）
     */
    public static  void saveGroup(String groupName){
//    	System.out.println(processEngine);
    	if(processEngine == null) {
    		init();
    	}
    	try {
    		IdentityService identityService=processEngine.getIdentityService();
            Group group=new GroupEntity(); // 实例化组实体
            group.setId(groupName);
            group.setName(groupName);
            identityService.saveGroup(group);
    	}
        catch (Exception e) {
        	System.out.println("组名:"+groupName+"已经存在");
		}
    }
    
    /**
     * 测试删除组(角色)
     */
    public static  void deleteGroup(String groupName){
    	if(processEngine == null) {
    		init();
    	}
        IdentityService identityService=processEngine.getIdentityService();
        identityService.deleteGroup(groupName);
    }
    
    /**
     * 测试添加用户和组（角色）关联关系
     */
    public static  void saveMembership(String username,String groupName){
    	if(processEngine == null) {
    		init();
    	}
    	try {
    		IdentityService identityService=processEngine.getIdentityService();
            identityService.createMembership(username,groupName);
    	}
        catch (Exception e) {
        	System.out.println("关系:"+username+"与"+groupName+"已经存在");
		}
    }
    
    /**
     * 测试删除用户和组（角色）关联关系
     */
    public static  void deleteMembership(String username,String groupName){
    	if(processEngine == null) {
    		init();
    	}
        IdentityService identityService=processEngine.getIdentityService();
        identityService.deleteMembership(username,groupName);
    }
    
}