package cn.edu.whu.openmi.util;

public class OpenMIException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;

	public OpenMIException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
