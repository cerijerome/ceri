package ceri.common.test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.io.IoUtil;

/**
 * Used to modify source code for operations more complicated than search/replace.
 * File contents are not replace - instead use print() to check results, then manually 
 * copy to the file.
 */
public class SourceManipulator {
	private String src;
	
	public static SourceManipulator from(String src) {
		return new SourceManipulator(src);
	}
	
	public static SourceManipulator from(Class<?> cls) {
		String fileName = "./src/main/java/" + fileName(cls);
		return from(new File(fileName));
	}
	
	public static SourceManipulator from(File file) {
		try {
			return new SourceManipulator(IoUtil.getContentString(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String fileName(Class<?> cls) {
		return cls.getCanonicalName().replaceAll("\\.", "/") + ".java";
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
