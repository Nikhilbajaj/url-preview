package com.example.url_preview;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.InetAddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UrlPreviewControllerTests {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() throws Exception {
		InetAddress publicAddress = InetAddress.getByName("8.8.8.8");
		UrlSafetyValidator validator = new UrlSafetyValidator(host -> new InetAddress[] { publicAddress });
		UrlPreviewService service = new UrlPreviewService(
				validator,
				uri -> new PageFetcher.FetchedPage("""
						<html><head>
						<meta property="og:title" content="Example page">
						<meta property="og:description" content="A sample description">
						</head></html>
						""", uri));
		this.mockMvc = MockMvcBuilders.standaloneSetup(new UrlPreviewController(service))
			.setControllerAdvice(new UrlPreviewExceptionHandler())
			.build();
	}

	@Test
	void returnsPreviewAsJson() throws Exception {
		this.mockMvc.perform(get("/api/preview").param("url", "https://example.com/article"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Example page"))
			.andExpect(jsonPath("$.description").value("A sample description"))
			.andExpect(jsonPath("$.url").value("https://example.com/article"));
	}

	@Test
	void returnsClearErrorForInvalidUrl() throws Exception {
		this.mockMvc.perform(get("/api/preview").param("url", "ftp://example.com/file"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Only HTTP and HTTPS URLs are supported."));
	}

	@Test
	void returnsClearErrorWhenUrlIsMissing() throws Exception {
		this.mockMvc.perform(get("/api/preview"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Enter a URL."));
	}
}
