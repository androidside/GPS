package com.utils.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import com.GPS.GPSReader;
import com.google.common.base.Preconditions;
import com.utils.serial.SerialPreferences;

import jssc.SerialPort;

public class SerialEstablishConnection {		


	public SerialPort connect() throws IOException
	{       
		SerialPreferences preferences = new SerialPreferences();
		final String fPort = preferences.fEagle_serialPort;
		try
		{
			Preconditions.checkNotNull(fPort);			
			SerialPort serialPort = new SerialPort (fPort);

			if (serialPort.isOpened())
			{
				System.out.println("Error: Serial Port currently in use");
			}

			else{
				serialPort.openPort();
			}
			serialPort.setParams(preferences.fEagle_baudRate, preferences.fEagle_dataBits,preferences.fEagle_stopBits, preferences.fEagle_parity, false, false);

			System.out.println("Serial Port Opened");
			
			return serialPort;


		}
		catch (Exception e)
		{
			throw new IOException("Error opening port: " + fPort + ": " + e);
		}
		
		
	}


	//		SerialInputStream serialPortInputStream= new SerialInputStream(serialPort);
	//(new Thread(new GPSReader(serialPortInputStream))).start(); //Read SerialPort

	//We don't need to write to the Serial Port
	//SerialOutputStream serialPortOutputStream= new SerialOutputStream(serialPort);
	//(new Thread(new SerialWriter(serialPortOutputStream))).start();



}
