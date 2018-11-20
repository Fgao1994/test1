package cn.edu.whu.openmi.websocket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.openmi.standard.ITime;
import org.openmi.standard.ITimeStamp;

public class WSModelUtils {
	public final static String QUANTITYKEY = "quantity";
	public final static String ELEMENTYKEY = "element";
	public final static String TIMEKEY = "time";
	public final static String METHODKEY = "method";
	public final static String VALUEKEY = "value";
	public final static String TIMEFORMAT = "yyyy-MM-dd hh:mm:ss";
	
	public static final byte a = 0;
	
	//时间格式转换参数
    private static Calendar modifiedJulianDateZero = new GregorianCalendar(1858, 10, 17);
    private static long modifiedJulianDateZeroMsec = modifiedJulianDateZero.getTimeInMillis();
    private static double msecDay = 24 * 60 * 60 * 1000;
	
	public static Map<String, String> parseArguments(String str){
		Map<String, String> paraMap = new HashMap<String, String>();
		String[] parameters = str.split("&");
		for(int i=0;i<parameters.length;i++){
			String[] keyValue = parameters[i].split("=");
			if (keyValue.length<2)
				continue;
			
			paraMap.put(keyValue[0], keyValue[1]);
		}
		return paraMap;
	}
	
	public static boolean isFormerThan(String formerTime, String laterTime) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMAT);
			Date formerDate = sdf.parse(formerTime);
			Date laterDate = sdf.parse(laterTime);
			if (formerDate.getTime() < laterDate.getTime()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

	}
	
	public static String timeToString(ITime time) {
        String timeStr="";
        SimpleDateFormat format = new SimpleDateFormat(TIMEFORMAT);
        if (time instanceof ITimeStamp) {
            timeStr = format.format(modifiedJulian2Gregorian(((ITimeStamp) time).getModifiedJulianDay()).getTime());
        }
        return timeStr;
    }
	
    private static Calendar modifiedJulian2Gregorian(double modifiedJulianDate) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(modifiedJulianDateZeroMsec + Math.round(modifiedJulianDate * msecDay));
        return c;
    }
    
 public static String appendToByte(String str, int length){
    	
    	//Random random = new Random();  
    	if (str.getBytes().length < length) {
    		
    		StringBuffer strbuffer = new StringBuffer(str);
        	strbuffer.append("&tempValue=");
        	while(strbuffer.toString().getBytes().length < length){
        		strbuffer.append(a);
        	}
        	return strbuffer.toString();
			
		}else{
			return "Error: The string is larger than set length !";
		}
    	
    	
    	
    }
 public static String appendToByte(double str, int length){
 	
 	//Random random = new Random();  
 	
 		
 		StringBuffer strbuffer = new StringBuffer(String.valueOf(str));
     	strbuffer.append("&");
     	while(strbuffer.toString().getBytes().length < length){
     		strbuffer.append(a);
     	}
     	return strbuffer.toString();
		
 	
 	
 	
 }
    
    
    public static void main(String[] args){
    	
    	String getReq = WSModelUtils.METHODKEY + "=get&" + "PET_Value"
				+ "=" + 2.54 + "&" + "Precip_Value" + "="
				+ 3.666777;
    	
    	String reString = appendToByte(getReq,100);
    	System.out.println(reString);
    	System.out.println(appendToByte(getReq,10).getBytes().length);
    	
    }
    
}
