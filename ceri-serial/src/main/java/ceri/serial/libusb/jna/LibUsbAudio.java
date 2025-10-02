package ceri.serial.libusb.jna;

import com.sun.jna.Pointer;
import ceri.common.data.Xcoder;
import ceri.common.math.Maths;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;
import ceri.jna.type.VarStruct;

/**
 * Support for USB audio classes. From document "USB Device Class Definition for Audio Devices
 * Revision 1.0". Not part of libusb source.
 */
public class LibUsbAudio {
	/** Audio interface protocol codes. Section A.3. */
	public static final int PR_PROTOCOL_UNDEFINED = 0x00;

	private LibUsbAudio() {}

	/***
	 * Section A.2.
	 */
	public static enum audio_interface_subclass_code {
		SUBCLASS_UNDEFINED(0x00),
		AUDIOCONTROL(0x01),
		AUDIOSTREAMING(0x02),
		MIDISTREAMING(0x03);

		public static final Xcoder.Type<audio_interface_subclass_code> xcoder =
			Xcoder.type(audio_interface_subclass_code.class);
		public final int value;

		private audio_interface_subclass_code(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.4.
	 */
	public static enum audio_descriptor_type {
		CS_UNDEFINED(0x20),
		CS_DEVICE(0x21),
		CS_CONFIGURATION(0x22),
		CS_STRING(0x23),
		CS_INTERFACE(0x24),
		CS_ENDPOINT(0x25),
		CS_CLUSTER(0x26);

		public static final Xcoder.Type<audio_descriptor_type> xcoder =
			Xcoder.type(audio_descriptor_type.class);
		public final int value;

		private audio_descriptor_type(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.5.
	 */
	public static enum audio_control_interface_desc_subtype {
		AC_DESCRIPTOR_UNDEFINED(0x00),
		HEADER(0x01),
		INPUT_TERMINAL(0x02),
		OUTPUT_TERMINAL(0x03),
		MIXER_UNIT(0x04),
		SELECTOR_UNIT(0x05),
		FEATURE_UNIT(0x06),
		PROCESSING_UNIT(0x07),
		EXTENSION_UNIT(0x08),
		ASSOC_INTERFACE(0x09); // not documented?

		public static final Xcoder.Type<audio_control_interface_desc_subtype> xcoder =
			Xcoder.type(audio_control_interface_desc_subtype.class);
		public final int value;

		private audio_control_interface_desc_subtype(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.6.
	 */
	public static enum audio_streaming_interface_desc_subtype {
		AS_DESCRIPTOR_UNDEFINED(0x00),
		AS_GENERAL(0x01),
		FORMAT_TYPE(0x02),
		FORMAT_SPECIFIC(0x03);

		public static final Xcoder.Type<audio_streaming_interface_desc_subtype> xcoder =
			Xcoder.type(audio_streaming_interface_desc_subtype.class);
		public final int value;

		private audio_streaming_interface_desc_subtype(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.7.
	 */
	public static enum audio_proc_unit_process_type {
		PROCESS_UNDEFINED(0x00),
		UPDOWNMIX_PROCESS(0x01),
		DOLBY_PROLOGIC_PROCESS(0x02),
		STEREO3D_EXTENDER_PROCESS(0x03),
		REVERBERATION_PROCESS(0x04),
		CHORUS_PROCESS(0x05),
		DYN_RANGE_COMP_PROCESS(0x06);

		public static final Xcoder.Type<audio_proc_unit_process_type> xcoder =
			Xcoder.type(audio_proc_unit_process_type.class);
		public final int value;

		private audio_proc_unit_process_type(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.8.
	 */
	public static enum audio_endpoint_desc_subtype {
		DESCRIPTOR_UNDEFINED(0x00),
		EP_GENERAL(0x01);

		public static final Xcoder.Type<audio_endpoint_desc_subtype> xcoder =
			Xcoder.type(audio_endpoint_desc_subtype.class);
		public final int value;

		private audio_endpoint_desc_subtype(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.9.
	 */
	public static enum audio_request_code {
		REQUEST_CODE_UNDEFINED(0x00),
		SET_CUR(0x01),
		SET_MIN(0x02),
		SET_MAX(0x03),
		SET_RES(0x04),
		SET_MEM(0x05),
		GET_CUR(0x81),
		GET_MIN(0x82),
		GET_MAX(0x83),
		GET_RES(0x84),
		GET_MEM(0x85),
		GET_STAT(0xff);

		public static final Xcoder.Type<audio_request_code> xcoder =
			Xcoder.type(audio_request_code.class);
		public final int value;

		private audio_request_code(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.1.
	 */
	public static enum audio_term_control_selector {
		TE_CONTROL_UNDEFINED(0x00),
		COPY_PROTECT_CONTROL(0x01);

		public static final Xcoder.Type<audio_term_control_selector> xcoder =
			Xcoder.type(audio_term_control_selector.class);
		public final int value;

		private audio_term_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.2.
	 */
	public static enum audio_feat_unit_control_selector {
		FU_CONTROL_UNDEFINED(0x00),
		MUTE_CONTROL(0x01),
		VOLUME_CONTROL(0x02),
		BASS_CONTROL(0x03),
		MID_CONTROL(0x04),
		TREBLE_CONTROL(0x05),
		GRAPHIC_EQUALIZER_CONTROL(0x06),
		AUTOMATIC_GAIN_CONTROL(0x07),
		DELAY_CONTROL(0x08),
		BASS_BOOST_CONTROL(0x09),
		LOUDNESS_CONTROL(0x0a);

		public static final Xcoder.Type<audio_feat_unit_control_selector> xcoder =
			Xcoder.type(audio_feat_unit_control_selector.class);
		public final int value;

		private audio_feat_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.3.1.
	 */
	public static enum audio_updownmix_proc_unit_control_selector {
		UD_CONTROL_UNDEFINED(0x00),
		UD_ENABLE_CONTROL(0x01),
		UD_MODE_SELECT_CONTROL(0x02);

		public static final Xcoder.Type<audio_updownmix_proc_unit_control_selector> xcoder =
			Xcoder.type(audio_updownmix_proc_unit_control_selector.class);
		public final int value;

		private audio_updownmix_proc_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.3.2.
	 */
	public static enum audio_dolbypl_proc_unit_control_selector {
		DP_CONTROL_UNDEFINED(0x00),
		DP_ENABLE_CONTROL(0x01),
		DP_MODE_SELECT_CONTROL(0x02);

		public static final Xcoder.Type<audio_dolbypl_proc_unit_control_selector> xcoder =
			Xcoder.type(audio_dolbypl_proc_unit_control_selector.class);
		public final int value;

		private audio_dolbypl_proc_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.3.3.
	 */
	public static enum audio_stereo3d_proc_unit_control_selector {
		S3D_CONTROL_UNDEFINED(0x00),
		S3D_ENABLE_CONTROL(0x01),
		SPACIOUSNESS_CONTROL(0x03);

		public static final Xcoder.Type<audio_stereo3d_proc_unit_control_selector> xcoder =
			Xcoder.type(audio_stereo3d_proc_unit_control_selector.class);
		public final int value;

		private audio_stereo3d_proc_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.3.4.
	 */
	public static enum audio_reverb_proc_unit_control_selector {
		RV_CONTROL_UNDEFINED(0x00),
		RV_ENABLE_CONTROL(0x01),
		REVERB_LEVEL_CONTROL(0x02),
		REVERB_TIME_CONTROL(0x03),
		REVERB_FEEDBACK_CONTROL(0x04);

		public static final Xcoder.Type<audio_reverb_proc_unit_control_selector> xcoder =
			Xcoder.type(audio_reverb_proc_unit_control_selector.class);
		public final int value;

		private audio_reverb_proc_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.3.5.
	 */
	public static enum audio_chorus_proc_unit_control_selector {
		CH_CONTROL_UNDEFINED(0x00),
		CH_ENABLE_CONTROL(0x01),
		CHORUS_LEVEL_CONTROL(0x02),
		CHORUS_RATE_CONTROL(0x03),
		CHORUS_DEPTH_CONTROL(0x04);

		public static final Xcoder.Type<audio_chorus_proc_unit_control_selector> xcoder =
			Xcoder.type(audio_chorus_proc_unit_control_selector.class);
		public final int value;

		private audio_chorus_proc_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.3.6.
	 */
	public static enum audio_drcomp_proc_unit_control_selector {
		DR_CONTROL_UNDEFINED(0x00),
		DR_ENABLE_CONTROL(0x01),
		COMPRESSION_RATE_CONTROL(0x02),
		MAXAMPL_CONTROL(0x03),
		THRESHOLD_CONTROL(0x04),
		ATTACK_TIME(0x05),
		RELEASE_TIME(0x06);

		public static final Xcoder.Type<audio_drcomp_proc_unit_control_selector> xcoder =
			Xcoder.type(audio_drcomp_proc_unit_control_selector.class);
		public final int value;

		private audio_drcomp_proc_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.4.
	 */
	public static enum audio_ext_unit_control_selector {
		XU_CONTROL_UNDEFINED(0x00),
		XU_ENABLE_CONTROL(0x01);

		public static final Xcoder.Type<audio_ext_unit_control_selector> xcoder =
			Xcoder.type(audio_ext_unit_control_selector.class);
		public final int value;

		private audio_ext_unit_control_selector(int value) {
			this.value = value;
		}
	}

	/***
	 * Section A.10.5.
	 */
	public static enum audio_endpoint_control_selector {
		EP_CONTROL_UNDEFINED(0x00),
		SAMPLING_FREQ_CONTROL(0x01),
		PITCH_CONTROL(0x02);

		public static final Xcoder.Type<audio_endpoint_control_selector> xcoder =
			Xcoder.type(audio_endpoint_control_selector.class);
		public final int value;

		private audio_endpoint_control_selector(int value) {
			this.value = value;
		}
	}

	/**
	 * Originator type from audio_interrupt_data. From "USB Device Class Definition for Audio
	 * Devices" 3.7.1.2.
	 */
	public static enum audio_originator {
		AUDIOCONTROL_INTERFACE(0),
		AUDIOSTREAMING_INTERFACE(1),
		AUDIOSTREAMING_ENDPOINT(2);

		public static final Xcoder.Type<audio_originator> xcoder =
			Xcoder.type(audio_originator.class);
		public final int value;

		private audio_originator(int value) {
			this.value = value;
		}
	}

	/**
	 * Status interrupt endpoint data. Section 3.7.1.2.
	 */
	@Fields({ "bStatusType", "bOriginator" })
	public static class audio_interrupt_data extends Struct {
		private static final int INTERRUPT_PENDING_MASK = 0x80;
		private static final int MEMORY_CONTENTS_CHANGED_MASK = 0x40;
		private static final int ORIGINATOR_MASK = 0x0f;
		public byte bStatusType;
		public byte bOriginator;

		public audio_interrupt_data(Pointer p) {
			super(p);
		}

		public boolean interruptPending() {
			return (bStatusType & INTERRUPT_PENDING_MASK) != 0;
		}

		public boolean memoryContentsChanged() {
			return (bStatusType & MEMORY_CONTENTS_CHANGED_MASK) != 0;
		}

		public audio_originator originatorType() {
			return audio_originator.xcoder.decode(bStatusType & ORIGINATOR_MASK);
		}
	}

	/**
	 * Audio channel cluster spatial locations. Section 3.7.2.3.
	 */
	public static enum audio_spatial_location {
		LEFT_FRONT(1 << 0), // L
		RIGHT_FRONT(1 << 1), // R
		CENTER_FRONT(1 << 2), // C
		LOW_FREQUENCY_ENHANCEMENT(1 << 3), // LFE
		LEFT_SURROUND(1 << 4), // LS
		RIGHT_SURROUND(1 << 5), // RS
		LEFT_OF_CENTER(1 << 6), // LC
		RIGHT_OF_CENTER(1 << 7), // RC
		SURROUND(1 << 8), // S
		SIDE_LEFT(1 << 9), // SL
		SIDE_RIGHT(1 << 10), // SR
		TOP(1 << 11); // T

		public static final Xcoder.Types<audio_spatial_location> xcoder =
			Xcoder.types(audio_spatial_location.class);
		public final int value;

		private audio_spatial_location(int value) {
			this.value = value;
		}
	}

	// 3.7.2.3 Dolby Prologic Cluster Descriptor
	// 3.7.2.3 Left Group Cluster Descriptor

	/**
	 * Section 4.3.2.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bcdADC", "wTotalLength",
		"bInCollection", "baInterfaceNr" })
	public static class audio_control_header_descriptor extends VarStruct {
		public static final int BASE_LENGTH = 8;
		public byte bLength; // 8+n
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype = (byte) audio_control_interface_desc_subtype.HEADER.value;
		public short bcdADC; //
		public short wTotalLength;
		public byte bInCollection;
		public byte[] baInterfaceNr = new byte[0];

		public audio_control_header_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			baInterfaceNr = new byte[count];
		}

		@Override
		protected int varCount() {
			return Maths.ubyte(bInCollection);
		}
	}

	/**
	 * Section 4.3.2.1.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bTerminalID", "wTerminalType",
		"bAssocTerminal", "bNrChannels", "wChannelConfig", "iChannelNames", "iTerminal" })
	public static class audio_input_term_descriptor extends Struct {
		public static final int LENGTH = 12;
		public byte bLength = LENGTH;
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.INPUT_TERMINAL.value;
		public byte bTerminalID;
		public short wTerminalType;
		public byte bAssocTerminal;
		public byte bNrChannels;
		public short wChannelConfig; // audio_spatial_location
		public byte iChannelNames;
		public byte iTerminal;

		public audio_input_term_descriptor(Pointer p) {
			super(p);
		}
	}

	/**
	 * Section 4.3.2.2.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bTerminalID", "wTerminalType",
		"bAssocTerminal", "bSourceID", "iTerminal" })
	public static class audio_output_term_descriptor extends Struct {
		public static final int LENGTH = 9;
		public byte bLength = LENGTH;
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.OUTPUT_TERMINAL.value;
		public byte bTerminalID;
		public short wTerminalType; // terminal type class code
		public byte bAssocTerminal;
		public byte bSourceID;
		public byte iTerminal;

		public audio_output_term_descriptor(Pointer p) {
			super(p);
		}
	}

	/**
	 * Section 4.3.2.3.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bUnitID", "bNrInPins",
		"baSourceID", "bNrChannels", "wChannelConfig", "iChannelNames", "bmControls", "iMixer" })
	public static class audio_mixer_unit_descriptor extends Struct {
		public static final int BASE_LENGTH = 10;
		public byte bLength; // 10+p+n
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.MIXER_UNIT.value;
		public byte bUnitID; //
		public byte bNrInPins; // p
		public byte[] baSourceID; // [p]
		public byte bNrChannels;
		public short wChannelConfig; // audio_spatial_location
		public byte iChannelNames;
		public byte[] bmControls; // [n]
		public byte iMixer;

		// TODO: cannot map this to a struct, access dynamically instead

		public audio_mixer_unit_descriptor(Pointer p) {
			super(p);
		}

		public int p() {
			return Maths.ubyte(bNrInPins);
		}

		public int n() {
			return Math.max(Maths.ubyte(bLength) - BASE_LENGTH - p(), 0);
		}
	}

	/**
	 * Section 4.3.2.4.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bUnitID", "bNrInPins",
		"baSourceID", "iSelector" })
	public static class audio_selector_unit_descriptor extends Struct {
		public static final int BASE_LENGTH = 6;
		public byte bLength; // 6+p
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.SELECTOR_UNIT.value;
		public byte bUnitID;
		public byte bNrInPins; // p
		public byte[] baSourceID; // [p]
		public byte iSelector;

		// TODO: cannot map this to a struct, access dynamically instead

		public audio_selector_unit_descriptor(Pointer p) {
			super(p);
		}

		public int p() {
			return Maths.ubyte(bNrInPins);
		}
	}

	/**
	 * Section 4.3.2.5.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bUnitID", "bSourceID",
		"bControlSize", "bmaControls", "iFeature" })
	public static class audio_feat_unit_descriptor extends Struct {
		public static final int BASE_LENGTH = 7;
		public byte bLength; // 7+(ch+1)*n
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.FEATURE_UNIT.value;
		public byte bUnitID;
		public byte bSourceID;
		public byte bControlSize; // n
		public byte[] bmaControls; // [(ch+1)*n]
		public byte iFeature;

		// TODO: cannot map this to a struct, access dynamically instead

		public audio_feat_unit_descriptor(Pointer p) {
			super(p);
		}

		public int channels() { // ch
			int n = n();
			return n == 0 ? 0 : Math.max(0, ((Maths.ubyte(bLength) - BASE_LENGTH) / n) - 1);
		}

		public int n() {
			return Maths.ubyte(bControlSize);
		}
	}

	/**
	 * General processing unit descriptor. Section 4.3.2.6, 4.3.2.6.3, 4.3.2.6.4, 4.3.2.6.5,
	 * 4.3.2.6.6.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bUnitID", "wProcessType",
		"bNrInPins", "baSourceID", "bNrChannels", "wChannelConfig", "iChannelNames", "bControlSize",
		"bmControls", "iProcessing", "extra" })
	public static class audio_proc_unit_descriptor extends Struct {
		public static final int BASE_LENGTH = 13;
		public byte bLength; // 13+p+n+x
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.PROCESSING_UNIT.value;
		public byte bUnitID; //
		public short wProcessType; // audio_processing_unit_process_type
		public byte bNrInPins; // p
		public byte[] baSourceID; // [p]
		public byte bNrChannels;
		public short wChannelConfig; // audio_spatial_location
		public byte iChannelNames;
		public byte bControlSize; // n
		public byte[] bmControls; // [n]
		public byte iProcessing;
		public byte[] extra; // [x] >= 0

		// TODO: cannot map this to a struct, access dynamically instead

		public audio_proc_unit_descriptor(Pointer p) {
			super(p);
		}

		public int p() {
			return Maths.ubyte(bNrInPins);
		}

		public int n() {
			return Maths.ubyte(bControlSize);
		}

		public int x() {
			return Maths.ubyte(bLength) - BASE_LENGTH - p() - n();
		}
	}

	/**
	 * Covers up/down-mix and Dolby Prologic descriptors. Section 4.3.2.6.1, 4.3.2.6.2.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bUnitID", "wProcessType",
		"bNrInPins", "bSourceID", "bNrChannels", "wChannelConfig", "iChannelNames", "bControlSize",
		"bmControls", "iProcessing", "bNrModes", "waModes" })
	public static class audio_mode_proc_unit_descriptor extends Struct {
		public static final int BASE_LENGTH = 15;
		public byte bLength; // 15+n+2*m
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.PROCESSING_UNIT.value;
		public byte bUnitID;
		public short wProcessType; // UPDOWNMIX_PROCESS/DOLBY_PROLOGIC_PROCESS
		public byte bNrInPins = 1; // p = 1
		public byte bSourceID; // [p]
		public byte bNrChannels;
		public short wChannelConfig; // audio_spatial_location
		public byte iChannelNames;
		public byte bControlSize; // n
		public byte[] bmControls; // [n]
		public byte iProcessing;
		public byte bNrModes; // m
		public short[] waModes; // [m]

		// TODO: cannot map this to a struct, access dynamically instead

		public audio_mode_proc_unit_descriptor(Pointer p) {
			super(p);
		}

		public int n() {
			return Maths.ubyte(bControlSize);
		}

		public int m() {
			return Maths.ubyte(bNrModes);
		}
	}

	/**
	 * Section 4.3.2.7.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bUnitID", "wExtensionCode",
		"bNrInPins", "baSourceID", "bNrChannels", "wChannelConfig", "iChannelNames", "bControlSize",
		"bmControls", "iExtension" })
	public static class audio_ext_unit_descriptor extends Struct {
		public static final int BASE_LENGTH = 13;
		public byte bLength; // 13+p+n
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.PROCESSING_UNIT.value;
		public byte bUnitID; //
		public short wExtensionCode; // vendor-specific
		public byte bNrInPins; // p
		public byte[] baSourceID; // [p]
		public byte bNrChannels;
		public short wChannelConfig; // audio_spatial_location
		public byte iChannelNames;
		public byte bControlSize; // n
		public byte[] bmControls; // [n]
		public byte iExtension;

		// TODO: cannot map this to a struct, access dynamically instead

		public audio_ext_unit_descriptor(Pointer p) {
			super(p);
		}

		public int p() {
			return Maths.ubyte(bNrInPins);
		}

		public int n() {
			return Maths.ubyte(bControlSize);
		}
	}

	/**
	 * Section 4.3.2.8.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bInterfaceNr", "extra" })
	public static class audio_assoc_interface_descriptor extends VarStruct {
		public static final int BASE_LENGTH = 4;
		public byte bLength; // 4+x
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_control_interface_desc_subtype.ASSOC_INTERFACE.value;
		public byte bInterfaceNr;
		public byte[] extra = new byte[0]; // association-specific number

		public audio_assoc_interface_descriptor(Pointer p) {
			super(p);
		}

		@Override
		protected void setVarArray(int count) {
			extra = new byte[count];
		}

		@Override
		protected int varCount() {
			return Maths.ubyte(bLength) - BASE_LENGTH;
		}
	}

	/**
	 * Section 4.5.2.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bTerminalLink", "bDelay",
		"wFormatTag" })
	public static class audio_streaming_interface_descriptor extends Struct {
		public static final int LENGTH = 7;
		public byte bLength = LENGTH;
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_INTERFACE.value;
		public byte bDescriptorSubtype =
			(byte) audio_streaming_interface_desc_subtype.AS_GENERAL.value;
		public byte bTerminalLink;
		public byte bDelay;
		public short wFormatTag;

		public audio_streaming_interface_descriptor(Pointer p) {
			super(p);
		}
	}

	/**
	 * Section 4.6.1.2.
	 */
	@Fields({ "bLength", "bDescriptorType", "bDescriptorSubtype", "bmAttributes", "bLockDelayUnits",
		"wLockDelay" })
	public static class audio_streaming_iso_endpoint_descriptor extends Struct {
		public static final int LENGTH = 7;
		public byte bLength = LENGTH;
		public byte bDescriptorType = (byte) audio_descriptor_type.CS_ENDPOINT.value;
		public byte bDescriptorSubtype = (byte) audio_endpoint_desc_subtype.EP_GENERAL.value;
		public byte bmAttributes;
		public byte bLockDelayUnits;
		public short wLockDelay;

		public audio_streaming_iso_endpoint_descriptor(Pointer p) {
			super(p);
		}
	}
}
