package cn.edu.whu.openmi.util;

public class OpenMIError {
	private StringBuffer errBuf = null;
	public OpenMIError(){
		errBuf = new StringBuffer();
	}
	
	public void add(String err){
		this.errBuf.append(err).append("\r\n");
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.errBuf.toString();
	}
}
