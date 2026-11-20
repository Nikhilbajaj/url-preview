package com.example.url_preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;
import java.net.URI;

import org.junit.jupiter.api.Test;

class UrlSafetyValidatorTests {

	@Test
	void acceptsPublicHttpAndHttpsUrls() throws Exception {
		UrlSafetyValidator validator = validator("8.8.8.8");

		assertEquals(URI.create("https://example.com/path"), validator.validate(" https://example.com/path "));
	}

	@Test
	void rejectsUnsupportedSchemesAndPorts() throws Exception {
		UrlSafetyValidator validator = validator("8.8.8.8");

		assertThrows(UrlValidationException.class, () -> validator.validate("file:///etc/passwd"));
		assertThrows(UrlValidationException.class, () -> validator.validate("http://example.com:8080"));
	}

	@Test
	void rejectsHostsThatResolveToPrivateAddresses() throws Exception {
		UrlSafetyValidator validator = validator("93.184.216.34", "127.0.0.1");

		assertThrows(UrlValidationException.class, () -> validator.validate("https://example.com"));
	}

	private UrlSafetyValidator validator(String... addresses) throws Exception {
		InetAddress[] resolved = new InetAddress[addresses.length];
		for (int index = 0; index < addresses.length; index++) {
			resolved[index] = InetAddress.getByName(addresses[index]);
		}
		return new UrlSafetyValidator(host -> resolved);
	}
}
