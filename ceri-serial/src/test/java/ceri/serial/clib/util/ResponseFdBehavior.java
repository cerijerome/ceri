package ceri.serial.clib.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.test.JnaTestUtil.assertPointer;
import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.serial.clib.FileDescriptor;
import ceri.serial.clib.Seek;
import ceri.serial.clib.jna.CLib;
import ceri.serial.clib.jna.CUtil;

public class ResponseFdBehavior {
	private static final int FD = 111;

	@Test
	public void shouldExposeFd() throws IOException {
		try (FileDescriptor fd = ResponseFd.of(FD)) {
			assertEquals(fd.fd(), FD);
		}
	}

	@Test
	public void shouldReadBytes() throws IOException {
		try (FileDescriptor fd = ResponseFd.of(FD, 1, 2, 3, 4, 5)) {
			Memory m = new Memory(4);
			int n = fd.read(m, 0);
			assertEquals(n, 4);
			assertPointer(m, 0, 1, 2, 3, 4);
			n = fd.read(m, 0);
			assertEquals(n, 1);
			assertPointer(m, 0, 5);
		}
	}

	@Test
	public void shouldReadEof() throws IOException {
		try (FileDescriptor fd = ResponseFd.of(FD, 1, 2, 3, 4, 5)) {
			Memory m = new Memory(5);
			int n = fd.read(m, 0);
			assertEquals(n, 5);
			assertPointer(m, 0, 1, 2, 3, 4, 5);
			n = fd.read(m, 0, 0);
			assertEquals(n, 0);
			n = fd.read(m, 0, 1);
			assertEquals(n, CLib.EOF);
		}
	}

	@Test
	public void shouldWriteBytes() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD)) {
			fd.write(CUtil.malloc(1, 2, 3));
			assertArray(fd.bytes, 1, 2, 3);
			fd.write(CUtil.malloc(4, 5));
			assertArray(fd.bytes, 1, 2, 3, 4, 5);
		}
	}

	@Test
	public void shouldResizeForWrites() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[4])) {
			assertEquals(fd.bytes().length, 4);
			fd.write(CUtil.malloc(1, 2, 3));
			assertEquals(fd.bytes().length, 4);
			fd.write(CUtil.malloc(4, 5));
			assertEquals(fd.bytes().length, 5);
		}
	}

	@Test
	public void shouldResizeForWriteAfterSeek() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[3])) {
			fd.seek(5, Seek.SEEK_SET);
			assertEquals(fd.position(), 5);
			assertEquals(fd.bytes().length, 3);
			fd.write(CUtil.malloc(1, 2, 3));
			assertEquals(fd.position(), 8);
			assertArray(fd.bytes(), 0, 0, 0, 0, 0, 1, 2, 3);
		}
	}

	@Test
	public void shouldSeekWithinFile() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[5])) {
			fd.write(CUtil.malloc(1, 2, 3));
			assertEquals(fd.seek(0, Seek.SEEK_CUR), 3);
			assertEquals(fd.position(), 3);
			assertEquals(fd.seek(1, Seek.SEEK_CUR), 4);
			assertEquals(fd.position(), 4);
			assertEquals(fd.seek(1, Seek.SEEK_SET), 1);
			assertEquals(fd.position(), 1);
			assertEquals(fd.seek(3, Seek.SEEK_END), 2);
			assertEquals(fd.position(), 2);
			assertEquals(fd.seek(2, Seek.SEEK_HOLE), 5);
			assertEquals(fd.position(), 5);
		}
	}

	@Test
	public void shouldLimitSeekingWithinFile() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[5])) {
			fd.write(CUtil.malloc(1, 2, 3));
			assertEquals(fd.seek(-4, Seek.SEEK_CUR), 0);
			assertEquals(fd.position(), 0);
			assertEquals(fd.seek(-1, Seek.SEEK_SET), 0);
			assertEquals(fd.position(), 0);
			assertEquals(fd.seek(6, Seek.SEEK_END), 0);
			assertEquals(fd.position(), 0);
		}
	}

	@Test
	public void shouldFailIoctl() {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[4])) {
			assertThrown(() -> fd.ioctl(0));
		}
	}

}
