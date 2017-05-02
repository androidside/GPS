
package originalReaderUblox;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import gov.nasa.gsfc.aurora.bettii.configuration.BettiiPrefs;
import gov.nasa.gsfc.aurora.bettii.protobuf.BettiiTelemetryMessageProtobuf.GpsReadings;
import gov.nasa.gsfc.aurora.bettii.protobuf.FordCommandablesProtobuf.FordCommandables;
import gov.nasa.gsfc.aurora.bettii.protobuf.TelemetryMiscSettingsProtobuf.TelemetryMiscSettings;
import gov.nasa.gsfc.aurora.common.component.Actor;
import gov.nasa.gsfc.aurora.common.component.ActorMessageDispatcher;
import gov.nasa.gsfc.aurora.common.hardware.NMEAParser;
import gov.nasa.gsfc.aurora.common.net.SerialInputStream;
import gov.nasa.gsfc.aurora.common.net.SerialOutputStream;
import gov.nasa.gsfc.aurora.common.util.AuroraEventBus;
import gov.nasa.gsfc.aurora.common.util.AuroraFileUtils;
import gov.nasa.gsfc.aurora.common.util.SleepUtil;
import gov.nasa.gsfc.aurora.configuration.AllProjPrefs;
import gov.nasa.gsfc.aurora.threads.CaughtThread;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Reads NMEA GPS over serial.  Also configures Airplane<2G for ublox chip
 * <P>This code was developed by NASA, Goddard Space Flight Center, Code 665.
 * 
 * @version	    $Date: 2010/05/26 16:18:34 $
 * @author      smaher
 *
 */
@Singleton
public class GpsReaderUblox
{
    /**
    * Logger for this class
    */

    private static final Logger sLogger = Logger.getLogger(GpsReaderUblox.class.getName());

    private static final String PORT_FOR_MAIN = "COM4";
//    private static final String PORT_FOR_MAIN = "/dev/bettiiGPS";

    private static final long CONFIG_SEND_PERIOD_MILLIS = 2000;

    private static final long PC_TIME_SET_THRESH_MILLIS = 5000;

    /**
     * ublox command to set dynamic mode to Airplane < 2G.  This is needed
     * to support > 12km alt.
     */
    private static String sSetDynamicModeAirplaneLessThan2G = String.valueOf(new char[] {0xB5, 0x62, 0x06, 0x24, 0x24, 0x00, 0xFF, 0xFF,
            0x07, 0x03, 0x00, 0x00, 0x00, 0x00, 0x10, 0x27, 0x00, 0x00, 0x05, 0x00, 0xFA, 0x00, 0xFA, 0x00, 0x64, 0x00, 0x2C, 0x01, 0x00,
            0x3C, 0x00, 0x00, 0x00, 0x00, 0xC8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B, 0x4A});

    private final String fBettii_gps_serialPort;

    private volatile boolean fEnableGpsForStarFinder = true;

    private long fPrevPcTimeMillis;
    private volatile long fPrevGpsTimeMillis;
    private volatile long fPrevPCTimeSettingMillis;

    private final int fBettii_localUtcOffsetHours;

    private final double fBettii_localLonDegrees;

    private final double fBettii_localLatDegrees;

    private final double fBettii_staticAltMeters;

    @Inject
    public GpsReaderUblox(AllProjPrefs prefs)
    {
        if (prefs == null)
        {
            fBettii_gps_serialPort = PORT_FOR_MAIN;
            fBettii_localUtcOffsetHours = 0;
            fBettii_localLatDegrees = 0;
            fBettii_localLonDegrees = 0;
            fBettii_staticAltMeters = 0;

        }
        else
        {
            fBettii_localUtcOffsetHours = ((BettiiPrefs) prefs.get()).getBettii_localUtcOffsetHours();
            fBettii_localLatDegrees = ((BettiiPrefs) prefs.get()).getBettii_staticLatDegrees();
            fBettii_localLonDegrees = ((BettiiPrefs) prefs.get()).getBettii_staticLonDegrees();
            fBettii_staticAltMeters = ((BettiiPrefs) prefs.get()).getBettii_staticAltMeters();
            fBettii_gps_serialPort = ((BettiiPrefs) prefs.get()).getBettii_gps_serialPort();
        }

        new ActorMessageDispatcher<TelemetryMiscSettings>(new Actor<TelemetryMiscSettings>()
        {
            @Override
            public void processActorMessage(TelemetryMiscSettings mesg)
            {
                if (mesg.hasEnableGpsForStarFinder() == true)
                {
                    fEnableGpsForStarFinder = mesg.getEnableGpsForStarFinder();
                }
            }
        }, TelemetryMiscSettings.class, this.getClass().getSimpleName(), 5);

        new CaughtThread(this.getClass().getName(), new Runnable()
        {

            @Override
            public void run()
            {
                sLogger.logp(Level.INFO, "GpsReaderUbloxMainRead", "$Runnable.run()", "Starting GpsReader");
                readAndPublishForever();
            }
        }).start();
        
        new CaughtThread(this.getClass().getName(), new Runnable()
        {

            @Override
            public void run()
            {
                sLogger.logp(Level.INFO, "GpsReaderUbloxPcBackup", "$Runnable.run()", "Starting PcBackup");
                pcBackupLoop();
            }
        }).start();
    }

    protected void pcBackupLoop()
    {
        while (true)
        {
            try
            {
                if (fEnableGpsForStarFinder == true)
                {

                    if (System.currentTimeMillis() - fPrevGpsTimeMillis > 3000 && System.currentTimeMillis() - fPrevPcTimeMillis > 1000)
                    {
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date());
                        float gpsTimeStamp = (c.get(Calendar.HOUR_OF_DAY) + fBettii_localUtcOffsetHours) * 10000 + c.get(Calendar.MINUTE) * 100 + c.get(Calendar.SECOND)
                                + c.get(Calendar.MILLISECOND) / 1000.0f;

                        AuroraEventBus.publish(FordCommandables.newBuilder()
                                .setCardiffLatDegrees((float) fBettii_localLatDegrees)
                                .setGpsLonDeg((float) fBettii_localLonDegrees)
                                .setGpsUtcTime((float) gpsTimeStamp)
                                .setGpsAltitudeMeters((float) fBettii_staticAltMeters)
                                .setGpsLocked(false)
                                .setWrite(true)
                                .build());
                        
                        fPrevPcTimeMillis = System.currentTimeMillis();
                    }
                    else
                    {
                        SleepUtil.sleepSeconds(1, "PcBackupLoop");
                    }
                }
            }
            catch (Throwable e)
            {
                SleepUtil.sleepSeconds(1, "PcBackupLoop");
            }
        }
        
    }

    private SerialPort connect(String portName) throws SerialPortException
    {
        SerialPort sp = new SerialPort(portName);
        sp.openPort();//Open serial port
        sp.setParams(9600, 8, 1, 0, false, false);
        return sp;
    }

    public static void main(String[] args) throws Exception
    {
        GpsReaderUblox gpsReaderUblox = new GpsReaderUblox(null);
        gpsReaderUblox.readAndPublishForever();
    }

    /**
         * @param throwExceptionIfNotOpen TODO
     * @throws SerialPortException 
         * @throws IOException  */
    public synchronized void readAndPublishForever()
    {
        SerialPort serialPort = null;
        NMEAParser parser = null;
        SerialInputStream is = null;
        SerialOutputStream os = null;

        long prevConfigSendMillis = 0;
        while (true)
        {
            try
            {
                if (is == null || os == null || serialPort == null)
                {

                    serialPort = connect(fBettii_gps_serialPort);
                    is = new SerialInputStream(serialPort);
                    os = new SerialOutputStream(serialPort);
                    parser = new NMEAParser();
                }

                String readCRLFLine = AuroraFileUtils.readCRLFLine(is);
                com.utils.GPS.aurora.common.hardware.NMEAParser.GPSPosition parse = parser.parse(readCRLFLine);
                if (parse != null)
                {
                    System.out.println("NMEA " + parse);
                    int hours = (int) (parse.time / 10000);
                    int minutes = (int) ((parse.time - hours * 10000) / 100);
                    float seconds = parse.time - hours * 10000 - minutes * 100;

                    GpsReadings gpsReadings = GpsReadings.newBuilder()
                            .addAltitudeMeters(parse.altitude)
                            .addLatitudeDegrees(parse.lat)
                            .addLongitudeDegrees(parse.lon)
                            .addHourUTC(hours)
                            .addMinuteUTC(minutes)
                            .addSecondUTC(seconds)
                            .build();

                    AuroraEventBus.publish(gpsReadings);

                    if (System.currentTimeMillis() - fPrevPCTimeSettingMillis > PC_TIME_SET_THRESH_MILLIS)
                    {
//                        String setTimeCommand = "/bin/date +%T -s \"" + hours + ":" + minutes + ":" + seconds + "\"";
//                        try
//                        {
//                            Runtime.getRuntime().exec(setTimeCommand);
//                        }
//                        catch (Exception e)
//                        {
//                            sLogger.logp(Level.WARNING, "GpsReaderUblox", "readAndPublishForever()", "exception setting time: " + setTimeCommand, e);
//
//                        }
                        fPrevPCTimeSettingMillis = System.currentTimeMillis();
                    }
                    fPrevGpsTimeMillis = System.currentTimeMillis();
                    
                    if (fEnableGpsForStarFinder == true)
                    {
                        /*
                         * Publish a FordCommandable to be used to set Cardiff settings
                         */
                        AuroraEventBus.publish(FordCommandables.newBuilder()
                                .setCardiffLatDegrees(parse.lat)
                                .setGpsLonDeg(parse.lon)
                                .setGpsUtcTime(parse.time)
                                .setGpsAltitudeMeters(parse.altitude)
                                .setGpsLocked(true)
                                .setWrite(true)
                                .build());
                    }
                }


                /*
                 * It's easier to just continue to send this then
                 * try and parse the current state.  Is has been tested to work several times.
                 */
                if (System.currentTimeMillis() - prevConfigSendMillis > CONFIG_SEND_PERIOD_MILLIS)
                {
                    sendConfig(os);
                    prevConfigSendMillis = System.currentTimeMillis();
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
                sLogger.logp(Level.WARNING, "GpsReaderUblox", "readAndPublishForever()", "Gps parse error", e);

                // Get these on startup
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                sLogger.logp(Level.WARNING, "GpsReaderUblox", "readAndPublishForever()", "Gps parse error", e);

                // Get these on startup
            }
            catch (NumberFormatException e)
            {
                sLogger.logp(Level.WARNING, "GpsReaderUblox", "readAndPublishForever()", "Gps parse error", e);

                // Get these on startup
            }
            catch (Throwable e)
            {
                sLogger.logp(Level.WARNING, "GpsReaderUblox", "readAndPublishForever()",
                        "Exception with GPS serial interface - fBettii_gps_serialPort=" + fBettii_gps_serialPort, e);

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
