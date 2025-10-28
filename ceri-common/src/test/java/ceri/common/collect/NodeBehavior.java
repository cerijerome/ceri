package ceri.common.collect;

import java.util.Map;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineIfHasValue() {
		Assert.no(NULL.hasValue());
		Assert.no(Node.of(null).hasValue());
		Assert.yes(Node.of("").hasValue());
	}

	@Test
	public void shouldDetermineIfNull() {
		Assert.yes(NULL.isNull());
		Assert.yes(Node.of(null).isNull());
		Assert.no(Node.of(1).isNull());
		Assert.no(Node.of("", null).isNull());
		Node<?> node = Node.builder(null).add(Node.builder(null)).build();
		Assert.no(node.isNull());
	}

	@Test
	public void shouldDetermineIfHasName() {
		Assert.no(NULL.isNamed());
		Assert.no(Node.of(null).isNamed());
		Assert.no(Node.of(1).isNamed());
		Assert.yes(Node.of("", 1).isNamed());
	}

	@Test
	public void shouldParseValue() {
		Assert.equal(NULL.parse().toBool(), null);
		Assert.equal(Node.of(false).parse().toBool(), Boolean.FALSE);
		Assert.equal(Node.of("true").parse().toBool(), Boolean.TRUE);
		Assert.equal(Node.of(2).parse().toInt(), 2);
		Assert.equal(Node.of("2").parse().toInt(), 2);
	}

	@Test
	public void shouldGetTypedValue() {
		Assert.equal(Node.of(1).asType(Number.class).doubleValue(), 1.0);
		Assert.equal(Node.of(1).asType(Long.class), null);
	}

	@Test
	public void shouldDetermineChildNamePaths() {
		Assert.ordered(NODE.namedPaths(), "b0", "b1", "b1.c0", "b2", "b4");
		Assert.ordered(NODE.child(3).namedPaths(), "c3");
	}

	@Test
	public void shouldFindChildByDottedPath() {
		assertNodeNameValue(NODE.find("b1.c0"), "c0", "C00");
		assertNodeNameValue(NODE.find("3.c3"), "c3", null);
		assertNodeNameValue(NODE.find("3.2"), null, "C04");
	}

	@Test
	public void shouldDetermineChildren() {
		Assert.ordered(NODE.childNames(), "b0", "b1", "b2", "b4");
		Assert.yes(NODE.hasChild("b2"));
		Assert.no(NODE.hasChild("3"));
		Assert.no(NODE.hasChild("b3"));
		Map<String, Node<?>> map = NODE.namedChildren();
		Assert.equal(map.size(), 4);
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
		Assert.equal(NODE.child(-1), NULL);
		Assert.equal(NODE.child(5), NULL);
		Assert.equal(NODE.child(3, 3), NULL);
		Assert.equal(NODE.child("5"), NULL);
		Assert.equal(NODE.child("3", "3"), NULL);
		Assert.equal(NODE.child("x"), NULL);
	}

	private static void assertNodeNameValue(Node<?> node, String name, Object value) {
		Assert.equal(node.name, name);
		Assert.equal(node.value, value);
	}

}
