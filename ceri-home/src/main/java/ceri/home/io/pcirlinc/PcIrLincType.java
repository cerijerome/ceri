package ceri.home.io.pcirlinc;

public enum PcIrLincType {
	TYPE_TV(0),
	TYPE_CABLE(1),
	TYPE_VIDEO(2),
	TYPE_SATELLITE(3),
	TYPE_VCR(4),
	TYPE_TAPE(5),
	TYPE_LD(6),
	TYPE_DAT(7),
	TYPE_DVD(8),
	TYPE_AMPTUNER(9),
	TYPE_MISCAUDIO(10),
	TYPE_CD(11),
	TYPE_PHONO(12),
	TYPE_HOMEAUTO(13);

	public final int id;

	PcIrLincType(int id) {
		this.id = id;
	}

}
