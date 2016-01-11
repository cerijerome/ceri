package ceri.jssc;

import jssc.SerialPortList;

public class PortLister {

	public static void main(String[] args) {
		for (String name : SerialPortList.getPortNames()) System.out.println(name);
	}
	
}
