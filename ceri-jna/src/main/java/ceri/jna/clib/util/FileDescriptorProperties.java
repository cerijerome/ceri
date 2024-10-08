package ceri.jna.clib.util;

import java.util.List;
import ceri.common.property.TypedProperties;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;

public class FileDescriptorProperties extends TypedProperties.Ref {
	private static final String PATH_KEY = "path";
	private static final String MODE_KEY = "mode";
	private static final String OPEN_FLAGS_KEY = "open.flags";

	public FileDescriptorProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public CFileDescriptor.Opener opener() {
		var path = parse(PATH_KEY).get();
		var mode = parse(MODE_KEY).asInt().to(Mode::of, Mode.NONE);
		var openFlags =
			parse(OPEN_FLAGS_KEY).split().asEnums(FileDescriptor.Open.class).get(List.of());
		return new CFileDescriptor.Opener(path, mode, openFlags);
	}
}
