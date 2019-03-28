package ceri.serial.jna.clib;

import static ceri.serial.jna.JnaUtil.validateFileDescriptor;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import ceri.serial.jna.JnaUtil;
import jtermios.JTermios;

/**
 * For C functionality not available from purejavacomm JTermios.
 */
public class CLib {
	private static final Logger logger = LogManager.getLogger();
	private static CLibNative CLIB = loadLibrary(Platform.C_LIBRARY_NAME);

	public static void main(String[] args) throws IOException {
		int fd = validateFileDescriptor(JTermios.open("/dev/stdout", 1));
		byte[] data = "hello\n".getBytes();
		int count = TermiosUtil.write(fd, data);
		System.out.println(count);
	}

	public static int ioctl(int fd, int request, Pointer p) {
		return CLIB.ioctl(fd, request, p);
	}

	public static int ioctl(int fd, int request, PointerType p) {
		return CLIB.ioctl(fd, request, p);
	}

	private static CLibNative loadLibrary(String name) {
		logger.info("Loading {} started", name);
		logger.info("Protected: {}", JnaUtil.setProtected());
		CLibNative lib = JnaUtil.loadLibrary(null, CLibNative.class);
		logger.info("Loading {} complete", name);
		return lib;
	}
}
