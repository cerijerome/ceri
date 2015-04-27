package ceri.ent.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class JsonFileStore<T> implements PersistentStore<T> {
	private static final Logger logger = LogManager.getLogger();
	private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting()
		.create();
	private final Type type;
	private final File file;

	private JsonFileStore(TypeToken<T> typeToken, File file) {
		type = typeToken.getType();
		this.file = file;
	}

	public static <T> JsonFileStore<T> create(TypeToken<T> typeToken, File file) {
		return new JsonFileStore<>(typeToken, file);
	}

	@Override
	public T load() throws IOException {
		try {
			logger.info("Loading from {}", file);
			String json = IoUtil.getContentString(file);
			return gson.fromJson(json, type);
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
		String json = gson.toJson(t, type);
		IoUtil.setContentString(file, json);
	}

}
