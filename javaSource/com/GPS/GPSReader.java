package com.GPS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import com.GPS.GPSReaderMain;
import com.sun.corba.se.pept.transport.Connection;
import com.utils.GPS.NMEAParser;
import com.utils.GPS.NMEAParser.GPSPosition;
import com.utils.eagle.EagleFileUtils;
import com.utils.eagle.SleepUtil;
import com.utils.serial.*;

import jssc.SerialPort;
import com.utils.GPS.*;

import java.lang.*;

public class GPSReader implements Runnable {

	SerialInputStream in;
	private float oldSecondUTC = 0;
	private static String sSetDynamicModeAirplaneLessThan2G = String.valueOf(new char[] {0xB5, 0x62, 0x06, 0x24, 0x24, 0x00, 0xFF, 0xFF,
			0x07, 0x03, 0x00, 0x00, 0x00, 0x00, 0x10, 0x27, 0x00, 0x00, 0x05, 0x00, 0xFA, 0x00, 0xFA, 0x00, 0x64, 0x00, 0x2C, 0x01, 0x00,
			0x3C, 0x00, 0x00, 0x00, 0x00, 0xC8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B, 0x4A});



	public GPSReader( ) {
	}

	public void run() {
		SerialPort serialPort = null;
		NMEAParser parser = null;
		SerialInputStream is = null;
		SerialOutputStream os = null;
		SerialEstablishConnection connection = new SerialEstablishConnection();

		long prevConfigSendMillis = 0;

		System.out.println("Starting ...");

		while (true) 
		{
			try
			{
				if (is == null || os == null || serialPort == null)
				{

					serialPort = connection.connect();
					is = new SerialInputStream(serialPort);
					os = new SerialOutputStream(serialPort);
					System.out.println("Starting NMEA Parser");
					parser = new NMEAParser();
				}

				//				while(true){
				//					byte b = 0;
				//					if(is.read(b)!=0)
				//					System.out.print(b);
				//					else{
				//						System.out.println("Bye!");
				//						break;
				//						}
				//				}
				String readCRLFLine = EagleFileUtils.readCRLFLine(is);
				GPSPosition parse = parser.parse(readCRLFLine);
				if (parse != null)
				{
					System.out.println("NMEA " + parse);
					int hours = (int) (parse.time / 10000);
					int minutes = (int) ((parse.time - hours * 10000) / 100);
					float seconds = parse.time - hours * 10000 - minutes * 100;

					GPSReadings gpsReadings = new GPSReadings();
					gpsReadings.altitudeMeters = parse.altitude;
					gpsReadings.latitudeDegrees = parse.lat;
					gpsReadings.longitudeDegrees =  parse.lon;
					gpsReadings.hourUTC = hours;
					gpsReadings.minuteUTC = minutes;
					gpsReadings.secondUTC = seconds;

				
					if(gpsReadings.secondUTC != oldSecondUTC){
						gpsReadings.printGpsReadings(gpsReadings);
						parse.toString();
						oldSecondUTC =  gpsReadings.secondUTC;
						
					}
					

				}

				//
				//	                    if (System.currentTimeMillis() - fPrevPCTimeSettingMillis > PC_TIME_SET_THRESH_MILLIS)
				//	                    {
				////	                        String setTimeCommand = "/bin/date +%T -s \"" + hours + ":" + minutes + ":" + seconds + "\"";
				////	                        try
				////	                        {
				////	                            Runtime.getRuntime().exec(setTimeCommand);
				////	                        }
				////	                        catch (Exception e)
				////	                        {
				////	                            sLogger.logp(Level.WARNING, "GpsReaderUblox", "readAndPublishForever()", "exception setting time: " + setTimeCommand, e);
				//	//
				////	                        }
				//	                        fPrevPCTimeSettingMillis = System.currentTimeMillis();
				//	                    }
				//	                    fPrevGpsTimeMillis = System.currentTimeMillis();
				//	                    
				//	                 
				//	                }
				//

				/*
				 * It's easier to just continue to send this then
				 * try and parse the current state.  Is has been tested to work several times.
				 */
				if (System.currentTimeMillis() - prevConfigSendMillis > 100000) //CONFIG_SEND_PERIOD_MILLIS = 100000?
				{
					sendConfig(os);
					prevConfigSendMillis = System.currentTimeMillis();
				}
			}

			catch (StringIndexOutOfBoundsException e)
			{
				System.out.println("Gps parse error "+ e);

				// Get these on startup
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				System.out.println("Gps parse error"+ e);

				// Get these on startup
			}
			catch (NumberFormatException e)
			{
				System.out.println("Gps parse error" + e);

				// Get these on startup
			}
			catch (Throwable e)
			{
				System.out.println("Exception with GPS serial interface - f_gps_serialPort=" + e);

				serialPort = null;
				is = null;
				os = null;
				SleepUtil.sleepSeconds(1, this.getClass().getName());
			}			
		}
	}

	private void sendConfig(SerialOutputStream os) throws IOException
	{
		//        System.out.println("CONFIG " + BitUtils.bytesToHexString(sSetDynamicModeAirplaneLessThan2G.getBytes()));

		for (Byte b : sSetDynamicModeAirplaneLessThan2G.getBytes())
		{
			os.write(b);
		}

	}

}