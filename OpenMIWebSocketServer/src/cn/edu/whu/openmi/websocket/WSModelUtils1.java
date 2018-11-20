package cn.edu.whu.openmi.websocket;

public class WSModelUtils1 {
	public static final byte a = 0;
	
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
