package com.nisum.exception.custom;

public class IncorrectInputException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	String message;

	public IncorrectInputException() {
		super();
	}

	public IncorrectInputException(String message) {
		super(message);
		this.message = message;
	}

}
