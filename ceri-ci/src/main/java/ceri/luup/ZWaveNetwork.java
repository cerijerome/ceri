package ceri.luup;

public class ZWaveNetwork {
	public static final String file = "S_ZWaveNetwork1.xml";
	public static final String sid = "urn:micasaverde-com:serviceId:ZWaveNetwork1";
	public static final String sType = "urn:schemas-micasaverde-org:service:ZWaveNetwork:1";

	public static enum Variable implements ceri.luup.Variable {
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

	public static enum Action implements ceri.luup.Action {
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
}
