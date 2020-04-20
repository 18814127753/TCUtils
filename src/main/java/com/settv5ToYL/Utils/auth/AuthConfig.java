package com.settv5ToYL.Utils.auth;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tc.LogUtil;




/**
 * 在openAuth为true时的一系列操作，主要涉及到waiting临时表的操作
 * @author tc
 *
 */
@Component
public class AuthConfig {
	final static String PATH = "com.settv5ToYL.Utils.auth.NeedAuthFunction"; 
	@Autowired
	private static AuthConfig authConfig;
	//储存着key:"备付金互转",value:Class。通过这个map利用反射调用方法，可以少写一些硬编码在service中

	
	
	/**
	 * 生成的批注，修改前台显示时在这里修改。
	 * 注意修改对应的解析函数{@link com.settv5ToYL.Utils.auth.AuthConfig#getArgsFromCommentString 对应的解析函数} 
	 * @param applyType
	 * @param bishu
	 * @param changCi
	 * @param amount
	 * @param username
	 * @param args
	 * @return
	 * 
	 */
	public static String generateComment(String applyType, String bishu, String changCi,String amount,  String username,String[] args) {
		String comment = "";
		if(applyType.equals("调账")||applyType.equals("额度调减")||applyType.equals("额度调增")) {
			comment = "类型:[" + applyType + "];金额:[" + amount + "]";
		}
		else if (applyType.equals("备付金互转")||applyType.equals("备款")) {
			comment = "类型:[" + applyType + "];金额:[" + amount + "];头寸["+args[0]+"]";
		}
		else if (applyType.equals("回款")) {
			comment = "类型:[" + applyType + "];金额:[" + amount + "];头寸["+args[0]+"];结转用途:["+args[1]+"]";
		}
		else if(applyType.equals("单笔结算"))
		{
			comment = "类型:[" + applyType + "];收款账号:["+args[0]+"];名称:["+args[1]+"];金额:[" + amount + "]";
		}
		else if (applyType.equals("批量结算")) {
			comment = "类型:[" + applyType + "];场次:[" + changCi + "];笔数:[" + bishu + "];金额:[" + amount + "]";
		}
		else {
			LogUtil.error("==========结束apply发起申请.找不到申请对应类型=============");
			return "找不到申请对应类型";
		}
		LogUtil.info("本次发起:"+comment);
		return comment;
	}
	
	/**
	 * 从批注中提取参数至map
	 */
	public static Map<String, Object> getArgsFromCommentString(String firstTaskComment) {
		String str = firstTaskComment;
		if(null==str || str.equals("")) {
			return null;
		}
		String type = null;
		String changci = null;
		String bishu = null;
		String amount = null;
		String payeeAcctNo = null;
		String payeeAcctName = null;
		String insSeq = null;
		String cfdPps = null;
		
		str = str.substring(str.indexOf("[") + 1, str.length());
		type = str.substring(0, str.indexOf("]"));
		
		if(type.equals("单笔结算")) {
			str = str.substring(str.indexOf("[") + 1, str.length());
			payeeAcctNo = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			payeeAcctName = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			amount = str.substring(0, str.indexOf("]"));
		}
		else if(type.equals("批量结算")) {
			str = str.substring(str.indexOf("[") + 1, str.length());
			changci = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			bishu = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			amount = str.substring(0, str.indexOf("]"));
		}
		else if (type.equals("备款")||type.equals("备付金互转")) {
			str = str.substring(str.indexOf("[") + 1, str.length());
			amount = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			insSeq = str.substring(0, str.indexOf("]"));
		}
		else if (type.equals("回款")) {
			str = str.substring(str.indexOf("[") + 1, str.length());
			amount = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			insSeq = str.substring(0, str.indexOf("]"));
			str = str.substring(str.indexOf("[") + 1, str.length());
			cfdPps = str.substring(0, str.indexOf("]"));
		}
		else {
			str = str.substring(str.indexOf("[") + 1, str.length());
			amount = str.substring(0, str.indexOf("]"));
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("changci", changci);
		map.put("bishu", bishu);
		map.put("amount", amount);
		map.put("txnAmt", amount);
		map.put("payeeAcctNo", payeeAcctNo);
		map.put("payeeAcctName", payeeAcctName);
		map.put("insSeq", insSeq);
		map.put("cfdPps", cfdPps);
		return map;
	}
	
	public Map<String, Object> sendBaoWen(Map<String, Object> paramMap) {
		Map<String, Object> resultMap = new HashMap<>();
		String filepath = null;
		if (paramMap.get("filepath") != null)
			filepath = (String) paramMap.get("filepath");
		String type = (String) paramMap.get("type");
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, Object> entry : paramMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue() + "");
		}
		LogUtil.info("权限审批最后一步通过，准备发送报文。报文类型:" + type);
		switch (type) {
//		case "备款":
//			return authConfig.mainService.beikuan(map);
//		case "回款":
//			return authConfig.mainService.huikuan(map);
//		case "调账":
//			return authConfig.mainService.tiaozhang(map);
//		case "单笔结算":
//			return authConfig.mainService.singleFundSettlement(map);
//		case "批量结算":
//			return authConfig.mainService.batchFundSettlement(filepath);
//		case "额度调增":
//			return authConfig.mainService.eDuTiaoZeng(map);
//		case "额度调减":
//			return authConfig.mainService.eDuTiaoJian(map);
//		case "备付金互转":
//			return authConfig.mainService.beiFuJinHuZhuan(map);

		default:
			LogUtil.error("没有找到合适类型报文switch走了default出口");
			resultMap.put("rspMessage", "后台报错。发送报文时找不掉匹配的交易类型");
			return resultMap;
		}
	}
	
	
	//工具类用spring注入会有问题，得用这个解决
	@PostConstruct
    public void postConstruct() {
		authConfig = this;
    }
}
