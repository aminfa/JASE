package de.upb.crc901.services.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimeLogger {
	private static final Logger log = LoggerFactory.getLogger(TimeLogger.class);
	
	

	private static long timestart = System.currentTimeMillis();
	
	public synchronized static void STOP_TIME(String label) {
		long difference = System.currentTimeMillis() - TimeLogger.timestart;
		String msg = "-->\t" + label + ":\n\t\t\t" + difference + " ms";
		log.debug(msg);
//		System.out.println(msg);
		RESET_TIME();
	}
	
	public static void RESET_TIME() {
		timestart = System.currentTimeMillis();
	}
	
}
