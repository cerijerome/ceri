package ceri.zwave.upnp;

public enum DeviceFile {
	BINARY_LIGHT("D_BinaryLight1.xml"),
	DIMMABLE_LIGHT("D_DimmableLight1.xml"),
	THERMOSTAT("D_HVAC_ZoneThermostat1.xml"),
	HUMIDITY_SENSOR("D_HumiditySensor1.xml"),
	MULTI_IO("D_GC100.xml"),
	DOOR_LOCK("D_DoorLock1.xml"),
	DOOR_SENSOR("D_DoorSensor1.xml"),
	ZWAVE_NETWORK("D_ZWaveNetwork.xml"),
	INSTEON_NETWORK("D_InsteonNetwork.xml"),
	USB_UIRT("D_USB_UIRT.xml"),
	TEMPERATURE_SENSOR("D_TemperatureSensor1.xml"),
	POWER_METER("D_PowerMeter1.xml"),
	MOTION_SENSOR("D_MotionSensor1.xml"),
	SMOKE_SENSOR("D_SmokeSensor1.xml"),
	LIGHT_SENSOR("D_LightSensor1.xml"),
	IR_TRANSMITTER("D_IrTransmitter1.xml"),
	IR_DEVICE("D_IrDevice1.xml"),
	WINDOW_COVERING("D_WindowCovering1.xml"),
	SERIAL_PORT_ROOT("D_SerialPortRoot1.xml"),
	GENERIC_IO("D_GenericIO1.xml"),
	REMOTE_CONTROL("D_RemoteControl1.xml"),
	COMBO_DEVICE("D_ComboDevice1.xml"),
	CAMERA("D_DigitalSecurityCamera1.xml"),
	SCENE_CONTROLLER("D_SceneController1.xml"),
	SCENE_CONTR_LED("D_SceneControllerLED1.xml"),
	ENERGY_CALCULATOR("D_EnergyCalculator1.xml"),
	AV_MISC("D_AvMisc1.xml"),
	TEMP_LEAK_SENSOR("D_TempLeakSensor1.xml"),
	RELAY("D_Relay1.xml"),
	AV_SCENE("D_Scene1.xml");

	public final String filename;

	DeviceFile(String filename) {
		this.filename = filename;
	}

}
