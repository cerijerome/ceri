package ceri.ffm;

import java.lang.foreign.Arena;

public class Ffm {

	// TODO:
	// - struct packing support in ffm
	// - memory arenas
	// - unions in ffm
	// - struct prototype
	// - union prototype
	// - int type prototype

	// Questions
	// - can you close a global mem segment?

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		var arena0 = Arena.ofAuto();
		var arena1 = Arena.ofAuto();

		System.out.println(arena0);
		System.out.println(arena1);
	}
}
