package com.spring.fileopertion.exception;

public class FileStorageException extends RuntimeException {
	private static final long serialVersionUID = 2858851925066362474L;

	public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}