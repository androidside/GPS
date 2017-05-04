package com.GPS;


import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.common.base.Preconditions;
import com.utils.serial.SerialInputStream;

import jssc.SerialPort;

public class GPSReaderMain
{
	private static final Logger sLogger = Logger.getLogger(GPSReaderMain.class.getName());
	//Need to set port

	public GPSReaderMain(){

	}

	public static void main( String[] args ) {
		try {
			(new Thread(new GPSReader())).start();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}


}
