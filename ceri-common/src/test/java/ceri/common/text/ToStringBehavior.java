package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import ceri.common.reflect.ReflectUtil;

public class ToStringBehavior {

	private static record Rec(int i, String s, Map<String, Double> map) {}

	@Test
	public void shouldConvertRecordFormat() {
		assertEquals(ToString.forRecord(null), "null");
		var r = new Rec(123, "test = , 1, 2", new TreeMap<>(Map.of("1, 0", 1.0, "2, 0", 2.0)));
		assertString(ToString.forRecord(r), "%s(123,test = , 1, 2,{1, 0=1.0, 2, 0=2.0})",
			ReflectUtil.name(Rec.class));
	}

	@Test
	public void shouldUseClassNameIfSpecified() {
		String toString = ToString.forClass(new Date());
		assertEquals(toString, "Date");
	}

	@Test
	public void shouldShowValues() {
		String toString = ToString.ofName("Test", "Value1", "Value2").toString();
		assertEquals(toString, "Test(Value1,Value2)");
		toString = ToString.ofName("Test").values().values("v1").values("v2").toString();
		assertEquals(toString, "Test(v1,v2)");
	}

	@Test
	public void shouldShowDefaultFormattedDateValues() {
		Date date = new Date(0);
		String toString = ToString.ofName("Test", date, null).toString();
		Instant.ofEpochMilli(0);
		LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
		assertEquals(toString, "Test(" + dt + ",null)");
	}

	@Test
	public void shouldNotShowEmptyValuesOrFields() {
		String toString = ToString.ofName("Test").toString();
		assertEquals(toString, "Test");
	}

	@Test
	public void shouldNotShowEmptyValuesWithFieldsSet() {
		String toString = ToString.ofName("Test").fields("Field").toString();
		assertEquals(toString, "Test[Field]");
	}

	@Test
	public void shouldUseClassNameFieldKeyIfSpecified() {
		String toString = ToString.ofName("Test").fieldsByClass("Field") //
			.fieldsByClass((Object) null).toString();
		assertEquals(toString, "Test[String=Field,null]");
	}

	@Test
	public void shouldAddMapsOfChildren() {
		String toString = ToString.ofName("Test").children("aaa", "bbb")
			.childrens(Map.of("k0", "v0")).childrens((Map<String, String>) null).toString();
		assertEquals(toString, lines("Test {", "  aaa", "  bbb", "  k0: v0", "}"));
	}

	@Test
	public void shouldAddIndentedChildren() {
		String toString = ToString.ofName("Test").children("aaa", "bbb")
			.childrens(Arrays.asList("ccc", "ddd")).toString();
		assertEquals(toString, lines("Test {", "  aaa", "  bbb", "  ccc", "  ddd", "}"));
	}

	@Test
	public void shouldAddChildrenWithSpecifiedPrefix() {
		String toString =
			ToString.ofName("Test").childIndent("\t").children("a", "b", "c").toString();
		assertEquals(toString, lines("Test {", "\ta", "\tb", "\tc", "}"));
	}

	@Test
	public void shouldIndentChildrenOfChildren() {
		String child1 = ToString.ofName("Child1").children("a1", "b1").toString();
		String child2 = ToString.ofName("Child2").children("a2", "b2").toString();
		String toString = ToString.ofName("Test").children(child1, child2).toString();
		assertEquals(toString, lines("Test {", "  Child1 {", "    a1", "    b1", "  }",
			"  Child2 {", "    a2", "    b2", "  }", "}"));
	}

	@Test
	public void shouldAllowValuesFieldsAndChildren() {
		String toString =
			ToString.ofName("Test", "Value").fields("Field").children("Child").toString();
		assertEquals(toString, lines("Test(Value)[Field] {", "  Child", "}"));
	}

	private static String lines(String... lines) {
		return String.join(System.lineSeparator(), lines);
	}

}
