package ceri.serial.libusb.jna;

import static ceri.common.test.TestUtil.exerciseEnum;
import java.util.function.Function;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.array.ArrayUtil;
import ceri.common.test.Assert;
import ceri.jna.type.Struct;
import ceri.serial.libusb.jna.LibUsbAudio.audio_assoc_interface_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_chorus_proc_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_control_header_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_control_interface_desc_subtype;
import ceri.serial.libusb.jna.LibUsbAudio.audio_descriptor_type;
import ceri.serial.libusb.jna.LibUsbAudio.audio_dolbypl_proc_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_drcomp_proc_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_endpoint_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_endpoint_desc_subtype;
import ceri.serial.libusb.jna.LibUsbAudio.audio_ext_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_ext_unit_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_feat_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_feat_unit_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_input_term_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_interface_subclass_code;
import ceri.serial.libusb.jna.LibUsbAudio.audio_interrupt_data;
import ceri.serial.libusb.jna.LibUsbAudio.audio_mixer_unit_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_mode_proc_unit_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_originator;
import ceri.serial.libusb.jna.LibUsbAudio.audio_output_term_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_proc_unit_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_proc_unit_process_type;
import ceri.serial.libusb.jna.LibUsbAudio.audio_request_code;
import ceri.serial.libusb.jna.LibUsbAudio.audio_reverb_proc_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_selector_unit_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_spatial_location;
import ceri.serial.libusb.jna.LibUsbAudio.audio_stereo3d_proc_unit_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_streaming_interface_desc_subtype;
import ceri.serial.libusb.jna.LibUsbAudio.audio_streaming_interface_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_streaming_iso_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsbAudio.audio_term_control_selector;
import ceri.serial.libusb.jna.LibUsbAudio.audio_updownmix_proc_unit_control_selector;

public class LibUsbAudioTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(LibUsbAudio.class);
	}

	@Test
	public void testEnumTypes() {
		exerciseEnum(audio_interface_subclass_code.class);
		exerciseEnum(audio_descriptor_type.class);
		exerciseEnum(audio_control_interface_desc_subtype.class);
		exerciseEnum(audio_streaming_interface_desc_subtype.class);
		exerciseEnum(audio_proc_unit_process_type.class);
		exerciseEnum(audio_endpoint_desc_subtype.class);
		exerciseEnum(audio_request_code.class);
		exerciseEnum(audio_term_control_selector.class);
		exerciseEnum(audio_feat_unit_control_selector.class);
		exerciseEnum(audio_updownmix_proc_unit_control_selector.class);
		exerciseEnum(audio_dolbypl_proc_unit_control_selector.class);
		exerciseEnum(audio_stereo3d_proc_unit_control_selector.class);
		exerciseEnum(audio_reverb_proc_unit_control_selector.class);
		exerciseEnum(audio_chorus_proc_unit_control_selector.class);
		exerciseEnum(audio_drcomp_proc_unit_control_selector.class);
		exerciseEnum(audio_ext_unit_control_selector.class);
		exerciseEnum(audio_endpoint_control_selector.class);
		// exerciseEnum(audio_originator.class);
		exerciseEnum(audio_spatial_location.class);
	}

	@Test
	public void testInterruptData() {
		var t = new audio_interrupt_data(null);
		Assert.equal(t.interruptPending(), false);
		Assert.equal(t.memoryContentsChanged(), false);
		Assert.equal(t.originatorType(), audio_originator.AUDIOCONTROL_INTERFACE);
		t.bStatusType = (byte) 0xc2;
		Assert.equal(t.interruptPending(), true);
		Assert.equal(t.memoryContentsChanged(), true);
		Assert.equal(t.originatorType(), audio_originator.AUDIOSTREAMING_ENDPOINT);
		writeRead(t, audio_interrupt_data::new);
	}

	@Test
	public void testControlHeaderDescriptor() {
		var t = new audio_control_header_descriptor(null);
		t.baInterfaceNr = ArrayUtil.bytes.of(1, 2, 3);
		t.bInCollection = (byte) t.baInterfaceNr.length;
		Struct.write(t);
		t = writeRead(t, audio_control_header_descriptor::new);
		Assert.equal(t.bInCollection, (byte) 3);
		Assert.array(t.baInterfaceNr, 1, 2, 3);
	}

	@Test
	public void testMixerUnitDescriptor() {
		var t = new audio_mixer_unit_descriptor(null);
		t.baSourceID = ArrayUtil.bytes.of(1, 2, 3);
		t.bNrInPins = (byte) t.baSourceID.length;
		t.bmControls = ArrayUtil.bytes.of(4, 5, 6);
		t.bLength = (byte) (audio_mixer_unit_descriptor.BASE_LENGTH + t.baSourceID.length +
			t.bmControls.length);
		Assert.equal(t.bLength, (byte) 16);
		Assert.equal(t.p(), 3);
		Assert.equal(t.n(), 3);
	}

	@Test
	public void testSelectorUnitDescriptor() {
		var t = new audio_selector_unit_descriptor(null);
		t.baSourceID = ArrayUtil.bytes.of(1, 2, 3);
		t.bNrInPins = (byte) t.baSourceID.length;
		t.bLength = (byte) (audio_selector_unit_descriptor.BASE_LENGTH + t.baSourceID.length);
		Assert.equal(t.bLength, (byte) 9);
		Assert.equal(t.p(), 3);
	}

	@Test
	public void testFeatUnitDescriptor() {
		var t = new audio_feat_unit_descriptor(null);
		Assert.equal(t.channels(), 0);
		t.bControlSize = 2;
		t.bmaControls = ArrayUtil.bytes.of(1, 2, 3, 4, 5, 6);
		t.bLength = (byte) (audio_feat_unit_descriptor.BASE_LENGTH + t.bmaControls.length);
		Assert.equal(t.bLength, (byte) 13);
		Assert.equal(t.channels(), 2);
	}

	@Test
	public void testProcUnitDescriptor() {
		var t = new audio_proc_unit_descriptor(null);
		t.baSourceID = ArrayUtil.bytes.of(1, 2, 3);
		t.bNrInPins = (byte) t.baSourceID.length;
		t.bmControls = ArrayUtil.bytes.of(4, 5, 6);
		t.bControlSize = (byte) t.bmControls.length;
		t.extra = ArrayUtil.bytes.of(7, 8);
		t.bLength = (byte) (audio_proc_unit_descriptor.BASE_LENGTH + t.baSourceID.length +
			t.bmControls.length + t.extra.length);
		Assert.equal(t.bLength, (byte) 21);
		Assert.equal(t.p(), 3);
		Assert.equal(t.n(), 3);
		Assert.equal(t.x(), 2);
	}

	@Test
	public void testModeProcUnitDescriptor() {
		var t = new audio_mode_proc_unit_descriptor(null);
		t.bmControls = ArrayUtil.bytes.of(4, 5, 6);
		t.bControlSize = (byte) t.bmControls.length;
		t.waModes = ArrayUtil.shorts.of(7, 8);
		t.bNrModes = (byte) t.waModes.length;
		t.bLength = (byte) (audio_mode_proc_unit_descriptor.BASE_LENGTH + t.bmControls.length +
			t.waModes.length * 2);
		Assert.equal(t.bLength, (byte) 22);
		Assert.equal(t.n(), 3);
		Assert.equal(t.m(), 2);
	}

	@Test
	public void testExtUnitDescriptor() {
		var t = new audio_ext_unit_descriptor(null);
		t.baSourceID = ArrayUtil.bytes.of(1, 2, 3);
		t.bNrInPins = (byte) t.baSourceID.length;
		t.bmControls = ArrayUtil.bytes.of(4, 5);
		t.bControlSize = (byte) t.bmControls.length;
		t.bLength = (byte) (audio_ext_unit_descriptor.BASE_LENGTH + t.baSourceID.length +
			t.bmControls.length);
		Assert.equal(t.bLength, (byte) 18);
		Assert.equal(t.p(), 3);
		Assert.equal(t.n(), 2);
	}

	@Test
	public void testAssocInterfaceDescriptor() {
		var t = new audio_assoc_interface_descriptor(null);
		t.extra = ArrayUtil.bytes.of(1, 2, 3);
		t.bLength = (byte) (audio_assoc_interface_descriptor.BASE_LENGTH + t.extra.length);
		Struct.write(t);
		t = writeRead(t, audio_assoc_interface_descriptor::new);
		Assert.equal(t.bLength, (byte) 7);
		Assert.array(t.extra, 1, 2, 3);
	}

	@Test
	public void testSimpleTypes() {
		writeRead(audio_input_term_descriptor::new);
		writeRead(audio_output_term_descriptor::new);
		writeRead(audio_streaming_interface_descriptor::new);
		writeRead(audio_streaming_iso_endpoint_descriptor::new);
	}

	private static <T extends Struct> T writeRead(Function<Pointer, T> constructor) {
		return writeRead(constructor.apply(null), constructor);
	}

	private static <T extends Struct> T writeRead(T t, Function<Pointer, T> constructor) {
		return Struct.read(constructor.apply(Struct.write(t).getPointer()));
	}
}
