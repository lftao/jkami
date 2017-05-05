package com.javatao.jkami;

/**
 * 异常信息
 *
 */
public class JkException extends RuntimeException {

	private static final long serialVersionUID = 7430449458407101783L;

	public JkException(String message){
		super(message);
	}
	
	public JkException(Throwable cause)
	{
		super(cause);
	}
	
	public JkException(String message,Throwable cause)
	{
		super(message,cause);
	}
}
