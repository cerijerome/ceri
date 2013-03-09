package ceri.image.ebay;

import java.util.Arrays;

/**
 * The documented Image Types for eBay Picture Services.
 */
public enum EpsImageType {
	_0(96, 96),
	_1(400, 400),
	_2(200, 200),
	_3(800, 800),
	_4(640, 480, true),
	_5(100, 75, true),
	_6(70, 70),
	_7(150, 150),
	_8(400, 300),
	_9(200, 150, true),
	//_10(9999, 9999), internal only
	_11(310, 90, true),
	_12(500, 500),
	_13(120, 42),
	_14(64, 64),
	_15(225, 125),
	_16(400, 300, true),
	_17(80, 80),
	_18(200, 200),
	_19(400, 400),
	_20(800, 600),
	_21(500, 500),
	_22(60, 60),
	_23(80, 80),
	_24(298, 298),
	_25(500, 500),
	_26(140, 140),
	_27(640, 480),
	_28(180, 60),
	_29(900, 300),
	_30(300, 115),
	_31(300, 225),
	_33(48, 48),
	_34(50, 50),
	_35(300, 300),
	_36(150, 40),
	_37(175, 175),
	_38(115, 115),
	_39(32, 32, true),
	_40(80, 60),
	_41(467, 467),
	_42(225, 70),
	_43(221, 330),
	_44(425, 34),
	_45(1200, 1200),
	_46(300, 300),
	_47(623, 290),
	_49(200, 170),
	_50(100, 85),
	_51(90, 90),
	_52(132, 123),
	_53(190, 100),
	_54(122, 122),
	_55(286, 214),
	_56(100, 100),
	_57(1600, 1600),
	_58(640, 640),
	_59(960, 960),
	//_61(120, 60), not available
	_62(225, 225),
	_65(300, 120),
	_66(82, 35),
	_67(728, 90),
	_68(256, 35),
	_69(120, 60),
	_70(64, 35),
	_71(55, 55),
	_72(500, 375),
	_73(65, 65),
	_74(135, 115),
	_75(430, 325),
	_76(400, 320),
	_77(72, 54, true),
	_78(75, 56, true),
	_79(320, 240, true),
	_80(600, 450, true),
	_81(96, 72, true),
	_82(147, 147),
	_83(358, 358),
	_84(498, 498),
	_85(726, 726),
	_86(1024, 1024),
	_87(70, 53),
	_88(175, 130),
	_89(600, 500),
	_90(220, 220),
	_91(276, 276),
	_92(560, 420),
	_93(360, 480),
	_94(280, 210),
	_95(180, 240),
	_96(205, 75),
	_97(90, 90, true),
	_98(180, 170, true),
	_99(200, 150, true),
	_100(650, 412, true),
	_101(107, 88, true),
	_102(247, 187, true),
	_103(180, 180),
	_104(720, 215);
	
	public final int id;
	public final int width;
	public final int height;
	public boolean padded;
	
	private EpsImageType(int width, int height) {
		this(width, height, false);
	}
	
	private EpsImageType(int width, int height, boolean padded) {
		id = Integer.parseInt(name().substring(1));
		this.width = width;
		this.height = height;
		this.padded = padded;
	}
	
	/**
	 * Creates the url from a given eps path
	 */
	public String url(String epsPath) {
		return epsPath.replaceFirst("_\\d+\\.", "_" + id + ".");
	}

	public static EpsImageType largestType() {
		return largestType(Arrays.asList(EpsImageType.values()));
	}
	
	public static EpsImageType largestType(Iterable<EpsImageType> types) {
		EpsImageType largestType = null;
		long largestArea = 0;
		for (EpsImageType type : types) {
			long area = (long) type.width * type.height;
			if (area <= largestArea) continue;
			largestType = type;
			largestArea = area;
		}
		return largestType;
	}

}
