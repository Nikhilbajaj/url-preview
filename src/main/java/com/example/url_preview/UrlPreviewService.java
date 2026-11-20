package com.example.url_preview;

import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class UrlPreviewService {

	private final UrlSafetyValidator urlSafetyValidator;

	private final PageFetcher pageFetcher;

	public UrlPreviewService(UrlSafetyValidator urlSafetyValidator, PageFetcher pageFetcher) {
		this.urlSafetyValidator = urlSafetyValidator;
		this.pageFetcher = pageFetcher;
	}

	public UrlPreview preview(String input) {
		URI uri = this.urlSafetyValidator.validate(input);
		PageFetcher.FetchedPage page = this.pageFetcher.fetch(uri);
		Document document = Jsoup.parse(page.html(), page.finalUri().toString());

		String title = firstNonBlank(
				metaContent(document, "property", "og:title"),
				metaContent(document, "name", "twitter:title"),
				document.title(),
				page.finalUri().getHost());
		String description = firstNonBlank(
				metaContent(document, "property", "og:description"),
				metaContent(document, "name", "description"),
				metaContent(document, "name", "twitter:description"));
		String imageUrl = safeImageUrl(firstNonBlank(
				metaContent(document, "property", "og:image"),
				metaContent(document, "name", "twitter:image")), page.finalUri());

		return new UrlPreview(uri.toASCIIString(), title, description, imageUrl);
	}

	private String safeImageUrl(String image, URI pageUri) {
		if (image == null) {
			return null;
		}
		try {
			URI resolvedImage = pageUri.resolve(image);
			return this.urlSafetyValidator.validate(resolvedImage.toString()).toASCIIString();
		}
		catch (IllegalArgumentException | UrlValidationException exception) {
			return null;
		}
	}

	private String metaContent(Document document, String attribute, String value) {
		Element meta = document.selectFirst("meta[" + attribute + "=\"" + value + "\"]");
		return meta == null ? null : meta.attr("content");
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value.trim();
			}
		}
		return null;
	}
}
