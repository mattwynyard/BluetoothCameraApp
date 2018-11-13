package Bluetooth;

import javax.bluetooth.BluetoothStateException;
//import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.bluetooth.DataElement;

public class BluetoothManager implements DiscoveryListener {
	
	private LocalDevice mLocalDevice;
	private RemoteDevice mRemoteDevice;
	private DiscoveryAgent mAgent;
	//private BluetoothManager bluetoothManager = new BluetoothManager();
	private static Object lock=new Object();
	//vector containing the devices discovered
	private static Vector<RemoteDevice> mDevices = new Vector();
	private static String connectionURL = null;
	//int[] attrIds = { 0x0100 };
	
	public BluetoothManager() {
		
		try {	
			this.mLocalDevice = LocalDevice.getLocalDevice();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void start() throws IOException {
	
		System.out.println("Local Bluetooth Address: " + mLocalDevice.getBluetoothAddress());
		System.out.println("Name: " + mLocalDevice.getFriendlyName());
		
		//get devices
		mAgent = mLocalDevice.getDiscoveryAgent();
		mAgent.startInquiry(DiscoveryAgent.LIAC, this);
		//SampleSPPClient client=new SampleSPPClient();
		
		try {
			synchronized(lock){
				lock.wait();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Device Inquiry Completed. ");

		//print all devices in vecDevices
		int deviceCount = mDevices.size();

		if(deviceCount <= 0) {
			System.out.println("No Devices Found .");
		} else {
			//print bluetooth device addresses and names in the format [ No. address (name) ]
			System.out.println("Bluetooth Devices: ");
			for (int i = 0; i < deviceCount; i++) {
				RemoteDevice remoteDevice = (RemoteDevice) mDevices.elementAt(i);
				System.out.println((i+1) + ". " + remoteDevice.getBluetoothAddress() + " ("+ remoteDevice.getFriendlyName(true) + ")");
				
				if (remoteDevice.getFriendlyName(true).equals("OnSite_BLT_Adapter")) {
					mRemoteDevice = (RemoteDevice) mDevices.elementAt(i);
					connect(mRemoteDevice, mAgent, this);
					
					//System.out.println("Attempting connection to: "+ remoteDevice.getFriendlyName(true));
				}
			}
		}
	}
	
	public void connect(RemoteDevice remoteDevice, DiscoveryAgent agent, BluetoothManager client) {
		
		UUID[] uuidSet = new UUID[1];
        uuidSet[0]=new UUID("0003000000001000800000805F9B34FB", false);
        int[] attrIds = { 0x0003 };
        System.out.println("\nSearching for service...");
        
        try {
			agent.searchServices(attrIds, uuidSet, remoteDevice, client);
		} catch (BluetoothStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
//        if(connectionURL == null){
//            System.out.println("Device does not support Simple SPP Service.");
//            System.exit(0);
//        }

        //connect to the server and send a line of text
        
		
	}
	
	/**
	 * This call back method will be called for each discovered bluetooth devices.
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		System.out.println("Device discovered: "+ btDevice.getBluetoothAddress());
		//add the device to the vector
		if(!mDevices.contains(btDevice)){
			mDevices.addElement(btDevice);
		}

	}
	
	//Need to implement this method since services are being discovered
		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

			System.out.println("Service discovered");
			System.out.println(servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
			connectionURL = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			
			try {
				StreamConnection streamConnection = (StreamConnection) Connector.open(connectionURL);
				if (streamConnection != null) {
					System.out.println("Connection succesful...");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        synchronized(lock){
	            lock.notify();
	        }
			
		}

		//Need to implement this method since services are not being discovered
		public void serviceSearchCompleted(int transID, int respCode) {
			synchronized(lock){
	            lock.notify();
	        }
		}


	/**
	 * This callback method will be called when the device discovery is
	 * completed.
	 */
	public void inquiryCompleted(int discType) {
		synchronized(lock){
			lock.notify();
		}
	
		switch (discType) {
		case DiscoveryListener.INQUIRY_COMPLETED :
			System.out.println("INQUIRY_COMPLETED");
			break;
	
		case DiscoveryListener.INQUIRY_TERMINATED :
			System.out.println("INQUIRY_TERMINATED");
			break;
	
		case DiscoveryListener.INQUIRY_ERROR :
			System.out.println("INQUIRY_ERROR");
			break;
	
		default :
			System.out.println("Unknown Response Code");
			break;
		}
	}//end method
} //end class