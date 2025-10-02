package ceri.serial.libusb.jna;

import ceri.common.data.Xcoder;

/**
 * From document "USB Device Class Definition for Terminal Types Release 3.0". Not part of libusb
 * source.
 */
public enum LibUsbTerminalType {
	// USB terminal Types (I/O)
	USB_undefined(0x0100),
	USB_streaming(0x0101),
	USB_vendor_specific(0x01ff),
	// Input terminal types (I)
	input_undefined(0x0200),
	microphone(0x0201),
	desktop_microphone(0x0202),
	personal_microphone(0x0203),
	omni_directional_microphone(0x0204),
	microphone_array(0x0205),
	processing_microphone_array(0x0206),
	// Output terminal types (O)
	output_undefined(0x0300),
	speaker(0x0301),
	headphones(0x0302),
	head_mounted_display_audio(0x0303),
	desktop_speaker(0x0304),
	room_speaker(0x0305),
	communication_speaker(0x0306),
	low_frequency_effects_speaker(0x0307),
	// Bi-directional terminal types (I/O)
	bidirectional_undefined(0x0400),
	handset(0x0401),
	headset(0x0402),
	speakerphone(0x0403),
	echo_suppressing_speakerphone(0x0404),
	echo_canceling_speakerphone(0x0405),
	// Telephony terminal types (I/O)
	telephony_undefined(0x0500),
	phone_line(0x501),
	telephone(0x502),
	down_line_phone(0x503),
	// External terminal types (I/O)
	external_undefined(0x0600),
	analog_connector(0x0601),
	digital_audio_interface(0x0602),
	line_connector(0x0603),
	legacy_audio_connector(0x0604),
	spdif_interface(0x0605),
	da1394_stream(0x0606),
	dv1394_stream_soundtrack(0x0607),
	adat_lightpipe(0x0608),
	tdif(0x0609),
	madi(0x060a),
	// Embedded function terminal types
	embedded_undefined(0x0700), // I/O
	level_calibration_noise_source(0x0701), // O
	equalization_noise(0x0702), // O
	cd_player(0x0703), // I
	dat(0x0704), // I/O
	dcc(0x0705), // I/O
	compressed_audio_player(0x0706), // I/O
	analog_tape(0x0707), // I/O
	phonograph(0x0708), // I
	vcr_audio(0x0709), // I
	video_disc_audio(0x070a), // I
	dvd_audio(0x070b), // I
	tv_tuner_audio(0x070c), // I
	satellite_receiver_audio(0x070d), // I
	cable_tuner_audio(0x070e), // I
	dss_audio(0x070f), // I
	radio_receiver(0x0710), // I
	radio_transmitter(0x0711), // O
	multitrack_recorder(0x0712), // I/O
	synthesizer(0x0713), // I
	piano(0x0714), // I/O
	guitar(0x0715), // I/O
	drums_rhythm(0x0716), // I/O
	other_musical_instrument(0x0717); // I/O

	public static final Xcoder.Type<LibUsbTerminalType> xcoder =
		Xcoder.type(LibUsbTerminalType.class);
	public final int value;

	private LibUsbTerminalType(int value) {
		this.value = value;
	}
}
