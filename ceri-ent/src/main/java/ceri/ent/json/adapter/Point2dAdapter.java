package ceri.ent.json.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import ceri.common.geom.Point2d;

public class Point2dAdapter {
	public static final JsonSerialAdapter<Point2d> INSTANCE =
		JsonSerialAdapter.of(Point2d.class, (json, _, context) -> deserialize(json, context),
			(value, _, context) -> serialize(value, context));

	private static Point2d deserialize(JsonElement json, JsonDeserializationContext context) {
		double[] values = context.deserialize(json, double[].class);
		if (values == null || values.length != 2)
			throw new JsonParseException("Expected [x, y]: " + json);
		return Point2d.of(values[0], values[1]);
	}

	private static JsonElement serialize(Point2d value, JsonSerializationContext context) {
		return context.serialize(new double[] { value.x(), value.y() });
	}
}