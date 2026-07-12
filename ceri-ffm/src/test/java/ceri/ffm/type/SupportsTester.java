package ceri.ffm.type;

import ceri.common.reflect.Generics;
import ceri.ffm.reflect.Refine;
import ceri.ffm.reflect.Refine.Const;
import ceri.ffm.reflect.Refine.Dims;
import ceri.ffm.reflect.Refine.Nul;
import ceri.ffm.reflect.Refine.Packed;

public class SupportsTester {

	// $(int[5!][4][3],<240/1)
	// $(int[5!][4][3],<240/1)
	// $(int[5!][4][3],<240/1)
	// $((const int[3][2]*)[2!][3],<48/1)
	// $(const int[5!][4][3]*,<8/8)
	// $(void*[3!]*,<8/8)

	public static void main(String[] args) {
		var ss = Supports.DEF;
		var s0 = ss.from(int[][][].class, Refine.custom().align(1).dims(5, 4, 3).nul().context());
		var s1 = Primitive.INT.align(1).asArray(5, true).asArray(4).asArray(3);
		var s2 = ss.from(new Generics.Token<@Packed @Dims({ 5, 4, 3 }) @Nul int[][][]>() {});
		var s3 = ss.from(new Generics.Token //
		<@Packed @Dims({ 2, 3 }) @Nul Pointer<@Dims({ 3, 2 }) @Const int[][]>[][]>() {});
		System.out.println(s0);
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);
		System.out.println(s1.asPointer(true));
		System.out.println(Support.VOID.asPointer(true).asArray(3, true).asPointer());
		System.out.println(Primitive.BYTE.asPointer(true).asArray(3, true).asArray(2).asPointer());
		System.out.println(
			Primitive.CHAR.asArray(5, true).asPointer().asArray(3, true).asArray(2).asPointer());
	}

}
