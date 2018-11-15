package Bluetooth;

import javax.bluetooth.BluetoothStateException;
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
	final Object lock = new Object();
	private static Vector<RemoteDevice> mDevices = new Vector(); //vector containing the devices discovered
	private static String connectionURL = null;
	private SPPClient mClient;

	public BluetoothManager() {
		
		try {	
			this.mLocalDevice = LocalDevice.getLocalDevice();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void sendStartCommand() {
		mClient.sendCommand("Start");
		
	}
	
	public void sendStopCommand() {
		mClient.sendCommand("Stop");
		
	}
	
	public void connectCommand() {
		connect(mRemoteDevice, mAgent, this);
		
	}
	
	public void start() throws IOException {
	
		System.out.println("Local Bluetooth Address: " + mLocalDevice.getBluetoothAddress());
		System.out.println("Name: " + mLocalDevice.getFriendlyName());
		
		//get discovery agent
		mAgent = mLocalDevice.getDiscoveryAgent();
		//Limited Dedicated Inquiry Access Code (LIAC)
		mAgent.startInquiry(DiscoveryAgent.LIAC, this);
		
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

				}
			}
		}
	}
	
	/**
	 * Called when a remote device OnSite_BLT_Adapter is found. Searches for service on that device to connect to.
	 * @param remoteDevice - the Onsite Bluetooth Adapter
	 * @param agent - local devices discovery agent
	 * @param client - this
	 */
	public void connect(RemoteDevice remoteDevice, DiscoveryAgent agent, BluetoothManager client) {
		
		UUID[] uuidSet = new UUID[1];
        uuidSet[0]=new UUID("0003000000001000800000805F9B34FB", false);
        int[] attrIds = { 0x0003 };
        System.out.println("\nSearching for service...");
        
        try {
        	synchronized(lock) {
        		agent.searchServices(attrIds, uuidSet, remoteDevice, client);

        		lock.wait();
        	}
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        if(connectionURL == null){
            System.out.println("Device does not support Simple SPP Service.");
            System.exit(0);
        }
	}
	
	//BLUECOVE CALLBACKS
	
	/**
	 * This call back method will be called for each discovered bluetooth devices.
	 * Each device added to device vector.
	 * @param btDevice - The Remote Device discovered.
	 * @param cod - The class of device record. Contains information on the bluetooth device.
	 * 
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		System.out.println("Device discovered: "+ btDevice.getBluetoothAddress());
		//add the device to the vector
		if(!mDevices.contains(btDevice)){
			mDevices.addElement(btDevice);
		}

	}
	
	/**
	 * This callback with be called when services found by DiscoveryListener during service search
	 * @param - transID: the transaction ID of the service search that is posting the result.
	 * @param - servRecord: a list of services found during the search request.
	 */
		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

			System.out.println("Service discovered");
			System.out.println(servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
			//URL needed for connection to android bluetooth server
			connectionURL = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			
			//Creates client running on new thread on specified url
			mClient = new SPPClient(connectionURL);
			mClient.start();
			System.out.println("Client started");
			
		}

		/**
		 * Called when service search completed
		 * @param - transID - the transaction ID identifying the request which initiated the service search
		 * @param - respCode - the response code that indicates the status of the transaction
		 */
		public void serviceSearchCompleted(int transID, int respCode) {

			synchronized(lock) {
				lock.notifyAll();
			}
			switch (respCode) {
			case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
				System.out.println("SERVICE_SEARCH_COMPLETED");
				break;
		
			case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
				System.out.println("SERVICE_SEARCH_TERMINATED");
				break;
		
			case DiscoveryListener.SERVICE_SEARCH_ERROR:
				System.out.println("SERVICE_SEARCH_ERROR");
				break;
				
			case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
				System.out.println("SERVICE_SEARCH_NO_RECORDS");
				break;
				
			case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
				System.out.println("SERVICE_SEARCH_DEVICE_NOT_REACHABLE");
				break;
		
			default :
				System.out.println("Unknown Response Code");
				break;
			}
		}


	/**
	 * This callback method will be called when the device discovery is
	 * completed.
	 */
	public void inquiryCompleted(int discType) {
		synchronized(lock){
			lock.notifyAll();
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