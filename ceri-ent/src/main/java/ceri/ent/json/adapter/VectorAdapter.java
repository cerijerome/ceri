package ceri.ent.json.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import ceri.common.math.Vector;

public class VectorAdapter {
	public static final JsonSerialAdapter<Vector> INSTANCE =
		JsonSerialAdapter.of(Vector.class, (json, typeOfT, context) -> deserialize(json, context),
			(value, typeOfT, context) -> serialize(value, context));

	private static Vector deserialize(JsonElement json, JsonDeserializationContext context) {
		double[] values = context.deserialize(json, double[].class);
		return Vector.of(values);
	}

	private static JsonElement serialize(Vector value, JsonSerializationContext context) {
		return context.serialize(value.values());
	}

}
