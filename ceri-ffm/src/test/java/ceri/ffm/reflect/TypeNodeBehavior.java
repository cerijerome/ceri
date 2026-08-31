package ceri.ffm.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.Test;
import ceri.common.collect.Lists;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.ffm.reflect.Refine.Align;
import ceri.ffm.reflect.Refine.In;

public class TypeNodeBehavior {

	public static final class Types {
		public static final Field FIELD_LIST = Reflect.publicField(Types.class, "LIST");
		public static final Field FIELD_LIST_ANNO = Reflect.publicField(Types.class, "LIST_ANNO");
		public static final Generics.Token<List<?>> LIST_TOKEN = new Generics.Token<>() {};
		public static final List<?> LIST = Lists.of();
		@In
		public static final List<?> LIST_ANNO = Lists.of();
	}

	@Test
	public void testIsVoid() {
		Assert.no(TypeNode.isVoid(null));
		Assert.yes(TypeNode.isVoid(Generics.Typed.NULL));
		Assert.yes(TypeNode.isVoid(Generics.Typed.VOID));
		var token = new Generics.Token<List<?>>() {};
		Assert.no(TypeNode.isVoid(token.typed()));
		Assert.yes(TypeNode.isVoid(token.typed().type(0)));
		Assert.no(TypeNode.of(token).isVoid());
		Assert.yes(TypeNode.of(token).type().isVoid());
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var token = new Generics.Token<List<@Align(1) Integer>>() {};
		var t = TypeNode.of(token);
		var eq0 = TypeNode.of(new Generics.Token<List<@Align(1) Integer>>() {});
		var ne0 = TypeNode.VOID;
		var ne1 = TypeNode.of((Generics.Token<?>) null);
		var ne2 = TypeNode.of((Parameter) null);
		var ne3 = TypeNode.of(new Generics.Token<List<Integer>>() {});
		var ne4 = TypeNode.of(new Generics.Token<List<@Align(2) Integer>>() {});
		var ne5 = TypeNode.of(new Generics.Token<List<@Align(1) Long>>() {});
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldAllowCustomContext() {
		var t = TypeNode.of(List.class, Refine.custom().align(1L).context());
		var eq0 = TypeNode.of(List.class, Refine.custom().align(1L).context());
		var ne0 = TypeNode.of(List.class, Refine.custom().align(2L).context());
		var ne1 = TypeNode.of(null, Refine.custom().align(1L).context());
		var ne2 = TypeNode.of(List.class, Refine.custom().align(1L).constant().context());
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldNavigateArrays() {
		var node0 = TypeNode.of(new Generics.Token<List<int[][]>[]>() {});
		var node1 = node0.components();
		var node2 = node1.type();
		var node3 = node2.components();
		assertTyped(node1, new Generics.Token<List<int[][]>>() {});
		assertTyped(node2, new Generics.Token<int[][]>() {});
		assertTyped(node3, int.class);
		assertTyped(node3.components(), int.class);
	}

	@Test
	public void shouldStopNavigationAtLeaf() {
		var node = TypeNode.of(new Generics.Token<List<Integer>>() {});
		assertTyped(node.type(), Integer.class);
		assertTyped(node.type().type(), Integer.class);
	}

	private static void assertTyped(TypeNode node, Generics.Token<?> token) {
		Assert.equal(node.typed(), token.typed());
	}

	private static void assertTyped(TypeNode node, Type type) {
		Assert.equal(node.typed(), Generics.typed(type));
	}
}
