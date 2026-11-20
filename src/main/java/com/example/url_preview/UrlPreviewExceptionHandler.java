package com.example.url_preview;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UrlPreviewExceptionHandler {

	@ExceptionHandler(UrlValidationException.class)
	public ResponseEntity<PreviewError> handleInvalidUrl(UrlValidationException exception) {
		return ResponseEntity.badRequest().body(new PreviewError(exception.getMessage()));
	}

	@ExceptionHandler(PreviewFetchException.class)
	public ResponseEntity<PreviewError> handleFetchFailure(PreviewFetchException exception) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
			.body(new PreviewError(exception.getMessage()));
	}
}
