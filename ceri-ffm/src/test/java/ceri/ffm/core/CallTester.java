package ceri.ffm.core;

import ceri.ffm.type.Callback;

public class CallTester {

	public interface Cb extends Callback {
		long invoke(int i, long l);
	}

	public static void main(String[] args) throws Throwable {
		try (var cb0 = Callback.noOpCallback(Cb.class); var cb1 = (Cb) (i, l) -> i + l) {
			Callback.pointer(cb0);
			Callback.pointer(cb1);
			System.out.println(cb0.invoke(1, 2));
			System.out.println(cb1.invoke(1, 2));
			cb0.close();
			cb1.close();
			System.gc();
			System.out.println(cb0.invoke(2, 3));
			System.gc();
			System.out.println(cb0.invoke(2, 3));
			System.gc();
			System.out.println(cb0.invoke(2, 3));
			System.gc();
			System.out.println(cb0.invoke(2, 3));
		}
	}
}
