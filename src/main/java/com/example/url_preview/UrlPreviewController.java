package com.example.url_preview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlPreviewController {

	private final UrlPreviewService urlPreviewService;

	public UrlPreviewController(UrlPreviewService urlPreviewService) {
		this.urlPreviewService = urlPreviewService;
	}

	@GetMapping("/api/preview")
	public UrlPreview preview(@RequestParam(name = "url", required = false) String url) {
		System.out.println("Received request for URL preview: " + url);
		return this.urlPreviewService.preview(url);
	}
}
