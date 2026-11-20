package com.example.url_preview;

public class PreviewFetchException extends RuntimeException {

	public PreviewFetchException(String message) {
		super(message);
	}

	public PreviewFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}
