package ceri.ent.web;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;
import ceri.common.function.Excepts.Supplier;

public class UriBuilder {
	private final URIBuilder builder;

	public static UriBuilder of(String uri) {
		return new UriBuilder(wrap(() -> new URIBuilder(uri)));
	}

	private UriBuilder(URIBuilder builder) {
		this.builder = builder;
	}

	public UriBuilder param(String name, Object value) {
		if (value != null) builder.addParameter(name, String.valueOf(value));
		return this;
	}

	public URI build() {
		return wrap(builder::build);
	}

	private static <T> T wrap(Supplier<URISyntaxException, T> supplier) {
		try {
			return supplier.get();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
