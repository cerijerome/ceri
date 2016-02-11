package ceri.common.test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.io.IoUtil;
import ceri.common.reflect.Caller;
import ceri.common.reflect.ReflectUtil;

/**
 * Used to modify source code for operations more complicated than search/replace.
 * File contents are not replaced - instead use print() to check results, then manually 
 * copy to the file.
 */
public class SourceManipulator {
	private static final String MAIN_DIR = "./src/main/java/";
	private static final String TEST_DIR = "./src/test/java/";
	private String src;
	
	public static SourceManipulator from(String src) {
		return new SourceManipulator(src);
	}
	
	public static SourceManipulator fromCaller() {
		Caller caller = ReflectUtil.previousCaller(1);
		String fileName = classToFilename(caller.fullCls, false);
		return from(new File(fileName));
	}
	
	public static SourceManipulator fromTestCaller() {
		Caller caller = ReflectUtil.previousCaller(1);
		String fileName = classToFilename(caller.fullCls, true);
		return from(new File(fileName));
	}
	
	public static SourceManipulator from(Class<?> cls) {
		return from(new File(classToFilename(cls, false)));
	}
	
	public static SourceManipulator fromTest(Class<?> cls) {
		return from(new File(classToFilename(cls, true)));
	}
	
	public static SourceManipulator from(File file) {
		try {
			return new SourceManipulator(IoUtil.getContentString(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String classToFilename(String fullName, boolean test) {
		return (test ? TEST_DIR : MAIN_DIR) + fullName.replaceAll("\\.", "/") + ".java";
	}
	
	public static String classToFilename(Class<?> cls, boolean test) {
		return classToFilename(cls.getCanonicalName(), test);
	}
	
	private SourceManipulator(String src) {
		this.src = src;
	}
	
	public Matcher matcher(String pattern) {
		return Pattern.compile(pattern).matcher(src);
	}
	
	public void replaceAll(String regex, String replacement) {
		src = src.replaceAll(regex, replacement);
	}
	
	public void replaceIdentifier(String from, String to) {
		src = src.replaceAll("\\b\\Q" + from + "\\E\\b", to);
	}
	
	public void print() {
		System.out.println(src);
	}
	
	public static void main(String[] args) {
		SourceManipulator sm = from(SourceManipulator.class);
		sm.print();
	}
	
}
