package com.tc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static void compress(String srcFilePath, String destFilePath) {
		File src = new File(srcFilePath);
		if (!src.exists()) {
			throw new RuntimeException(srcFilePath + "不存在");
		}

		File zipFile = new File(destFilePath);
		try {
			FileOutputStream fos = new FileOutputStream(zipFile);
			CheckedOutputStream cos = new CheckedOutputStream(fos, new CRC32());
			ZipOutputStream zos = new ZipOutputStream(cos);
			String baseDir = "";
			compressbyType(src, zos, baseDir);
			zos.close();
		} catch (Exception e) {
			LogUtil.error("压缩文件时出错",e);
		}
	}

	public static void compressbyType(File src, ZipOutputStream zos, String baseDir) {
		if (!src.exists())
			return;
		LogUtil.info("压缩:" + baseDir + src.getName());
		if (src.isFile()) {
			compressFile(src, zos, baseDir);
		} else if (src.isDirectory()) {
			compressDir(src, zos, baseDir);
		}
	}

	public static void compressFile(File file, ZipOutputStream zos, String baseDir) {
		if (!file.exists()||file.getName().indexOf("xls")>=0)
			return;
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			ZipEntry entry = new ZipEntry(file.getName());
//			ZipEntry entry = new ZipEntry(baseDir + file.getName());
			zos.putNextEntry(entry);
			int count;
			byte[] buf = new byte[1024];
			while ((count = bis.read(buf)) != -1) {
				zos.write(buf, 0, count);
			}
			bis.close();
		} catch (Exception e) {
			LogUtil.error("",e);
		}
	}

	public static void compressDir(File dir, ZipOutputStream zos, String baseDir) {
		if (!dir.exists())
			return;
		File[] files = dir.listFiles();
		if (files.length == 0) {
			try {
				zos.putNextEntry(new ZipEntry(dir.getName() + File.separator));
//				zos.putNextEntry(new ZipEntry(baseDir + dir.getName() + File.separator));
			} catch (IOException e) {
				LogUtil.error("",e);
			}
		}

		for (File file : files) {
			
//			if(file.getName().indexOf("zip")>0) {
//				LogUtil.LOGGER.info(file.getName()+"删除");
//				file.delete();
//			}
			
			//重复运行时会生成多个zip文件，过滤掉zip不压缩
			 if(file.getName().indexOf("zip")<0) {
				compressbyType(file, zos, baseDir + dir.getName() + File.separator);
			}
			
		}
	}
}
