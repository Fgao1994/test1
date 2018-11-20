package cn.edu.whu.openmi.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class InitEnv {
	public static final String LOG4J = "/config/log4j.properties";
	public static void doInit(){
		InputStream inputStream = InitEnv.getStreamFromResource(LOG4J);
        PropertyConfigurator.configure(inputStream);
        if (inputStream!=null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        Logger logger = Logger.getLogger(InitEnv.class);
        logger.info("Start the logger");
	}
	
	public static InputStream getStreamFromResource(String path) {
		return InitEnv.class.getClass().getResourceAsStream(path);
	}
}
