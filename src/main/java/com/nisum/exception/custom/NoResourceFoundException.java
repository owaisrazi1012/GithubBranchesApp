package com.nisum.exception.custom;

public class NoResourceFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	String message;

	public NoResourceFoundException() {
		super();
	}

	public NoResourceFoundException(String message) {
		super(message);
		this.message = message;
	}

}
