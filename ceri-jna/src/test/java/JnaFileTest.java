import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class JnaFileTest {
	// Macbook Pro M1 Ventura 13.2.1
	// OpenJDK 19.0.2
	// jna-5.13.0.jar

	// ********************
	// Try with old JNA lib
	// Try chmod?
	// ********************
	
	public static interface CLib extends Library {
		int O_RDWR = 0x2;
		int O_CREAT = 0x200; // value verified in c

		//int open(String path, int flags, short mode) throws LastErrorException;
		int open(String path, int flags, Object... args) throws LastErrorException;
		int close(int fd) throws LastErrorException;
		short umask(short mode) throws LastErrorException;
	}

	// #include <unistd.h>
	// #include <fcntl.h>
	//
	// int main(int argc, char *argv[]) {
	//     int fd = open("test-file", O_CREAT | O_RDWR, 0666); // -rw-r--r--
	//     close(fd);
	//     return 0;
	// }
	
	// Files.createFile(Path.of("test-file")); // // -rw-r--r--

	public static void main(String[] args) {
		var c = Native.load(Platform.C_LIBRARY_NAME, CLib.class);
		//System.out.printf("umask=0%03o\n", c.umask((short) 0022)); // 0022
		int fd = c.open("test-file", CLib.O_CREAT | CLib.O_RDWR, (short) 0666); // --w-r-----
		c.close(fd);
	}
}
