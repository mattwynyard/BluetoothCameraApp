package Bluetooth;

//import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import java.io.IOException;
import java.util.Vector;

public class BluetoothManager implements DiscoveryListener {
	
	private LocalDevice mLocalDevice;
	RemoteDevice remoteDevice;
	private DiscoveryAgent mAgent;
	//private BluetoothManager bluetoothManager = new BluetoothManager();
	private static Object lock=new Object();
	//vector containing the devices discovered
	private static Vector<RemoteDevice> vecDevices = new Vector();
	
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
		int deviceCount = vecDevices.size();

		if(deviceCount <= 0) {
			System.out.println("No Devices Found .");
		} else {
			//print bluetooth device addresses and names in the format [ No. address (name) ]
			System.out.println("Bluetooth Devices: ");
			for (int i = 0; i < deviceCount; i++) {
				RemoteDevice remoteDevice = (RemoteDevice)vecDevices.elementAt(i);
				System.out.println((i+1) + ". " + remoteDevice.getBluetoothAddress() + " ("+remoteDevice.getFriendlyName(true) + ")");
			}
		}
	}
	
	/**
	 * This call back method will be called for each discovered bluetooth devices.
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		System.out.println("Device discovered: "+ btDevice.getBluetoothAddress());
		//add the device to the vector
		if(!vecDevices.contains(btDevice)){
			vecDevices.addElement(btDevice);
		}
	}
	
	//no need to implement this method since services are not being discovered
		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		}

		//no need to implement this method since services are not being discovered
		public void serviceSearchCompleted(int transID, int respCode) {
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