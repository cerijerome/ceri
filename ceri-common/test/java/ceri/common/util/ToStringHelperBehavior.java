package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import java.util.Date;
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
	}
	
	@Test
	public void shouldNotShowEmptyValues() {
		String toString = ToStringHelper.create("Test").toString();
		assertFalse("String should not have values in parentheses", toString.contains("("));
	}
	
	@Test
	public void shouldNotShowEmptyValuesWithFieldsSet() {
		String toString = ToStringHelper.create("Test").add("Field").toString();
		assertFalse("String should not have values in parentheses", toString.contains("("));
	}
	
	@Test
	public void shouldUseClassNameFieldKeyIfSpecified() {
		String toString = ToStringHelper.create("Test").addByClass("Field").toString();
		assertThat(toString, is("Test[String=Field]"));
	}
	
	@Test
	public void shouldNotShowEmptyFields() {
		String toString = ToStringHelper.create("Test").toString();
		assertFalse("String should not have fields in square brackets", toString.contains("["));
	}
	
	@Test
	public void shouldNotShowEmptyFieldsWithValuesSet() {
		String toString = ToStringHelper.create("Test", "Value").toString();
		assertFalse("String should not have fields in square brackets", toString.contains("["));
	}
	
}
