package ceri.jna.clib.util;

import static ceri.common.function.FunctionUtil.safeApply;
import java.util.List;
import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.FileDescriptor;
import ceri.jna.clib.Mode;

public class FileDescriptorProperties extends Ref<TypedProperties> {
	private static final String PATH_KEY = "path";
	private static final String MODE_KEY = "mode";
	private static final String OPEN_FLAGS_KEY = "open.flags";

	public FileDescriptorProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
	}

	public CFileDescriptor.Opener opener() {
		return new CFileDescriptor.Opener(path(), mode(), openFlags());
	}

	private String path() {
		return ref.value(PATH_KEY);
	}

	private Mode mode() {
		return safeApply(ref.intValue(MODE_KEY), Mode::of, Mode.NONE);
	}

	private List<FileDescriptor.Open> openFlags() {
		return ref.enumValues(FileDescriptor.Open.class, List.of(), OPEN_FLAGS_KEY);
	}
}
