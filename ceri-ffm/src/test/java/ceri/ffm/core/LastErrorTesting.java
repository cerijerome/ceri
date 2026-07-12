package ceri.ffm.core;

public class LastErrorTesting {

	public static void main(String[] args) {
		System.out.println(LastError.message(3));
		System.out.println(LastError.message(7));
		System.out.println(LastError.message(Integer.MAX_VALUE));
	}

}
