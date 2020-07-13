package com.spring.fileopertion.exception;

public class FileValidationException extends RuntimeException {
	private static final long serialVersionUID = 2117317461638366091L;

	public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}