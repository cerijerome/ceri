package ceri.common.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.junit.Test;

public class ToStringHelperBehavior {

	@Test
	public void shouldUseClassNameIfSpecified() {
		String toString = ToStringHelper.createByClass(new Date()).toString();
		assertThat(toString, is("Date"));
	}

	@Test
	public void shouldShowValues() {
		String toString = ToStringHelper.create("Test", "Value1", "Value2").toString();
		assertThat(toString, is("Test(Value1,Value2)"));
		toString = ToStringHelper.create("Test").values().values("v1").values("v2").toString();
		assertThat(toString, is("Test(v1,v2)"));
	}

	@Test
	public void shouldShowDefaultFormattedDateValues() {
		Date date = new Date(0);
		String toString = ToStringHelper.create("Test", date, (Date) null).toString();
		Instant.ofEpochMilli(0);
		LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
		assertThat(toString, is("Test(" + dt + ",null)"));
	}

	@Test
	public void shouldNotShowEmptyValuesOrFields() {
		String toString = ToStringHelper.create("Test").toString();
		assertThat(toString, is("Test"));
	}

	@Test
	public void shouldNotShowEmptyValuesWithFieldsSet() {
		String toString = ToStringHelper.create("Test").fields("Field").toString();
		assertThat(toString, is("Test[Field]"));
	}

	@Test
	public void shouldUseClassNameFieldKeyIfSpecified() {
		String toString = ToStringHelper.create("Test").fieldsByClass("Field") //
			.fieldsByClass((Object) null).toString();
		assertThat(toString, is("Test[String=Field,null]"));
	}

	@Test
	public void shouldAddMapsOfChildren() {
		String toString = ToStringHelper.create("Test").children("aaa", "bbb")
			.childrens(Map.of("k0", "v0")).childrens((Map<String, String>) null).toString();
		assertThat(toString, is(lines("Test {", "  aaa", "  bbb", "  k0: v0", "}")));
	}

	@Test
	public void shouldAddIndentedChildren() {
		String toString = ToStringHelper.create("Test").children("aaa", "bbb")
			.childrens(Arrays.asList("ccc", "ddd")).toString();
		assertThat(toString, is(lines("Test {", "  aaa", "  bbb", "  ccc", "  ddd", "}")));
	}

	@Test
	public void shouldAddChildrenWithSpecifiedPrefix() {
		String toString =
			ToStringHelper.create("Test").childIndent("\t").children("a", "b", "c").toString();
		assertThat(toString, is(lines("Test {", "\ta", "\tb", "\tc", "}")));
	}

	@Test
	public void shouldIndentChildrenOfChildren() {
		String child1 = ToStringHelper.create("Child1").children("a1", "b1").toString();
		String child2 = ToStringHelper.create("Child2").children("a2", "b2").toString();
		String toString = ToStringHelper.create("Test").children(child1, child2).toString();
		assertThat(toString, is(lines("Test {", "  Child1 {", "    a1", "    b1", "  }",
			"  Child2 {", "    a2", "    b2", "  }", "}")));
	}

	@Test
	public void shouldAllowValuesFieldsAndChildren() {
		String toString =
			ToStringHelper.create("Test", "Value").fields("Field").children("Child").toString();
		assertThat(toString, is(lines("Test(Value)[Field] {", "  Child", "}")));
	}

	private static String lines(String... lines) {
		return StringUtil.toString("", "", System.lineSeparator(), Arrays.asList(lines));
	}

}
