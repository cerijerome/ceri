package ceri.ent.json.adapter;

import java.time.LocalDate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

/**
 * Retrofit LocalDate to read year/day/month and read/write as ISO string.
 */
public class LocalDateAdapter {
	private static final String YEAR = "year";
	private static final String DAY = "day";
	private static final String MONTH = "month";
	public static final JsonSerialAdapter<LocalDate> INSTANCE =
		JsonSerialAdapter.of(LocalDate.class, (json, typeOfT, context) -> deserialize(json),
			(value, typeOfT, context) -> serialize(value, context));

	private static LocalDate deserialize(JsonElement json) {
		if (json.isJsonPrimitive()) return LocalDate.parse(json.getAsString());
		JsonObject obj = json.getAsJsonObject();
		int year = obj.get(YEAR).getAsInt();
		int day = obj.get(DAY).getAsInt();
		int month = obj.get(MONTH).getAsInt();
		return LocalDate.of(year, month, day);
	}

	private static JsonElement serialize(LocalDate value, JsonSerializationContext context) {
		return context.serialize(value.toString());
	}

}