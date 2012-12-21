package ceri.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import ceri.common.util.StringUtil.Align;

public class Roms {
	private static final File listRoot = new File("/Users/cjerome/_LM-SJC-00712379/roms/");
	private static final File outRoot = new File(listRoot, "output/");
	
	private static enum Type {
		files, themes, videos, wheels
	}
	
	private static final String[] listFiles = {
		"mature_all",
		"mature_noclones",
		"mature_playable",
		"mature_noclones-playable",
		"nonmature_all",
		"nonmature_noclones",
		"nonmature_playable",
		"nonmature_noclones-playable",
	};

	public static void main(String[] args) throws IOException {
		Map<Type, Set<String>> typeLists = readAll(listRoot);		
		for (String listFile : listFiles) {
			File masterFile = new File(listRoot, listFile + ".txt");
			Set<String> masterList = readLines(masterFile);
			File file = new File(outRoot, "out_" + listFile + ".txt");
			try (PrintStream out = new PrintStream(file)) {
				write(out, masterList, typeLists);
				out.flush();
			}
		}
	}
	
	private static void write(PrintStream out, Set<String> masterList, Map<Type,
		Set<String>> typeLists) {
		out.print(StringUtil.pad("Name", 12, " ", Align.LEFT));
		for (Type type : Type.values())
			out.print(StringUtil.pad(type.name(), 8, " ", Align.LEFT));
		out.println();
		String buffer = "       ";
		for (String name : masterList) {
			out.print(StringUtil.pad(name, 12, " ", Align.LEFT));
			for (Type type : Type.values()) {
				Set<String> typeList = typeLists.get(type);
				String s = typeList.contains(name) ? "x" : " ";
				out.print(s);
				out.print(buffer);
			}
			out.println();
		}
	}

	private static Map<Type, Set<String>> readAll(File root) throws IOException {
		Map<Type, Set<String>> map = new HashMap<>();
		for (Type type : Type.values()) {
			Set<String> lines = readLines(new File(root, "rom" + type.name() + ".txt"));
			map.put(type, lines);
		}
		return map;
	}
	
	private static Set<String> readLines(File file) throws IOException {
		Set<String> lines = new TreeSet<>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.contains(".")) continue;
				lines.add(line);
			}
			return lines;
		}
	}
	
}
