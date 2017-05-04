package com.utils.serial;

import jssc.SerialPort;

public class SerialPreferences {
     final String fEagle_serialPort = "COM15";
	 final int fEagle_baudRate = 4800; 
	 final int fEagle_dataBits = SerialPort.DATABITS_8;
	 final int fEagle_stopBits = SerialPort.STOPBITS_1;
	 final int fEagle_parity = SerialPort.PARITY_NONE;
	public SerialPreferences() {
		 
	}
	
}
