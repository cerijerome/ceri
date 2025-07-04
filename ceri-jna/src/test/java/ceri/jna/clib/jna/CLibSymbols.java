package ceri.jna.clib.jna;

import ceri.jna.reflect.CAnnotations.CGen;
import ceri.jna.reflect.CSymbolGen;

/**
 * Generates c code symbols for clib.
 */
@CGen(target = { CErrNo.class, CFcntl.class, CIoctl.class, CPoll.class, CSignal.class,
	CTermios.class, CUnistd.class })
public class CLibSymbols {

	public static void main(String[] args) {
		CSymbolGen.Auto.gen(CLibSymbols.class);
	}
}
