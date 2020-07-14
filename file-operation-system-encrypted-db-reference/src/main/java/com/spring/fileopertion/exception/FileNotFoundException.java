package com.spring.fileopertion.exception;

public class FileNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 2117317461638366091L;

	public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}