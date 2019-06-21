package tech.stdev.core2;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Log{
	
	private static Logger logger = Logger.getGlobal();
	
	public static void log(Level level, String msg){
		logger.log(level, msg);
	}
}
