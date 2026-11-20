package com.example.url_preview;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class UrlSafetyValidator {

	private final HostResolver hostResolver;

	public UrlSafetyValidator() {
		this(InetAddress::getAllByName);
	}

	UrlSafetyValidator(HostResolver hostResolver) {
		this.hostResolver = hostResolver;
	}

	public URI validate(String input) {
		if (input == null || input.isBlank()) {
			throw new UrlValidationException("Enter a URL.");
		}

		URI uri;
		try {
			uri = new URI(input.trim()).normalize();
		}
		catch (URISyntaxException exception) {
			throw new UrlValidationException("Enter a valid URL.");
		}

		String scheme = uri.getScheme();
		String host = uri.getHost();
		if (!uri.isAbsolute() || uri.isOpaque() || scheme == null || host == null) {
			throw new UrlValidationException("Enter an absolute URL with a valid host.");
		}

		scheme = scheme.toLowerCase(Locale.ROOT);
		if (!scheme.equals("http") && !scheme.equals("https")) {
			throw new UrlValidationException("Only HTTP and HTTPS URLs are supported.");
		}
		if (uri.getRawUserInfo() != null) {
			throw new UrlValidationException("URLs containing user information are not supported.");
		}

		int port = uri.getPort();
		if (port == 0 || port > 65535
				|| (port != -1 && !(scheme.equals("http") && port == 80)
						&& !(scheme.equals("https") && port == 443))) {
			throw new UrlValidationException("Only the standard HTTP and HTTPS ports are supported.");
		}

		validatePublicHost(host);
		return uri;
	}

	private void validatePublicHost(String host) {
		InetAddress[] addresses;
		try {
			addresses = this.hostResolver.resolve(host);
		}
		catch (UnknownHostException exception) {
			throw new UrlValidationException("The URL host could not be resolved.");
		}

		if (addresses.length == 0) {
			throw new UrlValidationException("The URL host could not be resolved.");
		}
		for (InetAddress address : addresses) {
			if (!isPublicAddress(address)) {
				throw new UrlValidationException("URLs must resolve to a public internet address.");
			}
		}
	}

	private boolean isPublicAddress(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return false;
		}

		byte[] bytes = address.getAddress();
		if (bytes.length == 4) {
			int first = Byte.toUnsignedInt(bytes[0]);
			int second = Byte.toUnsignedInt(bytes[1]);
			int third = Byte.toUnsignedInt(bytes[2]);
			return first != 0 && first < 224
					&& !(first == 100 && second >= 64 && second <= 127)
					&& !(first == 192 && (second == 0 && (third == 0 || third == 2)
							|| second == 88 && third == 99 || second == 168))
					&& !(first == 198 && (second == 18 || second == 19 || second == 51 && third == 100))
					&& !(first == 203 && second == 0 && third == 113);
		}

		if (address instanceof Inet6Address) {
			int first = Byte.toUnsignedInt(bytes[0]);
			return first >= 0x20 && first <= 0x3f
					&& !(first == 0x20 && Byte.toUnsignedInt(bytes[1]) == 0x01
							&& Byte.toUnsignedInt(bytes[2]) == 0x0d
							&& Byte.toUnsignedInt(bytes[3]) == 0xb8)
					&& !(first == 0x20 && Byte.toUnsignedInt(bytes[1]) == 0x02);
		}
		return false;
	}

	@FunctionalInterface
	interface HostResolver {
		InetAddress[] resolve(String host) throws UnknownHostException;
	}
}
