package com.utils.serial;

import java.io.IOException;
import java.io.OutputStream;

public class SerialWriter implements Runnable {

	OutputStream out;
	int[] fCommandHolder = new int[50];
	int fCommandIndex=0;

	public SerialWriter( OutputStream out ) {
		this.out = out;
	}

	public void run() {
		try {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Write commands (max. 50 chars)...");
			int c = 0;
			while( ( c = System.in.read() ) >= -1 ) {
				
				if(c==13){
					
					//Send command.. do we need to include '/n' ?
					this.out.write( c );
					
					System.out.println("Command Written");
					System.out.flush();
					//Reset fCommandHolder for next command 
					fCommandIndex = 0;
					fCommandHolder	 = new int[50];
				}

				else{

					fCommandHolder[fCommandIndex] = c;
					fCommandIndex++;
				}
				
				
			}
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}
}