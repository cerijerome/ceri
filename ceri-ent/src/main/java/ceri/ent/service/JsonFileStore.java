package ceri.ent.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonSyntaxException;
import ceri.common.io.IoUtil;
import ceri.ent.json.JsonCoder;

public class JsonFileStore<T> implements PersistentStore<T> {
	private static final Logger logger = LogManager.getLogger();
	private final JsonCoder<T> coder;
	private final File file;

	private JsonFileStore(JsonCoder<T> coder, File file) {
		this.file = file;
		this.coder = coder;
	}

	public static <T> JsonFileStore<T> create(JsonCoder<T> coder, File file) {
		return new JsonFileStore<>(coder, file);
	}

	@Override
	public T load() throws IOException {
		try {
			logger.info("Loading from {}", file);
			String json = IoUtil.getContentString(file);
			return coder.fromJson(json);
		} catch (FileNotFoundException e) {
			return null;
		} catch (JsonSyntaxException e) {
			logger.catching(e);
			return null;
		}
	}

	@Override
	public void save(T t) throws IOException {
		logger.info("Saving to {}", file);
		String json = coder.toJson(t);
		IoUtil.setContentString(file, json);
	}

}
