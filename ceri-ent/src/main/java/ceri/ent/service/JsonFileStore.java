package ceri.ent.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonSyntaxException;
import ceri.ent.json.JsonCoder;

public class JsonFileStore<T> implements PersistentStore<T> {
	private static final Logger logger = LogManager.getLogger();
	private final JsonCoder<T> coder;
	private final Path file;

	private JsonFileStore(JsonCoder<T> coder, Path file) {
		this.file = file;
		this.coder = coder;
	}

	public static <T> JsonFileStore<T> create(JsonCoder<T> coder, Path file) {
		return new JsonFileStore<>(coder, file);
	}

	@Override
	public T load() throws IOException {
		try {
			logger.info("Loading from {}", file);
			String json = Files.readString(file);
			return coder.fromJson(json);
		} catch (FileNotFoundException e) {
			return null;
		} catch (JsonSyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void save(T t) throws IOException {
		logger.info("Saving to {}", file);
		String json = coder.toJson(t);
		Files.writeString(file, json);
	}

}
