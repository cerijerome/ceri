package ceri.home.audio;

import java.util.Arrays;


public class ClipInfo {
	private final int startPercent;
	private final int endPercent;
	
	public ClipInfo(int startPercent, int endPercent) {
		this.startPercent = startPercent;
		this.endPercent = endPercent;
	}
	
	public int getStartPercent() {
	    return startPercent;
    }
	
	public int getEndPercent() {
	    return endPercent;
    }
	
	public byte[] getClippedData(int frameSize, byte[] data) {
		int startOffset = (((data.length * startPercent) / 100)
			/ frameSize) * frameSize;
		int endOffset = (((data.length * (100 - endPercent)) / 100)
			/ frameSize) * frameSize;
		return Arrays.copyOfRange(data, startOffset, endOffset - startOffset);
	}
}
