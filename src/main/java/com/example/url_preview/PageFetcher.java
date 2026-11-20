package com.example.url_preview;

import java.net.URI;

public interface PageFetcher {

	FetchedPage fetch(URI initialUri);

	record FetchedPage(String html, URI finalUri) {
	}
}
