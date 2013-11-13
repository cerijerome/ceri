package ceri.zwave.upnp;
/**
 * http://wiki.micasaverde.com/index.php/Luup_UPNP_Files
 */
public enum DeviceType {
	HOME_AUTO_GATEWAY    ("urn:schemas-micasaverde-com:device:HomeAutomationGateway:1"),
	BINARY_LIGHT         ("urn:schemas-upnp-org:device:BinaryLight:1"),
	DIMMABLE_LIGHT       ("urn:schemas-upnp-org:device:DimmableLight:1"),
	THERMOSTAT           ("urn:schemas-upnp-org:device:HVAC_ZoneThermostat:1"),
	HUMIDITY_SENSOR      ("urn:schemas-micasaverde-com:device:HumiditySensor:1"),
	MULTI_IO             ("urn:schemas-micasaverde-com:device:MultiIO:1"),
	DOOR_LOCK            ("urn:schemas-micasaverde-com:device:DoorLock:1"),
	DOOR_SENSOR          ("urn:schemas-micasaverde-com:device:DoorSensor:1"),
	ZWAVE_NETWORK        ("urn:schemas-micasaverde-com:device:ZWaveNetwork:1"),
	ZIGBEE_NETWORK       ("urn:schemas-micasaverde-com:device:ZigbeeNetwork:1"),
	INSTEON_NETWORK      ("urn:schemas-micasaverde-com:device:InsteonNetwork:1"),
	USB_UIRT             ("urn:schemas-micasaverde-com:device:USBUIRT:1"),
	TEMPERATURE_SENSOR   ("urn:schemas-micasaverde-com:device:TemperatureSensor:1"),
	POWER_METER          ("urn:schemas-micasaverde-com:device:PowerMeter:1"),
	MOTION_SENSOR        ("urn:schemas-micasaverde-com:device:MotionSensor:1"),
	SMOKE_SENSOR         ("urn:schemas-micasaverde-com:device:SmokeSensor:1"),
	LIGHT_SENSOR         ("urn:schemas-micasaverde-com:device:LightSensor:1"),
	IR_TRANSMITTER       ("urn:schemas-micasaverde-com:device:IrTransmitter:1"),
	WINDOW_COVERING      ("urn:schemas-micasaverde-com:device:WindowCovering:1"),
	GENERIC_IO           ("urn:schemas-micasaverde-com:device:GenericIO:1"),
	REMOTE_CONTROL       ("urn:schemas-micasaverde-com:device:RemoteControl:1"),
	COMBO_DEVICE         ("urn:schemas-micasaverde-com:device:ComboDevice:1"),
	CAMERA               ("urn:schemas-upnp-org:device:DigitalSecurityCamera:1"),
	SERIALPORT           ("urn:micasaverde-org:device:SerialPort:1"),
	SERIALPORTROOT       ("urn:micasaverde-org:device:SerialPortRoot:1"),
	SCENE_CONTROLLER     ("urn:schemas-micasaverde-com:device:SceneController:1"),
	SCENE_CONTR_LED      ("urn:schemas-micasaverde-com:device:SceneControllerLED:1"),
	ENERGY_CALCULATOR    ("urn:schemas-micasaverde-com:device:EnergyCalculator:1"),
	TEMP_LEAK_SENSOR     ("urn:schemas-micasaverde-com:device:TemperatureLeakSensor:1"),
	RELAY                ("urn:schemas-micasaverde-com:device:Relay:1"),
	ALARMPANEL           ("urn:schemas-micasaverde-com:device:AlarmPanel:1"),
	ALARMPARTITION1      ("urn:schemas-micasaverde-com:device:AlarmPartition:1"),
	ALARMPARTITION2      ("urn:schemas-micasaverde-com:device:AlarmPartition:2"),
	SCENE                ("urn:schemas-micasaverde-com:device:Scene:1"),
	IR                   ("urn:schemas-micasaverde-com:device:IrDevice:1"),
	TV                   ("urn:schemas-micasaverde-com:device:tv:1"),
	CABLE                ("urn:schemas-micasaverde-com:device:cable:1"),
	SATELLITE            ("urn:schemas-micasaverde-com:device:satellite:1"),
	VIDEO_ACCESSORY      ("urn:schemas-micasaverde-com:device:videoaccessory:1"),
	VCR_DVR              ("urn:schemas-micasaverde-com:device:vcrdvd:1"),
	DVD_BLURAY           ("urn:schemas-micasaverde-com:device:dvdbluray:1"),
	RECEIVER             ("urn:schemas-micasaverde-com:device:receiver:1"),
	AMP                  ("urn:schemas-micasaverde-com:device:amp:1"),
	CD                   ("urn:schemas-micasaverde-com:device:cd:1"),
	MISC_HOME_CONTROL    ("urn:schemas-micasaverde-com:device:mischomecontrol:1"),
	AV_MISC              ("urn:schemas-micasaverde-com:device:avmisc:1"),
	VIRTUAL_DEVICE       ("urn:schemas-micasaverde-com:device:VirtualDevice:1");
	
	public final String urn;
	
	private DeviceType(String urn) {
		this.urn = urn;
	}
	
}
