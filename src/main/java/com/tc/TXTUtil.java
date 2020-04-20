package com.tc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

public class TXTUtil {
	public static boolean writeTxtFile(String content, String fileName,boolean append) throws Exception {
		File file = new File(fileName);

		File fileParent = file.getParentFile();
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}

		boolean flag = false;
		
		
		
		OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(file,append), "GB18030");
		try {
			op.append(content);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			op.flush();
			op.close();
		}
		return flag;
	}
	 
	public static void writeTXT_NIO(String content, String fileName) {
		RandomAccessFile raf = null;
		File file = new File(fileName);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
		}
		try{  
            /**以读写的方式建立一个RandomAccessFile对象**/  
            raf=new RandomAccessFile(fileName, "rw");  
            
            //将记录指针移动到文件最后  
        	raf.seek(raf.length());  
            raf.write(content.getBytes("UTF-8"));  
              
        }catch(Exception e){  
            e.printStackTrace();  
        } finally {
			try {
				raf.close();
			} catch (IOException e) {
				LogUtil.error("写入文件时发生错误",e);
				e.printStackTrace();
			}
		}
		
	}
	
	public static void insert(String fileName,long points,String insertContent){  
		RandomAccessFile raf = null;
		FileOutputStream tmpOut = null;
		FileInputStream tmpIn = null;
        try{  
	        File tmp=File.createTempFile("tmp", null);  
	        tmp.deleteOnExit();//在JVM退出时删除  
	          
	        raf=new RandomAccessFile(fileName, "rw");  
	        //创建一个临时文件夹来保存插入点后的数据  
	        tmpOut=new FileOutputStream(tmp);  
	        tmpIn=new FileInputStream(tmp);  
	        
	        raf.seek(points);  
	        /**将插入点后的内容读入临时文件夹**/  
	          
	        byte [] buff=new byte[1024];  
	        //用于保存临时读取的字节数  
	        int hasRead=0;  
	        //循环读取插入点后的内容  
	        while((hasRead=raf.read(buff))>0){  
	            // 将读取的数据写入临时文件中  
	            tmpOut.write(buff, 0, hasRead);  
	        }  
	          
	        //插入需要指定添加的数据  
	        raf.seek(points);//返回原来的插入处  
	        //追加需要追加的内容  
	        raf.write(insertContent.getBytes("GB18030"));  
	        //最后追加临时文件中的内容  
	        while((hasRead=tmpIn.read(buff))>0){
	            raf.write(buff,0,hasRead);  
	        }
        }catch(Exception e){
            LogUtil.error("写入文件时发生错误",e);
        }finally {
			try {
				if(tmpIn!=null)
					tmpIn.close();
				if(tmpOut!=null)
					tmpOut.close();
				if(raf!=null)
					raf.close();
			} catch (IOException e) {
				LogUtil.error("写入文件在关闭时发生错误",e);
			}
           
       }
        
       
    }   

	 
	 
}
