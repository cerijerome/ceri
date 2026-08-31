package ceri.ffm.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.array.Dimensions;
import ceri.common.data.Bytes;
import ceri.common.io.Direction;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.ffm.core.Layouts;
import ceri.ffm.reflect.Refine.Align;
import ceri.ffm.reflect.Refine.Chars;
import ceri.ffm.reflect.Refine.Dims;
import ceri.ffm.reflect.Refine.In;
import ceri.ffm.reflect.Refine.Order;
import ceri.ffm.reflect.Refine.Out;
import ceri.ffm.reflect.Refine.Packed;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.type.Primitive;

public class RefineBehavior {

	@Size(1)
	@Align(4)
	@Order(Bytes.Order.big)
	@Dims({ 1, 2, 3 })
	@Chars("US-ASCII")
	public static class C {
		public static final Field fIn = Reflect.publicField(C.class, "in");
		public static final Field fOut = Reflect.publicField(C.class, "out");
		public static final Field fInOut = Reflect.publicField(C.class, "inOut");
		@In
		public static final int in = 0;
		@Out
		public static final int out = 0;
		@In
		@Out
		public static final int inOut = 0;
	}

	@Size()
	@Dims()
	@Chars()
	public static class Defs {}

	@Align(1)
	@Packed
	public static class AlignOk {}

	@Align(2)
	@Packed
	public static class AlignBad {}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Refine.class);
	}

	@Test
	public void shouldProvideAnnotationDescriptor() {
		var s = Refine.context(C.class).toString();
		Assert.find(s, "%s\\$C\\{.*\\}", getClass());
		Assert.find(s, "align=4");
		Assert.find(s, "size=1");
		Assert.find(s, "order=BIG_ENDIAN");
		Assert.find(s, "dims=\\[1,2,3\\]");
		Assert.find(s, "chars=US\\-ASCII");
	}

	@Test
	public void shouldAllowManualCustomization() {
		Assert.equal(Refine.custom((AnnotatedElement) null).context().chars(),
			Charset.defaultCharset());
		Assert.equal(Refine.custom((Refine.Context) null).context().dims(), Dimensions.NONE);
		var context = Refine.custom(C.class).size(null).nul().constant(false).unsigned()
			.direction(Direction.in).context();
		Assert.equal(context.align(), 4L);
		Assert.equal(context.size(), 0);
		Assert.equal(context.nul(), true);
		Assert.equal(context.constant(), false);
		Assert.equal(context.unsigned(), true);
		Assert.equal(context.direction(), Direction.in);
		Testing.exerciseEquals(context, context);
		var copy = Refine.custom(context).context();
		Assert.equal(context, copy);
		Assert.equal(context.hashCode(), copy.hashCode());
		Assert.notEqual(context, Refine.custom(context).size(0).context());
	}

	@Test
	public void shouldCustomizeFromLayout() {
		var context = Refine.custom().copy(null).context();
		Assert.equal(context.align(), Layouts.ALIGN_NATURAL);
		Assert.equal(context.order(), ByteOrder.nativeOrder());
		context = Refine.custom().copy(Primitive.LONG.align(4).order(ByteOrder.BIG_ENDIAN).layout())
			.context();
		Assert.equal(context.align(), 4L);
		Assert.equal(context.order(), ByteOrder.BIG_ENDIAN);
		context = Refine.custom()
			.copy(Primitive.LONG.order(ByteOrder.BIG_ENDIAN).asArray(2).layout()).context();
		Assert.equal(context.align(), 8L);
		Assert.equal(context.order(), ByteOrder.nativeOrder()); // doesn't copy order
	}

	@Test
	public void shouldSpecifyDirection() {
		Assert.equal(Refine.custom().context().direction(), Direction.duplex);
		Assert.equal(Refine.custom().in().context().direction(), Direction.in);
		Assert.equal(Refine.custom().out().context().direction(), Direction.out);
	}

	@Test
	public void shouldApplyContextToLayout() {
		var context = Refine.custom().align(2L).order(ByteOrder.BIG_ENDIAN).context();
		Assert.equal(Refine.apply(context, null), null);
		Assert.equal(Refine.apply(null, Layouts.INT), Layouts.INT);
		Assert.equal(Refine.apply(context, Layouts.INT),
			Layouts.INT.withByteAlignment(2).withOrder(ByteOrder.BIG_ENDIAN));
	}

	@Test
	public void shouldFailMismatchedAlignment() {
		Assert.equal(Refine.context(AlignOk.class).align(), 1L);
		Assert.illegalArg(() -> Refine.context(AlignBad.class).align());
	}

	@Test
	public void shouldProvideDefaultSize() {
		Assert.equal(Refine.context(Defs.class).size(7), 7);
	}

	@Test
	public void shouldProvideDefaultDimensions() {
		var def = Dimensions.of(2, 4);
		Assert.equal(Refine.context(Defs.class).dims(def), def);
	}

	@Test
	public void shouldProvideDefaultCharset() {
		var def = StandardCharsets.UTF_16;
		Assert.equal(Refine.context(Defs.class).chars(def), def);
	}

	@Test
	public void shouldProvideDirection() {
		Assert.equal(Refine.context(C.fIn).direction(), Direction.in);
		Assert.equal(Refine.context(C.fOut).direction(), Direction.out);
		Assert.equal(Refine.context(C.fInOut).direction(), Direction.duplex);
	}

	@Test
	public void shouldFixDimensions() {
		var context = Refine.custom().dims(4, 8).context();
		Assert.equal(context.dims(0, true, 0), Dimensions.NONE);
		Assert.equal(context.dims(1, true, 0), Dimensions.of(4));
		Assert.equal(context.dims(2, true, 0), Dimensions.of(4, 8));
		Assert.equal(context.dims(3, true, 0), Dimensions.of(4, 8, Refine.NUL_MAX_DEF));
		Assert.equal(context.dims(3, true, 5), Dimensions.of(4, 8, 5));
		Assert.equal(context.dims(3, false, 5), Dimensions.of(4, 8, 0));
	}
}
