package com.tc;

import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class LogUtil {
	/** 空数组 */
	private static final Object[] EMPTY_ARRAY = new Object[] {};
	/** 全类名 */
	private static final String FQCN = LogUtil.class.getName();

	/** 通过堆栈信息获取调用当前方法的类名和方法名，这边因为封装了一层，所以默认取第二层 */
	private static LocationAwareLogger getLocationAwareLogger(final int stackDepth) {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		StackTraceElement frame = stacks[stackDepth];
		return (LocationAwareLogger) LoggerFactory.getLogger(frame.getClassName());
	}
	
	public static void info(String msg) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.INFO_INT, msg, EMPTY_ARRAY, null);
	}

	public static void error(String msg) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, EMPTY_ARRAY, null);
	}

	public static void error(String msg, Throwable e) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, EMPTY_ARRAY, e);
	}

	public static void debug(String msg) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, EMPTY_ARRAY, null);
	}

	public static void debug(String msg, Throwable e) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, EMPTY_ARRAY, e);
	}

	public static void warn(String msg) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.WARN_INT, msg, EMPTY_ARRAY, null);
	}

	public static void warn(String msg, Throwable e) {
		getLocationAwareLogger(2).log(null, FQCN, LocationAwareLogger.WARN_INT, msg, EMPTY_ARRAY, e);
	}

}
