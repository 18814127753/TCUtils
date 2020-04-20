package com.tc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//工具类
public class ArgsUtil {

	/**
	 * 文件的上级目录不存在时创建之
	 */
	public static void initFile(String absolutePath) {
		File file = new File(absolutePath);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
		}
	}
	/**
	 * 获取今天HHmmss
	 */
	public static String getTodayTimeString() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		return new SimpleDateFormat("HHmmss").format(date);
	}

	public static void printMap(Map<String, ?> map) {
		for (String key : map.keySet()) {
			System.out.println(key + ":" + map.get(key));
		}
	}

	public static Map<String, Object> getAllFieldMap(Object obj) {
		Map<String, Object> map = new LinkedHashMap<>();

		Field[] field = obj.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
		try {
			for (int j = 0; j < field.length; j++) { // 遍历所有属性
				String name = field[j].getName(); // 获取属性的名字
				name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
				String type = field[j].getGenericType().toString(); // 获取属性的类型
				// System.out.println("name:"+name+",type:"+type);
				if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class "，后面跟类名
					Method m = obj.getClass().getMethod("get" + name);
					String value = (String) m.invoke(obj); // 调用getter方法获取属性值
					if (value == null) {
						m = obj.getClass().getMethod("set" + name, String.class);
						m.invoke(obj, "");
						value = "";
					}
					map.put(name, value);
				}
				if (type.equals("class java.lang.Integer")) {
					Method m = obj.getClass().getMethod("get" + name);
					Integer value = (Integer) m.invoke(obj);
					if (value == null) {
						m = obj.getClass().getMethod("set" + name, Integer.class);
						m.invoke(obj, 0);
					}
					map.put(name, value+"");
				}
				if (type.equals("class java.util.Date")) {
					Method m = obj.getClass().getMethod("get" + name);
					Date value = (Date) m.invoke(obj);
					if (value == null) {
						m = obj.getClass().getMethod("set" + name, Date.class);
						m.invoke(obj, new Date());
					}
					map.put(name, value);
				} // 如果有需要,可以仿照上面继续进行扩充,再增加对其它类型的判断
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return map;
	}

	public static void download(String filePath, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		File file = new File(filePath);
		FileInputStream is = new FileInputStream(file);

		String userAgent = req.getHeader("User-Agent");
		String oraFileName = file.getName();
		String formFileName = oraFileName;
		if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
			formFileName = java.net.URLEncoder.encode(formFileName, "UTF-8");
		} else {
			// 非IE浏览器的处理：
			formFileName = new String(formFileName.getBytes("UTF-8"), "ISO-8859-1");
		}
		resp.reset();
		resp.setContentType("multipart/form-data");
		resp.setContentType("application/vnd.ms-excel;charset=utf-8");
		resp.setHeader("Content-Disposition", "attachment;filename=" + formFileName);
		ServletOutputStream out = resp.getOutputStream();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(out);
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (final IOException e) {
			throw e;
		} finally {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
			if (is != null)
				is.close();
			
		}
	}

	/**
	 * 获取前一天yyMMdd
	 */
	public static String getYesterdayStr() {
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar c1 = Calendar.getInstance();

		c1.set(Calendar.DATE, c1.get(Calendar.DATE) - 1);
		String dstr = dtFormat.format(c1.getTime());
		return dstr;
	}

	/**
	 * 获取今天yyyyMMdd
	 */
	public static String getTodayString() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}
}
