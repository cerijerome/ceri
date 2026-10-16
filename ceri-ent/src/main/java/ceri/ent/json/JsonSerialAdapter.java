package ceri.ent.json;

import java.lang.reflect.Type;
import java.time.LocalDate;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import ceri.common.geom.Point2d;
import ceri.common.math.Matrix;

/**
 * Json adapter using JsonDeserializer and JsonSerializer.
 */
public class JsonSerialAdapter<T> implements Json.Registrant {
	private static final String YEAR = "year";
	private static final String DAY = "day";
	private static final String MONTH = "month";
	/** Retrofit local date to read 'year'/'day'/'month' and read/write as ISO string. */
	public static final JsonSerialAdapter<LocalDate> LOCAL_DATE =
		JsonSerialAdapter.of(LocalDate.class, (json, _, _) -> deserializeLocalDate(json),
			(value, _, context) -> serializeLocalDate(value, context));
	/** An adapter for a Point2d as 'x', 'y'. */
	public static final JsonSerialAdapter<Point2d> POINT2D =
		JsonSerialAdapter.of(Point2d.class, (json, _, context) -> deserializePoint2d(json, context),
			(value, _, context) -> serializePoint2d(value, context));
	/** An adapter for a matrix vector as double array. */
	public static final JsonSerialAdapter<Matrix> VECTOR =
		JsonSerialAdapter.of(Matrix.class, (json, _, context) -> deserializeVector(json, context),
			(value, _, context) -> serializeVector(value, context));
	private final Type type;
	private final JsonDeserializer<T> deserializer;
	private final JsonSerializer<T> serializer;

	public static <T> JsonSerialAdapter<T> deserializer(Type type,
		JsonDeserializer<T> deserializer) {
		return of(type, deserializer, null);
	}

	public static <T> JsonSerialAdapter<T> serializer(Type type, JsonSerializer<T> serializer) {
		return of(type, null, serializer);
	}

	public static <T> JsonSerialAdapter<T> of(Type type, JsonDeserializer<T> deserializer,
		JsonSerializer<T> serializer) {
		return new JsonSerialAdapter<>(type, deserializer, serializer);
	}

	private JsonSerialAdapter(Type type, JsonDeserializer<T> deserializer,
		JsonSerializer<T> serializer) {
		this.type = type;
		this.deserializer = deserializer;
		this.serializer = serializer;
	}

	@Override
	public GsonBuilder registerWith(GsonBuilder builder) {
		if (serializer != null) builder.registerTypeAdapter(type, serializer);
		if (deserializer != null) builder.registerTypeAdapter(type, deserializer);
		return builder;
	}

	// support

	private static LocalDate deserializeLocalDate(JsonElement json) {
		if (json.isJsonPrimitive()) return LocalDate.parse(json.getAsString());
		JsonObject obj = json.getAsJsonObject();
		int year = obj.get(YEAR).getAsInt();
		int day = obj.get(DAY).getAsInt();
		int month = obj.get(MONTH).getAsInt();
		return LocalDate.of(year, month, day);
	}

	private static JsonElement serializeLocalDate(LocalDate value,
		JsonSerializationContext context) {
		return context.serialize(value.toString());
	}

	private static Point2d deserializePoint2d(JsonElement json,
		JsonDeserializationContext context) {
		double[] values = context.deserialize(json, double[].class);
		if (values == null || values.length != 2)
			throw new JsonParseException("Expected [x, y]: " + json);
		return Point2d.of(values[0], values[1]);
	}

	private static JsonElement serializePoint2d(Point2d value, JsonSerializationContext context) {
		return context.serialize(new double[] { value.x(), value.y() });
	}

	private static Matrix deserializeVector(JsonElement json, JsonDeserializationContext context) {
		double[] values = context.deserialize(json, double[].class);
		return Matrix.vector(values);
	}

	private static JsonElement serializeVector(Matrix value, JsonSerializationContext context) {
		return context.serialize(value.values());
	}
}
