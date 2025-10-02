package ceri.common.collect;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Map;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class NodeBehavior {
	private static final Node<?> NULL = Node.of();
	/**
	 * <pre>
	 * Node(a0,A00) {
	 *     Node(b0,B00)
	 *     Node(b1,B01) {
	 *         Node(c0,C00)
	 *         Node(null,C01)
	 *     }
	 *     Node(b2,null)
	 *     Node(null,B03) {
	 *         Node(null,null)
	 *         Node(c3,null)
	 *         Node(null,C04)
	 *     }
	 *     Node(b4,null) {
	 *         Node(,)
	 *     }
	 * }
	 * </pre>
	 */
	private static final Node<?> NODE = Node.<String>tree().startGroup("a0", "A00")
		.value("b0", "B00").startGroup("b1", "B01").value("c0", "C00").value(null, "C01")
		.closeGroup().value("b2", null).startGroup(null, "B03").value(null, null).value("c3", null)
		.value(null, "C04").closeGroup().startGroup("b4", null).value("", "").build().child(0);

	@Test
	public void shouldNotBreachEqualsContract() {
		Node<?> t = Node.of("name", 9);
		Node<?> eq0 = Node.of("name", 9);
		Node<?> eq1 = Node.builder(9).name("name").build();
		Node<?> ne0 = Node.of();
		Node<?> ne1 = Node.of("Name", 9);
		Node<?> ne2 = Node.of("name", 8);
		Node<?> ne3 = Node.builder(9).name("name").add(Node.builder(null)).build();
		Node<?> ne4 = Node.of(9);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineIfHasValue() {
		assertFalse(NULL.hasValue());
		assertFalse(Node.of(null).hasValue());
		assertTrue(Node.of("").hasValue());
	}

	@Test
	public void shouldDetermineIfNull() {
		assertTrue(NULL.isNull());
		assertTrue(Node.of(null).isNull());
		assertFalse(Node.of(1).isNull());
		assertFalse(Node.of("", null).isNull());
		Node<?> node = Node.builder(null).add(Node.builder(null)).build();
		assertFalse(node.isNull());
	}

	@Test
	public void shouldDetermineIfHasName() {
		assertFalse(NULL.isNamed());
		assertFalse(Node.of(null).isNamed());
		assertFalse(Node.of(1).isNamed());
		assertTrue(Node.of("", 1).isNamed());
	}

	@Test
	public void shouldParseValue() {
		assertEquals(NULL.parse().toBool(), null);
		assertEquals(Node.of(false).parse().toBool(), Boolean.FALSE);
		assertEquals(Node.of("true").parse().toBool(), Boolean.TRUE);
		assertEquals(Node.of(2).parse().toInt(), 2);
		assertEquals(Node.of("2").parse().toInt(), 2);
	}

	@Test
	public void shouldGetTypedValue() {
		assertEquals(Node.of(1).asType(Number.class).doubleValue(), 1.0);
		assertEquals(Node.of(1).asType(Long.class), null);
	}

	@Test
	public void shouldDetermineChildNamePaths() {
		assertOrdered(NODE.namedPaths(), "b0", "b1", "b1.c0", "b2", "b4");
		assertOrdered(NODE.child(3).namedPaths(), "c3");
	}

	@Test
	public void shouldFindChildByDottedPath() {
		assertNodeNameValue(NODE.find("b1.c0"), "c0", "C00");
		assertNodeNameValue(NODE.find("3.c3"), "c3", null);
		assertNodeNameValue(NODE.find("3.2"), null, "C04");
	}

	@Test
	public void shouldDetermineChildren() {
		assertOrdered(NODE.childNames(), "b0", "b1", "b2", "b4");
		assertTrue(NODE.hasChild("b2"));
		assertFalse(NODE.hasChild("3"));
		assertFalse(NODE.hasChild("b3"));
		Map<String, Node<?>> map = NODE.namedChildren();
		assertEquals(map.size(), 4);
		assertNodeNameValue(map.get("b0"), "b0", "B00");
		assertNodeNameValue(map.get("b1"), "b1", "B01");
		assertNodeNameValue(map.get("b2"), "b2", null);
		assertNodeNameValue(map.get("b4"), "b4", null);
	}

	@Test
	public void shouldAccessChildrenByNamedPath() {
		assertNodeNameValue(NODE.child(new String[0]), "a0", "A00");
		assertNodeNameValue(NODE.child("b0"), "b0", "B00");
		assertNodeNameValue(NODE.child("b1"), "b1", "B01");
		assertNodeNameValue(NODE.child("b1", "c0"), "c0", "C00");
		assertNodeNameValue(NODE.child("b1", "1"), null, "C01");
		assertNodeNameValue(NODE.child("b2"), "b2", null);
		assertNodeNameValue(NODE.child("3"), null, "B03");
		assertNodeNameValue(NODE.child("3", "0"), null, null);
		assertNodeNameValue(NODE.child("3", "c3"), "c3", null);
		assertNodeNameValue(NODE.child("3", "2"), null, "C04");
		assertNodeNameValue(NODE.child("b4"), "b4", null);
		assertNodeNameValue(NODE.child("b4", "0"), "", "");
	}

	@Test
	public void shouldAccessChildrenByArrayIndexPath() {
		assertNodeNameValue(NODE.child(new int[0]), "a0", "A00");
		assertNodeNameValue(NODE.child(0), "b0", "B00");
		assertNodeNameValue(NODE.child(1), "b1", "B01");
		assertNodeNameValue(NODE.child(1, 0), "c0", "C00");
		assertNodeNameValue(NODE.child(1, 1), null, "C01");
		assertNodeNameValue(NODE.child(2), "b2", null);
		assertNodeNameValue(NODE.child(3), null, "B03");
		assertNodeNameValue(NODE.child(3, 0), null, null);
		assertNodeNameValue(NODE.child(3, 1), "c3", null);
		assertNodeNameValue(NODE.child(3, 2), null, "C04");
		assertNodeNameValue(NODE.child(4), "b4", null);
		assertNodeNameValue(NODE.child(4, 0), "", "");
	}

	@Test
	public void shouldReturnNullForChildOutOfBounds() {
		assertEquals(NODE.child(-1), NULL);
		assertEquals(NODE.child(5), NULL);
		assertEquals(NODE.child(3, 3), NULL);
		assertEquals(NODE.child("5"), NULL);
		assertEquals(NODE.child("3", "3"), NULL);
		assertEquals(NODE.child("x"), NULL);
	}

	private static void assertNodeNameValue(Node<?> node, String name, Object value) {
		assertEquals(node.name, name);
		assertEquals(node.value, value);
	}

}
