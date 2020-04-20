package com.tc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelUtil {
	public static Workbook loadExcelFile(String InXLSPath) throws IOException {
		FileInputStream is = new FileInputStream(InXLSPath);
		// 根据指定的文件输入流导入Excel从而产生Workbook对象
		Workbook wb0;
		if (InXLSPath.endsWith(".xlsx")) {
			wb0 = new XSSFWorkbook(is);
		} else {
			wb0 = new HSSFWorkbook(is);
		}

		// 获取Excel文档中的第一个表单
		is.close();
		return wb0;
	}
	
	public synchronized static void createExcelWithListMap(Map<Integer, Object> tailMap,
			Map<String, List<Map<String, Object>>> rsMap, String outPutPath, boolean ifSumRow) throws IOException {
		// 文件目录不存在时创建目录
		File file = new File(outPutPath);
		File fileParent = file.getParentFile();
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}
		LogUtil.info("本次写入文件:" + file.getAbsolutePath());

		List<Map<String, Object>> lmap = null;
		// row
		Map<Integer, Double> sumMap = null;
		FileInputStream fs = null;
		Workbook wb = null;
		try {
			fs = new FileInputStream(file.getAbsolutePath());
			wb = new HSSFWorkbook(fs);
		} catch (FileNotFoundException e) {
			wb = new HSSFWorkbook();
		}
		Map<String, CellStyle> styles = createStyles(wb);

		Sheet sheet = null;
		// 表头
		Row row = null;
		Iterator<String> itM = rsMap.keySet().iterator();
		while (itM.hasNext()) {
			String sheetName = itM.next();
			lmap = rsMap.get(sheetName);
			int i = 2;
			// boolean shouldcrateFile = false;
			for (Map<String, Object> map : lmap) {
				// if (!shouldcrateFile) {
				// shouldcrateFile = true;
				if (wb.getSheet(sheetName) != null)
					sheet = wb.getSheet(sheetName);
				else
					sheet = wb.createSheet(sheetName);

				
				row = sheet.createRow(0);
				row.setHeightInPoints(25);
				Cell celltitle = row.createCell(1);
//				celltitle.setCellType(Cell.CELL_TYPE_STRING);
				celltitle.setCellValue(sheetName);
				celltitle.setCellStyle(styles.get("title"));
//				sheet.addMergedRegion(new CellRangeAddress(0, // first row (0-based)
//						0, // last row (0-based)
//						1, // first column (0-based)
//						4 // last column (0-based)
//				));

				// 表头
				row = sheet.createRow(1);
				Iterator<String> headerTxtIt = map.keySet().iterator();
				int j = 1;
				while (headerTxtIt.hasNext()) {
					Cell cell = row.createCell(j - 1);
//					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(headerTxtIt.next());
					j++;
				}

				if (ifSumRow) {
					sumMap = new HashMap<Integer, Double>();
				}
				// }
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				// System.out.println(sheet.getLastRowNum());
				// row = sheet.createRow(i++);
				Iterator<Entry<String, Object>> mapValue = map.entrySet().iterator();

				// int j = 1;
				j = 1;
				while (mapValue.hasNext()) {
					Entry<String, Object> et = mapValue.next();
					Cell cell = row.createCell(j - 1);

					Object obj = et.getValue();
					if (obj == null) {
//						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue("");
					} else if (obj instanceof java.util.Date || obj instanceof java.sql.Date) {
//						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(obj.toString().trim());
					} else if (obj instanceof java.lang.Double || obj instanceof java.lang.Number) {
//						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(Double.parseDouble(obj.toString()));
						if (sumMap != null) {
							if (sumMap.get(j) != null) {
								sumMap.put(j, sumMap.get(j) + Double.parseDouble(obj.toString()));
							} else {
								sumMap.put(j, Double.parseDouble(obj.toString()));
							}
						}
					} else if (obj instanceof java.lang.Integer) {
//						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(Double.parseDouble(obj.toString()));

						if (sumMap != null) {
							if (sumMap.get(j) != null) {
								sumMap.put(j, sumMap.get(j) + Double.parseDouble(obj.toString()));
							} else {
								sumMap.put(j, Double.parseDouble(obj.toString()));
							}
						}
					} else {
//						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(obj.toString().trim());
					}
					j = j + 1;
				}

			}
			if (sumMap != null) {
				row = sheet.createRow(i++);
				Iterator<Entry<Integer, Double>> iter = sumMap.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, Double> entry = iter.next();
					Integer key = entry.getKey();
					Double val = entry.getValue();
					Cell cell = row.createCell(key - 1);
//					cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(val);
				}

			}
			if (tailMap != null) {
				row = sheet.createRow(i++);
				Iterator<Entry<Integer, Object>> iter = tailMap.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, Object> entry = iter.next();
					Integer key = entry.getKey();
					Object val = entry.getValue();
					Cell cell = row.createCell(key);
					// cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellValue(val.toString());
				}

			}
		}
		FileOutputStream fileOut = new FileOutputStream(outPutPath);
		wb.write(fileOut);
		fileOut.close();
	}

	private static Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		CellStyle style;
		Font titleFont = wb.createFont();
		titleFont.setFontHeightInPoints((short) 18);
		style = wb.createCellStyle();
		style.setFont(titleFont);
		styles.put("title", style);

		Font monthFont = wb.createFont();
		monthFont.setFontHeightInPoints((short) 11);
		monthFont.setColor(IndexedColors.WHITE.getIndex());
		style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFont(monthFont);
		style.setWrapText(true);
		styles.put("header", style);

		style = wb.createCellStyle();
		style.setWrapText(true);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("cell", style);
		style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
		styles.put("formula", style);
		style = wb.createCellStyle();
		style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
		styles.put("formula_2", style);

		return styles;
	}
}
