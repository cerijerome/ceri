package ceri.ffm;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class NativeTriangle {

	public static void main(String[] args) throws Throwable {
		String pattern = (args.length > 0) ? args[0] : "********";
		invokeStrdup(pattern);
	}

	private static void invokeStrdup(String pattern) throws Throwable {
		try (var arena = Arena.ofConfined()) {

			// Allocate off-heap memory and
			// copy the argument, a Java string, into off-heap memory
			var nativeString = arena.allocateFrom(pattern);

			// Obtain an instance of the native linker
			var linker = Linker.nativeLinker();

			// Locate the address of the C function signature
			var stdLib = linker.defaultLookup();
			var strdup_addr = stdLib.find("strdup").get();

			// Create a description of the C function
			var layout = MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE);
			var strdup_sig = FunctionDescriptor.of(ValueLayout.ADDRESS.withTargetLayout(layout),
				ValueLayout.ADDRESS.withTargetLayout(layout));

			// Create a downcall handle for the C function
			var strdup_handle = linker.downcallHandle(strdup_addr, strdup_sig);

			//var acc = Accessible.of(nativeString);
			//acc.apply(m -> (MemorySegment) strdup_handle.invokeExact(nativeString));
			// Call the C function directly from Java
			var duplicatedAddress = (MemorySegment) strdup_handle.invokeExact(nativeString);

			for (int i = pattern.length() - 1; i >= 0; i--)
				System.out.println(duplicatedAddress.getString(i));
		}
	}
}
