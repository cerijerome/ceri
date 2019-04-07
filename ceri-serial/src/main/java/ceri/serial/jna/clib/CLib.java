package ceri.serial.jna.clib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Platform;
import ceri.serial.jna.JnaUtil;

/**
 * For C functionality not available from purejavacomm JTermios.
 */
public class CLib {
	private static final Logger logger = LogManager.getLogger();
	private static final CLibNative CLIB = loadLibrary(Platform.C_LIBRARY_NAME);

	public static int ioctl(int fd, int request, Object... objs) throws CException {
		return JnaUtil.verify(CLIB.ioctl(fd, request, objs), () -> ioctlName(request));
	}

	public static int ioctl(String name, int fd, int request, Object... objs) throws CException {
		return JnaUtil.verify(CLIB.ioctl(fd, request, objs), () -> ioctlName(name, request));
	}

	private static String ioctlName(String name, int request) {
		return String.format("ioctl:%s(0x%08x)", name, request);
	}

	private static String ioctlName(int request) {
		return String.format("ioctl:0x%08x", request);
	}

	private static CLibNative loadLibrary(String name) {
		logger.info("Loading {} started", name);
		// logger.info("Protected: {}", JnaUtil.setProtected()); // only use for debug
		CLibNative lib = JnaUtil.loadLibrary(null, CLibNative.class);
		logger.info("Loading {} complete", name);
		return lib;
	}
}
