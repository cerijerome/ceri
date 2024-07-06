package ceri.jna.clib.util;

import static ceri.common.function.FunctionUtil.safeApply;
import java.util.List;
import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;
import ceri.jna.clib.CFileDescriptor;
import ceri.jna.clib.Mode;
import ceri.jna.clib.OpenFlag;

public class FileDescriptorProperties extends Ref<BaseProperties> {
	private static final String PATH_KEY = "path";
	private static final String MODE_KEY = "mode";
	private static final String OPEN_FLAGS_KEY = "open.flags";

	public FileDescriptorProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
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

	private List<OpenFlag> openFlags() {
		return ref.enumValues(OpenFlag.class, List.of(), OPEN_FLAGS_KEY);
	}
}
