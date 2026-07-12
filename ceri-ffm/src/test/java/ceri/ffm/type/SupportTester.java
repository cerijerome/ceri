package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import ceri.common.array.RawArray;
import ceri.common.io.Direction;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Support.OfArray;

public class SupportTester {
	private static final String t = "a";
	private static final String tt = "bb";
	private static final String ttt = "ccc";

	public static void main(String[] args) {
		var F = StringType.UTF8.support(3, false);
		var N = StringType.UTF8.support(4, true);
		var FF = F.asArray(2);
		var FN = N.asArray(2);
		var NF = F.asArray(3, true);
		var NN = N.asArray(3, true);
		var FFF = FF.asArray(3);
		var FFN = FN.asArray(3);
		var FNF = NF.asArray(3);
		var FNN = NN.asArray(3);
		var NFF = FF.asArray(4, true);
		var NFN = FN.asArray(4, true);
		var NNF = NF.asArray(4, true);
		var NNN = NN.asArray(4, true);
		// FFF: |ttt|ttt||ttt|ttt||ttt|ttt| <=> {{ttt,ttt},{ttt,ttt},{ttt,ttt}}
		encode(FFF, new String[][] { { ttt, ttt }, { ttt, ttt }, { ttt, ttt } });
		// FFN: |tttn|tn||n|ttn||ttn|tttn| <=> {{ttt,t},{,tt},{tt,ttt}}
		encode(FFN, new String[][] { { ttt, t }, { "", tt }, { tt, ttt } });
		// FNF: |ttt|ttt|nnn||ttt|nnn||nnn| <=> {{ttt,ttt},{ttt},{}}
		encode(FNF, new String[][] { { ttt, ttt }, { ttt }, {} });
		// NFF: |ttt|ttt||ttt|ttt||nnnnnn| <=> {{ttt,ttt},{ttt,ttt}}
		encode(NFF, new String[][] { { ttt, ttt }, { ttt, ttt } });
		// FNN: |tttn|tn|n||ttn|n||tn|n| <=> {{ttt,t},{tt},{t}}
		encode(FNN, new String[][] { { ttt, t }, { tt }, { t } });
		// NNF: |ttt|ttt|nnn||ttt|nnn||nnn| <=> {{ttt,ttt},{ttt}}
		encode(NNF, new String[][] { { ttt, ttt }, { ttt } });
		// NFN: |tttn|tn||ttn|n||n|n| <=> {{ttt,t},{tt,}}
		encode(NFN, new String[][] { { ttt, t }, { tt, "" } });
		// NNN: |tttn|tn|n||tn|n||ttn|n||n| <=> {{ttt,t},{t},{tt}}
		encode(NNN, new String[][] { { ttt, t }, { t }, { tt } });
	}

	private static <T> MemorySegment encode(OfArray<T> support, T value) {
		var r = support.encode(Direction.in, value);
		System.out.println(support);
		FfmTesting.bin(r.value());
		System.out.println("Encode: " + RawArray.toString(value));
		var decoded = support.decode(r.value());
		System.out.println("Decode: " + RawArray.toString(decoded));
		System.out.println();
		return r.value();
	}

}
