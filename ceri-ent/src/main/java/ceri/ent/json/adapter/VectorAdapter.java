package ceri.ent.json.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import ceri.common.math.Matrix;

public class VectorAdapter {
	public static final JsonSerialAdapter<Matrix> INSTANCE =
		JsonSerialAdapter.of(Matrix.class, (json, typeOfT, context) -> deserialize(json, context),
			(value, typeOfT, context) -> serialize(value, context));

	private static Matrix deserialize(JsonElement json, JsonDeserializationContext context) {
		double[] values = context.deserialize(json, double[].class);
		return Matrix.vector(values);
	}

	private static JsonElement serialize(Matrix value, JsonSerializationContext context) {
		return context.serialize(value.values());
	}

}
