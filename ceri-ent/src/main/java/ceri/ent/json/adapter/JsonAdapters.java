package ceri.ent.json.adapter;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Function;

public class JsonAdapters {
	public static final JsonAdapter<LocalDateTime> LOCAL_DATE_TIME =
		ofString(LocalDateTime.class, LocalDateTime::parse);
	public static final JsonAdapter<ZoneId> ZONE_ID = ofString(ZoneId.class, ZoneId::of);

	private JsonAdapters() {}

	public static <T> JsonAdapter<T> ofString(Type type, Function<String, T> deserializer) {
		return ofString(type, deserializer, String::valueOf);
	}

	public static <T> JsonAdapter<T> ofString(Type type, Function<String, T> deserializer,
		Function<T, String> serializer) {
		return JsonAdapter.of(type, in -> deserializer.apply(in.nextString()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofInt(Type type, Function<Integer, T> deserializer,
		Function<T, Integer> serializer) {
		return JsonAdapter.of(type, in -> deserializer.apply(in.nextInt()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofLong(Type type, Function<Long, T> deserializer,
		Function<T, Long> serializer) {
		return JsonAdapter.of(type, in -> deserializer.apply(in.nextLong()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofDouble(Type type, Function<Double, T> deserializer,
		Function<T, Double> serializer) {
		return JsonAdapter.of(type, in -> deserializer.apply(in.nextDouble()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofBoolean(Type type, Function<Boolean, T> deserializer,
		Function<T, Boolean> serializer) {
		return JsonAdapter.of(type, in -> deserializer.apply(in.nextBoolean()),
			(out, t) -> out.value(serializer.apply(t)));
	}

}
