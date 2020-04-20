package com.tc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVUtil {
	public static void readCSV() throws IOException {
		File file = new File("e:\\read.csv");
		FileReader fReader = new FileReader(file);
		CSVReader csvReader = new CSVReader(fReader);
		String[] strs = csvReader.readNext();
		if (strs != null && strs.length > 0) {
			for (String str : strs)
				if (null != str && !str.equals(""))
					System.out.print(str + " , ");
			System.out.println("\n---------------");
		}
		List<String[]> list = csvReader.readAll();
		for (String[] ss : list) {
			for (String s : ss)
				if (null != s && !s.equals(""))
					System.out.print(s + " , ");
			System.out.println();
		}
		csvReader.close();
	}

//	@Test
//	public void test() {
//		List<Map<String, Object>> list_map = new ArrayList<Map<String, Object>>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("id", "000001");
//		map.put("name", "tc");
//		list_map.add(map);
//		String outputPath = "C:\\Users\\Administrator\\Desktop\\空白.csv";
//		writeCSV(list_map, outputPath,false,false);
//	}
	/**
	 * 
	 * @param list
	 * @param forNum 打印条数上限
	 * @param outputPath 文件是追加还是覆盖
	 * @param appendHead 是否加上文件 头 列名
	 */
	public static void writeCSV(List list,int forNum,String outputPath,boolean appendHead) {
		List<Map<String, Object>> csv_list  = new ArrayList<Map<String,Object>>();
		int loopNum = list.size()>forNum?forNum:list.size();
		for(int i=0;i<loopNum;i++) {
			Map<String, Object> map = ArgsUtil.getAllFieldMap(list.get(i));
			ArgsUtil.printMap(map);
			csv_list.add(map);
		}
		CSVUtil.writeCSV(csv_list, outputPath,false,appendHead);
	}
	/**
	 * 
	 * @param list_map
	 * @param outputPath
	 * @param append 文件是追加还是覆盖
	 */
	public static void writeCSVWithObjectList(List<?> list,String outputPath,boolean append) {
		writeCSVWithObjectList(list,outputPath,append,true);
	}
	/**
	 * @param list_map list中直接存着数据库查询结果
	 * @param outputPath 输出路径
	 * @param append 文件是追加还是覆盖
	 * @param appendHead 是否加上文件 头 列名
	 */
	public static void writeCSVWithObjectList(List<?> list,String outputPath,boolean append,boolean appendHead) {
		List<Map<String, Object>> csv_list  = new ArrayList<Map<String,Object>>();
		for(int i=0;i<list.size();i++) {
			Map<String, Object> map = ArgsUtil.getAllFieldMap(list.get(i));
//			ArgsUtil.printMap(map);
			csv_list.add(map);
		}
		CSVUtil.writeCSV(csv_list, outputPath,append,appendHead);
	}
	/**
	 * 
	 * @param list_map
	 * @param outputPath
	 * @param append 文件是追加还是覆盖
	 * @param appendHead 是否加上文件 头 列名
	 */
	public static void writeCSV(List<Map<String, Object>> list_map,String outputPath,boolean append,boolean appendHead) {
		File file = new File(outputPath);
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		Writer writer = null;
		CSVWriter csvWriter = null;
		try {
			
			if(append) {
				writer = new FileWriter(file,true);
			}
			else {
				writer = new FileWriter(file);
			}
			csvWriter = new CSVWriter(writer);//纯追加的话右面加个参数true
			List<String[]> outputList = new ArrayList<String[]>();
//			判断是否需要添加文件头（是否把文件头加入headList,最后把headList加入tempList最终输出）。仅追加一次文件头
			boolean isHeadAppended = false;
			//追加文件内容
			for(Map<String, Object> map:list_map) {
				List<String> tempList = new ArrayList<>();
				List<String> headList = new ArrayList<>();
				for(Entry<String, Object> entry:map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue()+"";
					//是否需要追加文件头
					if(!isHeadAppended) {
						headList.add(key);
					}
					tempList.add(value);
				}
				//是否需要追加文件头
				if(!isHeadAppended && appendHead) {
//					if(!isHeadAppended) {
					isHeadAppended = true;
					String[] array = new String[headList.size()];
					array = headList.toArray(array);
					outputList.add(array);
				}
				String[] array = new String[tempList.size()];
				array = tempList.toArray(array);
				outputList.add(array);
			}
			csvWriter.writeAll(outputList);
			LogUtil.info("写入文件"+outputPath+"成功");
		} catch (IOException e) {
			LogUtil.error("写入csv时发生错误",e);
			e.printStackTrace();
		}
		finally {
			try {
				csvWriter.close();
				writer.close();
			} catch (IOException e) {
				LogUtil.error("关闭writer流时发生错误",e);
				e.printStackTrace();
			}
		}
		
	}
}
