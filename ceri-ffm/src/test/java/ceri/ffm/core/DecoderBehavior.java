package ceri.ffm.core;

import java.lang.foreign.MemorySegment;
import org.junit.Test;
import ceri.common.array.RawArray;
import ceri.common.io.Direction;
import ceri.common.test.Assert;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.Support;
import ceri.ffm.type.Support.OfArray;

public class DecoderBehavior {
	private static final short[] t = { 0x11 };
	private static final short[] tt = { 0x22, 0x22 };
	private static final short[] ttt = { 0x33, 0x33, 0x33 };
	private static final Primitive.OfShort S = Primitive.SHORT;
	private static final Support.OfArray<short[]> F = S.asArray(3);
	private static final Support.OfArray<short[]> N = S.asArray(4, true);
	private static final Support.OfArray<short[][]> FF = F.asArray(2);
	private static final Support.OfArray<short[][]> FN = N.asArray(2);
	private static final Support.OfArray<short[][]> NF = F.asArray(3, true);
	private static final Support.OfArray<short[][]> NN = N.asArray(3, true);
	private static final Support.OfArray<short[][][]> FFF = FF.asArray(3);
	private static final Support.OfArray<short[][][]> FFN = FN.asArray(3);
	private static final Support.OfArray<short[][][]> FNF = NF.asArray(3);
	private static final Support.OfArray<short[][][]> FNN = NN.asArray(3);
	private static final Support.OfArray<short[][][]> NFF = FF.asArray(4, true);
	private static final Support.OfArray<short[][][]> NFN = FN.asArray(4, true);
	private static final Support.OfArray<short[][][]> NNF = NF.asArray(4, true);
	private static final Support.OfArray<short[][][]> NNN = NN.asArray(4, true);

	@Test
	public void shouldDecodeArrays() {
		// FFF: |ttt|ttt||ttt|ttt||ttt|ttt| <=> {{ttt,ttt},{ttt,ttt},{ttt,ttt}}
		encode(FFF, new short[][][] { { ttt, ttt }, { ttt, ttt }, { ttt, ttt } });
		// FFN: |tttn|tn||n|ttn||ttn|tttn| <=> {{ttt,t},{,tt},{tt,ttt}}
		encode(FFN, new short[][][] { { ttt, t }, { {}, tt }, { tt, ttt } });
		// FNF: |ttt|ttt|nnn||ttt|nnn||nnn| <=> {{ttt,ttt},{ttt},{}}
		encode(FNF, new short[][][] { { ttt, ttt }, { ttt }, {} });
		// NFF: |ttt|ttt||ttt|ttt||nnnnnn| <=> {{ttt,ttt},{ttt,ttt}}
		encode(NFF, new short[][][] { { ttt, ttt }, { ttt, ttt } });
		// FNN: |tttn|tn|n||ttn|n||tn|n| <=> {{ttt,t},{tt},{t}}
		encode(FNN, new short[][][] { { ttt, t }, { tt }, { t } });
		// NNF: |ttt|ttt|nnn||ttt|nnn||nnn| <=> {{ttt,ttt},{ttt}}
		encode(NNF, new short[][][] { { ttt, ttt }, { ttt } });
		// NFN: |tttn|tn||ttn|n||n|n| <=> {{ttt,t},{tt,}}
		encode(NFN, new short[][][] { { ttt, t }, { tt, {} } });
		// NNN: |tttn|tn|n||tn|n||ttn|n||n| <=> {{ttt,t},{t},{tt}}
		encode(NNN, new short[][][] { { ttt, t }, { t }, { tt } });
	}

	private static <T> MemorySegment encode(OfArray<T> support, T value) {
		var r = support.encode(Direction.in, value);
		FfmTesting.bin(r.value());
		System.out.println("Encode: " + RawArray.toString(value));
		var decoded = support.decode(r.value());
		System.out.println("Decode: " + RawArray.toString(decoded));
		System.out.println();
		Assert.deepEqual(value, decoded);
		return r.value();
	}
}
