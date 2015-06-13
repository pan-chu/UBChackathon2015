//Modified by Brittaney Geisler March 2015

package com.bitalino.deviceandroid;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import ceu.marten.ui.HomeActivity;
import ceu.marten.ui.NewRecordingActivity;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoException;
import com.bitalino.comm.BITalinoFrame;
import com.ubc.capstonegroup70.PatientSessionActivity;

/**
 * Wrapper for the Java SDK of Bitalino by ppires
 * 
 * @author David Gonzalez
 * 
 */


public class BitalinoAndroidDevice{
	private String remoteDeviceMAC = "98:D3:31:B2:BD:41";
	//public static String btName = "defualt";
	private int sampleRate = 1000;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	BITalinoDevice bitalino;
	
	public BitalinoAndroidDevice(String remoteDeviceMAC) {
		super();
		this.remoteDeviceMAC=remoteDeviceMAC;		
	}
	
	public int start(){
        try {
			bitalino.start();
		} catch (BITalinoException e) {
			e.printStackTrace();
			return -1;
		}
        return 0;
	}
	
	public BITalinoFrame[] read(int numberOfSamplesToRead){
		BITalinoFrame[] frames = null;
		try {
			frames = bitalino.read(numberOfSamplesToRead);
		} catch (BITalinoException e) {
			e.printStackTrace();
			return null;
		}
		return frames;
	}
    
	public int stop(){
		try {
			bitalino.stop();
		} catch (BITalinoException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public int connect(int sampleRate, int[] activeChannelsArray) {		
		this.sampleRate=sampleRate;
		final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();	
		BluetoothDevice dev = null; //= btAdapter.getRemoteDevice(remoteDeviceMAC);
		//BluetoothDevice dev = btAdapter.getRemoteDevice("bitalino");
		
		//START OF MY CODE
		//BluetoothDevice dev;
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
	    if(pairedDevices.size() > 0)
	    {
	        for(BluetoothDevice device : pairedDevices)
	        {
	            if(device.getName().equals(HomeActivity.btName)) //HomeActivity.btName//98:D3:31:B2:BD:55//EMG_Sensor//NewConfigurationActivity.macAdd
	            {
	                dev = device;
	                
	                //START
	        		btAdapter.cancelDiscovery();
	        		BluetoothSocket sock;
	        		try {
	        			sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
	        			sock.connect();

	        		} catch (IOException e) {
	        			e.printStackTrace();
	        			return -1;
	        		}
	        		
	        		try {
	        			bitalino = new BITalinoDevice(sampleRate, activeChannelsArray);
	        		} catch (BITalinoException e) {
	        			e.printStackTrace();
	        			return -2;
	        		}
	        		try {
	        			bitalino.open(sock.getInputStream(), sock.getOutputStream());
	        		} catch (BITalinoException | IOException e) {
	        			e.printStackTrace();
	        		}
	        		//END
	                return 0;
	                //break;
	            }
	            //else Toast.makeText(this, NewConfigurationActivity.macAddress.getText().toString(), Toast.LENGTH_SHORT).show();
	        }
	    }
		//END OF MY CODE
		
		/*btAdapter.cancelDiscovery();
		BluetoothSocket sock;
		try {
			sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
			sock.connect();

		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
		try {
			bitalino = new BITalinoDevice(sampleRate, activeChannelsArray);
		} catch (BITalinoException e) {
			e.printStackTrace();
			return -2;
		}
		try {
			bitalino.open(sock.getInputStream(), sock.getOutputStream());
		} catch (BITalinoException | IOException e) {
			e.printStackTrace();
		}*/

	    NewRecordingActivity.btConnectError = true;
		return 1;
	}
	
	

}
