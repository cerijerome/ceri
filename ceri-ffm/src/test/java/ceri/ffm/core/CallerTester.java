package ceri.ffm.core;

import java.util.Map;
import ceri.common.function.Functions;
import ceri.common.io.Buffers;
import ceri.common.text.Joiner;
import ceri.common.text.Transformer;
import ceri.ffm.reflect.Refine.Nul;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.IntType.ssize_t;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.Struct;
import ceri.ffm.type.Union;

public class CallerTester {

	@Fields({ "i", "s" })
	public static class union extends Union<union> {
		public static final Union.Supporter<union> $ = Union.support(union.class);
		public int i = -1;
		public @Size(3) String s = "abc";
	}

	@Fields({ "i", "s" })
	public static class struct extends Struct<struct> {
		public static final Struct.Supporter<struct> $ = Struct.support(struct.class);
		public @Nul int[][] i = { { -1, 1, -2, 2, 0 }, {} };
		public @Size(3) String s = "ABC";
	}

	public static void main(String[] args) {
		Functions.Consumer<String> con = s -> System.out.println(s);
		System.out.println(con);

		Object[] values = { //
			"abc\nde\0", new int[] { 1, -1, 0, 2, -2 }, //
			Map.of("a", 1, "b", 2), ssize_t.$.ofAll(1, -1, 0, 2, -2), //
			new union(), new struct(), //
			Buffers.SHORT.of(-1, 1, 0), //
			Primitive.BOOL.allocAll(true, false, true), //
			Pointer.of(Pointer.ofByte(-1)), //
		};
		print(Caller.Transform.FULL, values);
		print(Caller.Transform.COMPACT, values);
	}

	private static void print(Transformer transformer, Object[] values) {
		FfmTesting.title(transformer.toString());
		System.out.println(Joiner.ARRAY.joinAll(transformer, values));
	}
}
