package ceri.zwave.luup;

import java.io.IOException;
import ceri.zwave.veralite.CommandFactory;
import ceri.zwave.veralite.VeraLite;

public class ZWaveNetwork {
	public static final String file = "S_ZWaveNetwork1.xml";
	public static final String sid = "urn:micasaverde-com:serviceId:ZWaveNetwork1";
	public static final String sType = "urn:schemas-micasaverde-org:service:ZWaveNetwork:1";
	private static final int ALL_DEVICE = 1;
	private static final String DATA = "Data";
	private static final String SET_ALL_ON_OFF_DATA = "0x27-1-";
	private static final String ALL_OFF_DATA = "0-0x19-0xff-2-0x27-5";
	private static final String ALL_ON_DATA = "0-0x19-0xff-2-0x27-4";
	private final CommandFactory factory;
	
	public static enum AllOnOff {
		none(0), off(1), on(2), both(255);
		
		public final int value;
		
		private AllOnOff(int value) {
			this.value = value;
		}
	}
	
	public static void main(String[] args) throws Exception {
		VeraLite vl = new VeraLite("192.168.0.109:3480");
		//sp.off(4);
		//Thread.sleep(2000);
		//zwn.enableAlOnOff(5);
		//vl.switchPower.on(3);
		//vl.dimming.on(4);
		vl.zWaveNetwork.allOn();
		//vl.zWaveNetwork.setAllOnOff(1, AllOnOff.both);
		Thread.sleep(5000);
		//vl.dimming.off(4);
		vl.zWaveNetwork.allOff();
		//Thread.sleep(3000);
		//vl.switchPower.off(3);
	}
	
	public static enum Variable implements ceri.zwave.veralite.Variable {
		LastUpdate, // DEVICEDATA_LastUpdate_CONST
		LastHeal, // DEVICEDATA_LastUpdate_CONST
		LastRouteFailure, // DEVICEDATA_LastUpdate_CONST
		LastDongleBackup,
		NetStatusID,
		NetStatusText,
		ComPort, // DEVICEDATA_COM_Port_on_PC_CONST
		LockComPort,
		NodeID, // The dongle's id
		VersionInfo,
		HomeID,
		Role,
		ResetMode,
		InclusionMode,
		NodeType,
		Timeout,
		Multiple,
		SimulateIncomingData,
		PollingEnabled,
		PollDelayInitial,
		PollDelayDeadTime,
		PollMinDelay,
		PollFrequency,
		LastError,
		DelayProcessing,
		FailedOnly,
		Use45,
		UseMR,
		TO3066, // indicates when we're going to work around the TO3066 issue
		LimitNeighbors; // indicates when we figure manual routing, only consider Z-Wave's neighbors as valid options
	}

	public static enum Action implements ceri.zwave.veralite.Action {
		ResetNetwork,
		ReconfigureAllNodes,
		RemoveNodes,
		AddNodes,
		DownloadNetwork,
		HealNetwork,
		UpdateNetwork,
		UpdateNeighbors,
		SetPolling,
		SendData,
		PollAllNodes,
		SoftReset,
		BackupDongle,
		SceneIDs, // For scene controllers, node#-button#=ZWaveSceneID,...
		PutByte;
	}
	
	public ZWaveNetwork(CommandFactory factory) {
		this.factory = factory;
	}
	
	public void allOn() throws IOException {
		sendData(ALL_DEVICE, ALL_ON_DATA);
	}
	
	public void allOff() throws IOException {
		sendData(ALL_DEVICE, ALL_OFF_DATA);
	}
	
	public void setAllOnOff(int device, AllOnOff aoo) throws IOException {
		sendData(device, SET_ALL_ON_OFF_DATA + Integer.toHexString(aoo.value));
	}
	
	public void sendData(int device, String data) throws IOException {
		factory.action(Action.SendData, sid).device(device).param(DATA, data).execute();
	}
	
}
