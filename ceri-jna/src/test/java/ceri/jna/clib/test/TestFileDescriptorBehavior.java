package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.jna.test.JnaTestUtil.assertMemory;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteProvider;
import ceri.jna.clib.Seek;
import ceri.jna.util.JnaUtil;

public class TestFileDescriptorBehavior {

	@Test
	public void shouldProvideFileDescriptorNumber() throws IOException {
		try (var fd = TestFileDescriptor.of(33)) {
			assertEquals(fd.fd(), 33);
		}
	}

	@Test
	public void shouldReadIntoMemory() throws IOException {
		try (var fd = TestFileDescriptor.of(33); Memory m = JnaUtil.calloc(5)) {
			assertEquals(fd.read(m), 0);
			fd.read.assertAuto(5);
			fd.read.autoResponses(ByteProvider.of(6, 7, 8, 9));
			assertEquals(fd.read(m), 4);
			fd.read.assertAuto(5);
			assertMemory(m, 0, 6, 7, 8, 9, 0);
		}
	}

	@Test
	public void shouldAllowNullRead() throws IOException {
		try (var fd = TestFileDescriptor.of(33); Memory m = JnaUtil.calloc(1)) {
			fd.read.autoResponses((ByteProvider) null);
			assertEquals(fd.read(m), 0);
			fd.read.assertAuto(1);
		}
	}

	@Test
	public void shouldWriteFromMemory() throws IOException {
		try (var fd = TestFileDescriptor.of(33); Memory m = JnaUtil.mallocBytes(1, 2, 3, 4, 5)) {
			fd.write(m);
			fd.write.assertAuto(ByteProvider.of(1, 2, 3, 4, 5));
		}
	}

	@Test
	public void shouldAllowWriteFromNullPointer() throws IOException {
		try (var fd = TestFileDescriptor.of(33)) {
			fd.write(null, 0, 3);
			fd.write.assertAuto(ByteProvider.of(0, 0, 0));
		}
	}

	@Test
	public void shouldSeek() throws IOException {
		try (var fd = TestFileDescriptor.of(33)) {
			fd.seek.autoResponses(3);
			assertEquals(fd.seek(5, Seek.SEEK_END), 3);
			fd.seek.assertAuto(List.of(5, Seek.SEEK_END));
		}
	}

	@Test
	public void shouldCallIoctl() throws IOException {
		try (var fd = TestFileDescriptor.of(33)) {
			fd.ioctl.autoResponses(3);
			assertEquals(fd.ioctl(5, "test", -1), 3);
			fd.ioctl.assertAuto(List.of(5, "test", -1));
		}
	}

	@Test
	public void shouldCallFcntl() throws IOException {
		try (var fd = TestFileDescriptor.of(33)) {
			fd.fcntl.autoResponses(7);
			assertEquals(fd.fcntl(11, "test", -1), 7);
			fd.fcntl.assertAuto(List.of(11, "test", -1));
		}
	}

}
