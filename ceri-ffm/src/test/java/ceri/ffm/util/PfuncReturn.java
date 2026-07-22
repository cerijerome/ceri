package ceri.ffm.util;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;

public class PfuncReturn {

	// void (*signal(int sig, void (*func)(int)))(int);
	//
	// typedef void (*sig_t)(int);
	// sig_t signal(int sig, sig_t func);
	//
	
	// typedef int (*math_op_fptr)(int, int);
	//
	// math_op_fptr get_operation(const char* name);
	//

	public static void main(String[] args) throws Throwable {
		// 1. Acquire the system linker and load your native library symbols
		var lookup = SymbolLookup.libraryLookup("my_math_lib", Segments.GLOBAL);
		// 2. Define descriptors: (int, int) -> int and (String) -> Function Pointer
		var mathOpDesc = FunctionDescriptor.of(Layouts.INT, Layouts.INT, Layouts.INT);
		var getOpDesc = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
		// 3. Find and link the 'get_operation' factory function
		var getOpAddress = lookup.find("get_operation").orElseThrow();
		var getOperation = Native.LINKER.downcallHandle(getOpAddress, getOpDesc);
		// 4. Invoke the factory function to get the raw function pointer
		try (Arena arena = Arena.ofConfined()) {
			// Convert Java String to C string
			var cStringName = arena.allocateFrom("add");
			// Execute the factory function: (String) -> function pointer
			var functionPointer = (MemorySegment) getOperation.invokeExact(cStringName);
			// Safety Check: Verify the pointer isn't NULL
			if (functionPointer.equals(MemorySegment.NULL))
				throw new Exception("Failed to fetch math function pointer.");
			// 5. Convert the returned function pointer into a callable MethodHandle
			// Pass the returned MemorySegment directly into downcallHandle
			var mathOperation = Native.LINKER.downcallHandle(functionPointer, mathOpDesc);
			// 6. Execute the function pointer dynamically fetched from C
			var result = (int) mathOperation.invokeExact(15, 30);
			System.out.println("Result from function pointer: " + result); // Output: 45
		}
	}
}
