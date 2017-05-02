package com.utils.serial;

import jssc.SerialPort;

public class SerialPreferences {
     final String fEagle_serialPort = "COM14";
	 final int fEagle_baudRate = 9600; 
	 final int fEagle_dataBits = SerialPort.DATABITS_8;
	 final int fEagle_stopBits = SerialPort.STOPBITS_1;
	 final int fEagle_parity = SerialPort.PARITY_NONE;
	public SerialPreferences() {
		 
	}
	
}
