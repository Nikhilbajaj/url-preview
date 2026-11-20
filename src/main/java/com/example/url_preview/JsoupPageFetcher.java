package com.example.url_preview;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class JsoupPageFetcher implements PageFetcher {

	private static final int MAX_REDIRECTS = 5;

	private static final int MAX_RESPONSE_BYTES = 1_000_000;

	private static final int TIMEOUT_MILLIS = 5_000;

	private final UrlSafetyValidator urlSafetyValidator;

	public JsoupPageFetcher(UrlSafetyValidator urlSafetyValidator) {
		this.urlSafetyValidator = urlSafetyValidator;
	}

	@Override
	public FetchedPage fetch(URI initialUri) {
		URI currentUri = initialUri;
		for (int redirectCount = 0; ; redirectCount++) {
			if (redirectCount > 0) {
				try {
					currentUri = this.urlSafetyValidator.validate(currentUri.toString());
				}
				catch (UrlValidationException exception) {
					throw new PreviewFetchException("The page redirected to an unsafe or unsupported address.");
				}
			}

			Connection.Response response;
			try {
				response = Jsoup.connect(currentUri.toASCIIString())
					.userAgent("UrlPreview/1.0")
					.timeout(TIMEOUT_MILLIS)
					.maxBodySize(MAX_RESPONSE_BYTES)
					.followRedirects(false)
					.ignoreHttpErrors(true)
					.ignoreContentType(true)
					.execute();
			}
			catch (IOException exception) {
				throw new PreviewFetchException("The page could not be reached.", exception);
			}

			int statusCode = response.statusCode();
			if (statusCode >= 300 && statusCode < 400) {
				if (redirectCount == MAX_REDIRECTS) {
					throw new PreviewFetchException("The page redirected too many times.");
				}
				String location = response.header("Location");
				if (location == null || location.isBlank()) {
					throw new PreviewFetchException("The page returned an invalid redirect.");
				}
				try {
					currentUri = currentUri.resolve(new URI(location));
				}
				catch (IllegalArgumentException | URISyntaxException exception) {
					throw new PreviewFetchException("The page returned an invalid redirect.", exception);
				}
				continue;
			}

			if (statusCode < 200 || statusCode >= 300) {
				throw new PreviewFetchException("The page returned an unsuccessful response.");
			}

			String contentType = response.contentType();
			if (contentType == null
					|| !(contentType.toLowerCase(Locale.ROOT).contains("text/html")
							|| contentType.toLowerCase(Locale.ROOT).contains("application/xhtml+xml"))) {
				throw new PreviewFetchException("The URL does not point to an HTML page.");
			}
			return new FetchedPage(response.body(), currentUri);
		}
	}
}
