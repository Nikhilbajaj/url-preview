package com.example.url_preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class UrlPreviewServiceTests {

	@Test
	void prefersOpenGraphMetadataAndResolvesRelativeImages() throws Exception {
		UrlSafetyValidator validator = publicHostValidator();
		String html = """
				<html><head>
				<title>Document title</title>
				<meta property="og:title" content="Social title">
				<meta property="og:description" content="Social description">
				<meta property="og:image" content="/images/preview.png">
				</head></html>
				""";
		UrlPreviewService service = service(validator, html);

		UrlPreview preview = service.preview("https://example.com/article");

		assertEquals("Social title", preview.title());
		assertEquals("Social description", preview.description());
		assertEquals("https://example.com/images/preview.png", preview.imageUrl());
	}

	@Test
	void fallsBackToStandardHtmlMetadataWhenSocialTagsAreMissing() throws Exception {
		UrlPreviewService service = service(publicHostValidator(), """
				<html><head>
				<title>Document title</title>
				<meta name="description" content="Document description">
				</head></html>
				""");

		UrlPreview preview = service.preview("https://example.com/article");

		assertEquals("Document title", preview.title());
		assertEquals("Document description", preview.description());
		assertNull(preview.imageUrl());
	}

	@Test
	void omitsImagesWithUnsafeSchemes() throws Exception {
		UrlPreviewService service = service(publicHostValidator(), """
				<html><head>
				<title>Example</title>
				<meta property="og:image" content="javascript:alert(1)">
				</head></html>
				""");

		assertNull(service.preview("https://example.com").imageUrl());
	}

	@Test
	void doesNotFetchInvalidUrls() throws Exception {
		AtomicBoolean fetched = new AtomicBoolean();
		UrlPreviewService service = new UrlPreviewService(
				publicHostValidator(),
				uri -> {
					fetched.set(true);
					return new PageFetcher.FetchedPage("<title>unused</title>", uri);
				});

		assertThrows(UrlValidationException.class, () -> service.preview("file:///etc/passwd"));
		assertFalse(fetched.get());
	}

	@Test
	void propagatesUnavailablePageErrors() throws Exception {
		UrlPreviewService service = new UrlPreviewService(publicHostValidator(), uri -> {
			throw new PreviewFetchException("The page could not be reached.");
		});

		assertThrows(PreviewFetchException.class, () -> service.preview("https://example.com"));
	}

	private UrlPreviewService service(UrlSafetyValidator validator, String html) {
		return new UrlPreviewService(validator, uri -> new PageFetcher.FetchedPage(html, uri));
	}

	private UrlSafetyValidator publicHostValidator() throws Exception {
		InetAddress publicAddress = InetAddress.getByName("8.8.8.8");
		return new UrlSafetyValidator(host -> new InetAddress[] { publicAddress });
	}
}
