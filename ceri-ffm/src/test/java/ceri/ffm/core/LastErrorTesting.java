package ceri.ffm.core;

public class LastErrorTesting {

	public static void main(String[] args) {
		System.out.println(ErrNo.message(3));
		System.out.println(ErrNo.message(7));
		System.out.println(ErrNo.message(Integer.MAX_VALUE));
	}

}
