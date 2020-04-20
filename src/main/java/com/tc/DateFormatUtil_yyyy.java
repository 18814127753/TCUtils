package com.tc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
 
public class DateFormatUtil_yyyy {
 
    private static ThreadLocal<DateFormat> sdfThreadLocal =  new ThreadLocal<DateFormat>(){
            @Override
            public SimpleDateFormat initialValue(){
               return  new SimpleDateFormat("yyyyMMdd");
            }
    };
 
    public static  String format(Date date){
        return sdfThreadLocal.get().format(date);
    }
 
    public static Date parse(String strDate){
 
        try {
			return sdfThreadLocal.get().parse(strDate);
		} catch (ParseException e) {
			LogUtil.error("转换日期出错",e);
			e.printStackTrace();
		}
		return null;
    }
}
