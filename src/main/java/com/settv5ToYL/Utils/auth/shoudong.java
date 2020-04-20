package com.settv5ToYL.Utils.auth;


import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//import net.sf.json.JSONObject;

//<!-- 1銆�  璧勯噾鍒掍粯缁忓姙宀楋細璐熻矗鏂板缓鏀粯鍦烘锛屾彁浜ゅ垝浠樻暟鎹寘銆� -->
//<!-- 2銆�  璧勯噾鍒掍粯澶嶆牳宀楋細璐熻矗鏀粯鍦烘鍙婂垝浠樻暟鎹寘鐨勫鏍搞�� -->
//<!-- 3銆�  涓氬姟涓荤宀楋細璐熻矗鏀粯鍦烘銆佸垝浠樻暟鎹寘銆佽祫閲戝強瓒呴鐨勫鎵广�� -->
//<!-- 4銆�  璐㈠姟瀹℃牳宀楋細璐熻矗瀵规敮浠樺満娆°�佸垝浠樻暟鎹寘鐨勫鏍稿強璧勯噾鍒掍粯鐨勬巿鏉冦�� -->

//濡傛灉绋嬪簭鎶ラ敊浜嗗彲浠ュ湪杩欓噷鎵嬪姩鎵ц涓�閮ㄥ垎浠ｇ爜锛屼竴鑸笉浼氬嚭闂銆�
//鏈夊皬閮ㄥ垎鎿嶄綔绋嬪簭涓槸娌℃湁璋冪敤鐨�:
//1.鏌愪釜instance娴佺▼鏈夐棶棰橈紝闇�瑕佹墜鍔ㄦ墽琛屽垹闄nstance
//2.鏌愪釜task鏈夐棶棰橈紝闇�瑕佹墜鍔ㄥ垹闄ask
//3.閮ㄧ讲/鍒犻櫎閮ㄧ讲

//鐗瑰埆娉ㄦ剰 鍒犻櫎浠诲姟鏃惰鍘荤湅waiting琛紝涔熻鍒犳帀
public class shoudong {
	public static void main(String[] args) {

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		// SDKConfig.getConfig().loadPropertiesFromSrc();
		
		
//		1.褰撲换鍔ug鏃讹紝鍒犳帀instance,杩欓噷鐨勫垹闄ゆ槸鍒犻櫎姝ｅ湪杩愯鐨勶紝鎵�浠ヨ鍙栨渶鍚庝竴鏉＄殑浠诲姟id
//		娉ㄦ剰鍒犻櫎浠诲姟鍚庤鍘籷ingsuan_auth_waitingQueue琛ㄥ垹闄ゅ搴旂殑璁板綍
//		AuthUtil.deleteProcessInstance("admin","270013", "TC鍚庡彴鍒犻櫎");
		AuthUtil.deleteProcessInstanceByTaskId("admin", "270260", "TC鍚庡彴鍒犻櫎");
		
//		2.鏌愪釜task鏈夐棶棰橈紝闇�瑕佹墜鍔ㄥ垹闄ask
//		AuthUtil.deleteTask(taskID);
		
//		3.閮ㄧ讲resource/diagrams鐨勬祦绋嬪浘
//		AuthUtil.deploytask("fund");
//		寮�鍚竴涓祦绋嬪浘浠诲姟瀹炰緥instance,鐜板湪宸茬粡鏁村悎鍒扮粡鍔炲矖鐢宠涓紝涓嶈鎵嬪姩鍙戣捣,浼氱己灏戞壒娉ㄩ儴鍒�
//		AuthUtil.starttask("fund");
//		鍒犻櫎閮ㄧ讲
//		AuthUtil.deleteProcessDefinition("9");
		
//		鎵归噺缁撶畻涓牴鎹壒娉ㄥ彇寰楀弬鏁�
//		AuthUtil.getArgsFromCommentString("绫诲瀷:[鎵归噺缁撶畻];鍦烘:[2];绗旀暟:[78];閲戦:[95580.85]");
		
		//鏄剧ず鏃ユ湡鍖洪棿鍐呯殑浠诲姟
//		String a = AuthUtil.showHistoryTask("20190201","20190221");
//		System.out.println(a);
		
		//鏄剧ずinstance鏄惁瀹屾垚
//		AuthUtil.isFinished("45001");
		
		//妫�鏌rocess/rollback鏉冮檺
//		boolean a = AuthUtil.permissionCheck("zhuguan1","40004");
//		System.out.println(a);
		
		


		//鍥為��
//		AuthUtil.rollback("fuhe1","117504", "閲戦涓嶅");
		
		
		//鏌ユ壘鎴戠殑灏忕粍浠诲姟
//		System.out.println(AuthUtil.findMyGroupTask("fuhe1"));
		
		//鏍规嵁processInstantce鐨処d杩斿洖杩欎釜instance绗竴涓猼ask鐨処D,浣滀负鏄剧ず鐢�
//		System.out.println(AuthUtil.findFirstTaskIdByProcessInstanceId("72501"));

		
		//缁忓姙鏌ョ湅鍙帴鎵嬩换鍔�
//		System.out.println(AuthUtil.findMyGroupTask("jingban1"));
//		AuthUtil.claim("jingban1","72505");
//		AuthUtil.claimBack("jingban1","40009");
//		AuthUtil.findMyTask("jingban1");
//		AuthUtil.exetask("jingban1","72505","澶囨","缁忓姙1瀹℃壒閫氳繃");
		
		
//		AuthUtil.findNext("40005");
		
		
		//澶嶆牳1锛屾煡鐪嬫垜鐨勫皬缁勪换鍔�
//		System.out.println(AuthUtil.findMyGroupTask("fuhe1"));
//		鏌ョ湅鎵规敞
//		List<Comment> list = AuthUtil.getComments("77510");
//		for(Comment comment:list) {
//			System.out.println(comment.getFullMessage());
//		}
		
		//鎵ц浠诲姟鍒颁笅涓�姝ワ紝涓嶈鍗曠嫭鎵ц锛屼細纭疄鎵规敞閮ㄥ垎
//		Util.exetask("42502");
		
		

//		UserUtil.modifyPassword("jingban1","123");
		
		//鏌ヨ浠诲姟鏄庣粏锛岄渶瑕佷紶鍏askID
//		String detailJson = AuthUtil.showDetail("232509");
//		System.out.println(detailJson);
		
		
		
//		UserUtil.findUserById("admin", "syladmin");
		System.out.println("鎵ц瀹屾瘯");
			ctx.close();
	}
	
	
	
	
	
	
}
