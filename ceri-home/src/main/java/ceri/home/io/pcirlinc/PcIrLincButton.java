package ceri.home.io.pcirlinc;

import java.util.List;
import ceri.common.collection.ImmutableUtil;

public enum PcIrLincButton {
	BTN_1(1),
	BTN_2(2),
	BTN_3(3),
	BTN_4(4),
	BTN_5(5),
	BTN_6(6),
	BTN_7(7),
	BTN_8(8),
	BTN_9(9),
	BTN_0(10),
	BTN_VOLUME_UP(11),
	BTN_VOLUME_DOWN(12),
	BTN_MUTE(13),
	BTN_CHANNEL_UP(14),
	BTN_CHANNEL_DOWN(15),
	BTN_POWER(16),
	BTN_ENTER(17),
	BTN_PREV_CHANNEL(18),
	BTN_TV_VIDEO(19),
	BTN_TV_VCR(20),
	BTN_A_B(21),
	BTN_TV_DVD(22),
	BTN_TV_LD(23),
	BTN_INPUT(24),
	BTN_TV_DSS(25),
	BTN_TV_SAT(25),
	BTN_PLAY(26),
	BTN_STOP(27),
	BTN_SEARCH_FORW(28),
	BTN_SEARCH_REV(29),
	BTN_PAUSE(30),
	BTN_RECORD(31),
	BTN_MENU(32),
	BTN_MENU_UP(33),
	BTN_MENU_DOWN(34),
	BTN_MENU_LEFT(35),
	BTN_MENU_RIGHT(36),
	BTN_SELECT(37),
	BTN_EXIT(38),
	BTN_DISPLAY(39),
	BTN_GUIDE(40),
	BTN_PAGE_UP(41),
	BTN_PAGE_DOWN(42),
	BTN_DISK(43),
	BTN_PLUS_10(44),
	BTN_OPEN_CLOSE(45),
	BTN_RANDOM(46),
	BTN_TRACK_FORW(47),
	BTN_TRACK_REV(48),
	BTN_SURROUND(49),
	BTN_SURROUND_MODE(50),
	BTN_SURROUND_UP(51),
	BTN_SURROUND_DOWN(52),
	BTN_PIP(53),
	BTN_PIP_MOVE(54),
	BTN_PIP_SWAP(55),
	BTN_PROGRAM(56),
	BTN_SLEEP(57),
	BTN_ON(58),
	BTN_OFF(59),
	BTN_11(60),
	BTN_12(61),
	BTN_13(62),
	BTN_14(63),
	BTN_15(64),
	BTN_16(65),
	BTN_BRIGHT(66),
	BTN_DIM(67),
	BTN_CLOSE(68),
	BTN_OPEN(69),
	BTN_STOP2(70),
	BTN_FM_AM(71),
	BTN_CUE(72);

	private static final List<PcIrLincButton> DIGITS = ImmutableUtil.asList(BTN_0, BTN_1, BTN_2,
		BTN_3, BTN_4, BTN_5, BTN_6, BTN_7, BTN_8, BTN_9);

	public final int id;

	PcIrLincButton(int id) {
		this.id = id;
	}

	public static PcIrLincButton getDigitButton(byte digit) {
		if (digit < 0 || digit > 9) throw new IllegalArgumentException("Digit must be 0-9: " +
			digit);
		return DIGITS.get(digit);
	}

}
