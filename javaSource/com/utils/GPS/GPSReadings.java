package com.utils.GPS;
//https://store.uputronics.com/?route=product/product&product_id=83 gps Model uBLOX MAX-M8Q SAW/LNA Breakout With Sarantel Antenna 5V 
//http://gpsinformation.net/main/gpslock.htm
public class GPSReadings {
	
		public float latitudeDegrees;
		public float longitudeDegrees;
		public float altitudeMeters;
		public int hourUTC;
		public int minuteUTC;
		public float secondUTC;
		
		public void printGpsReadings(GPSReadings gpsReadings){
			
			System.out.println("Time : "+hourUTC+":"+minuteUTC+":"+secondUTC);
			
			
			
		}
	

}
