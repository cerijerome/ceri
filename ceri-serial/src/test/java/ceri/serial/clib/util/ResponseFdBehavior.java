package ceri.serial.clib.util;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.serial.jna.test.JnaTestUtil.assertPointer;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
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
			assertThat(fd.fd(), is(FD));
		}
	}

	@Test
	public void shouldReadBytes() throws IOException {
		try (FileDescriptor fd = ResponseFd.of(FD, 1, 2, 3, 4, 5)) {
			Memory m = new Memory(4);
			int n = fd.read(m, 0);
			assertThat(n, is(4));
			assertPointer(m, 0, 1, 2, 3, 4);
			n = fd.read(m, 0);
			assertThat(n, is(1));
			assertPointer(m, 0, 5);
		}
	}

	@Test
	public void shouldReadEof() throws IOException {
		try (FileDescriptor fd = ResponseFd.of(FD, 1, 2, 3, 4, 5)) {
			Memory m = new Memory(5);
			int n = fd.read(m, 0);
			assertThat(n, is(5));
			assertPointer(m, 0, 1, 2, 3, 4, 5);
			n = fd.read(m, 0, 0);
			assertThat(n, is(0));
			n = fd.read(m, 0, 1);
			assertThat(n, is(CLib.EOF));
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
			assertThat(fd.bytes().length, is(4));
			fd.write(CUtil.malloc(1, 2, 3));
			assertThat(fd.bytes().length, is(4));
			fd.write(CUtil.malloc(4, 5));
			assertThat(fd.bytes().length, is(5));
		}
	}

	@Test
	public void shouldResizeForWriteAfterSeek() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[3])) {
			fd.seek(5, Seek.SEEK_SET);
			assertThat(fd.position(), is(5));
			assertThat(fd.bytes().length, is(3));
			fd.write(CUtil.malloc(1, 2, 3));
			assertThat(fd.position(), is(8));
			assertArray(fd.bytes(), 0, 0, 0, 0, 0, 1, 2, 3);
		}
	}

	@Test
	public void shouldSeekWithinFile() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[5])) {
			fd.write(CUtil.malloc(1, 2, 3));
			assertThat(fd.seek(0, Seek.SEEK_CUR), is(3));
			assertThat(fd.position(), is(3));
			assertThat(fd.seek(1, Seek.SEEK_CUR), is(4));
			assertThat(fd.position(), is(4));
			assertThat(fd.seek(1, Seek.SEEK_SET), is(1));
			assertThat(fd.position(), is(1));
			assertThat(fd.seek(3, Seek.SEEK_END), is(2));
			assertThat(fd.position(), is(2));
			assertThat(fd.seek(2, Seek.SEEK_HOLE), is(5));
			assertThat(fd.position(), is(5));
		}
	}

	@Test
	public void shouldLimitSeekingWithinFile() throws IOException {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[5])) {
			fd.write(CUtil.malloc(1, 2, 3));
			assertThat(fd.seek(-4, Seek.SEEK_CUR), is(0));
			assertThat(fd.position(), is(0));
			assertThat(fd.seek(-1, Seek.SEEK_SET), is(0));
			assertThat(fd.position(), is(0));
			assertThat(fd.seek(6, Seek.SEEK_END), is(0));
			assertThat(fd.position(), is(0));
		}
	}

	@Test
	public void shouldFailIoctl() {
		try (ResponseFd fd = ResponseFd.of(FD, new byte[4])) {
			assertThrown(() -> fd.ioctl(0));
		}
	}

}
