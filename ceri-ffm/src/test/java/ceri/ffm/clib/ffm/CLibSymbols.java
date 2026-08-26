package ceri.ffm.clib.ffm;

import ceri.ffm.reflect.CAnnotations.CGen;
import ceri.ffm.reflect.CSymbolGen;

/**
 * Generates c code symbols for clib.
 */
@CGen(target = { CErrNo.class, CFcntl.class, CTermios.class, CIoctl.class, CPoll.class,
	CSignal.class, CUnistd.class })
public class CLibSymbols {

	public static void main(String[] args) {
		CSymbolGen.Auto.gen(CLibSymbols.class);
	}
}
