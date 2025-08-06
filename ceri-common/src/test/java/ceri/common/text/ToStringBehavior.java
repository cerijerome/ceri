package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertLines;
import static ceri.common.test.AssertUtil.assertString;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import ceri.common.reflect.Reflect;

public class ToStringBehavior {

	private static record Rec(int i, String s, Map<String, Double> map) {}

	@Test
	public void testDeep() {
		assertEquals(ToString.deep(null), "null");
		assertEquals(ToString.deep("test"), "test");
		assertEquals(ToString.deep(new boolean[] { true, false }), "[true, false]");
		assertEquals(ToString.deep(new byte[] { 1, 2 }), "[1, 2]");
		assertEquals(ToString.deep(new char[] { '1', '2' }), "[1, 2]");
		assertEquals(ToString.deep(new short[] { 1, 2 }), "[1, 2]");
		assertEquals(ToString.deep(new int[] { 1, 2 }), "[1, 2]");
		assertEquals(ToString.deep(new long[] { 1, 2 }), "[1, 2]");
		assertEquals(ToString.deep(new float[] { 1, 2 }), "[1.0, 2.0]");
		assertEquals(ToString.deep(new double[] { 1, 2 }), "[1.0, 2.0]");
		assertEquals(ToString.deep(new String[] { "1", "2" }), "[1, 2]");
	}

	@Test
	public void testHex() {
		assertEquals(ToString.hex(Byte.MIN_VALUE), "0x80");
		assertEquals(ToString.hex(Short.MIN_VALUE), "0x8000");
		assertEquals(ToString.hex(Integer.MIN_VALUE), "0x80000000");
		assertEquals(ToString.hex(Long.MIN_VALUE), "0x8000000000000000");
	}

	@Test
	public void shouldConvertRecordFormat() {
		assertEquals(ToString.forRecord(null), "null");
		var r = new Rec(123, "test = , 1, 2", new TreeMap<>(Map.of("1, 0", 1.0, "2, 0", 2.0)));
		assertString(ToString.forRecord(r), "%s(123,test = , 1, 2,{1, 0=1.0, 2, 0=2.0})",
			Reflect.name(Rec.class));
	}

	@Test
	public void shouldUseClassNameIfSpecified() {
		var s = ToString.forClass(new Date());
		assertEquals(s, "Date");
	}

	@Test
	public void shouldShowValues() {
		var s = ToString.ofName("Test", "Value1", "Value2").toString();
		assertEquals(s, "Test(Value1,Value2)");
		s = ToString.ofName("Test").values().values("v1").values("v2").toString();
		assertEquals(s, "Test(v1,v2)");
	}

	@Test
	public void shouldShowDefaultFormattedDateValues() {
		var d = new Date(0);
		var s = ToString.ofName("Test", d, null).toString();
		Instant.ofEpochMilli(0);
		var dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
		assertEquals(s, "Test(" + dt + ",null)");
	}

	@Test
	public void shouldNotShowEmptyValuesOrFields() {
		var s = ToString.ofName("Test").toString();
		assertEquals(s, "Test");
	}

	@Test
	public void shouldNotShowEmptyValuesWithFieldsSet() {
		var s = ToString.ofName("Test").fields("Field").toString();
		assertEquals(s, "Test[Field]");
	}

	@Test
	public void shouldUseClassNameFieldKeyIfSpecified() {
		var s =
			ToString.ofName("Test").fieldsByClass("Field").fieldsByClass((Object) null).toString();
		assertEquals(s, "Test[String=Field,null]");
	}

	@Test
	public void shouldAddChild() {
		var s = ToString.ofName("Test").child(null).child("Child").toString();
		assertLines(s, "Test {", "  Child", "}");
		s = ToString.ofName("Test").childs(null).childs(List.of("Child0", "Child1")).toString();
		assertLines(s, "Test {", "  [Child0, Child1]", "}");
	}

	@Test
	public void shouldAddMapsOfChildren() {
		var s = ToString.ofName("Test").children("aaa", "bbb").childrens(Map.of("k0", "v0"))
			.childrens((Map<String, String>) null).toString();
		assertLines(s, "Test {", "  aaa", "  bbb", "  k0: v0", "}");
	}

	@Test
	public void shouldAddIndentedChildren() {
		var s = ToString.ofName("Test").children("aaa", "bbb")
			.childrens(Arrays.asList("ccc", "ddd")).toString();
		assertLines(s, "Test {", "  aaa", "  bbb", "  ccc", "  ddd", "}");
	}

	@Test
	public void shouldAddChildrenWithSpecifiedPrefix() {
		var s = ToString.ofName("Test").childIndent("\t").children("a", "b", "c").toString();
		assertLines(s, "Test {", "\ta", "\tb", "\tc", "}");
	}

	@Test
	public void shouldIndentChildrenOfChildren() {
		var child1 = ToString.ofName("Child1").children("a1", "b1").toString();
		var child2 = ToString.ofName("Child2").children("a2", "b2").toString();
		var s = ToString.ofName("Test").children(child1, child2).toString();
		assertLines(s, "Test {", "  Child1 {", "    a1", "    b1", "  }", "  Child2 {", "    a2",
			"    b2", "  }", "}");
	}

	@Test
	public void shouldAllowValuesFieldsAndChildren() {
		var s = ToString.ofName("Test", "Value").fields("Field").children("Child").toString();
		assertLines(s, "Test(Value)[Field] {", "  Child", "}");
	}
}
