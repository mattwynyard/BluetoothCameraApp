package Bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import TCPConnection.CameraApp;

public class SPPClient extends Thread {
	
	private String connectionURL;
	private boolean connected = false;
	private StreamConnection mStreamConnection;
	private OutputStream out;
	private InputStream in;
	private PrintWriter writer;
	private BufferedReader reader;
	
	public SPPClient(String connectionURL) {
		this.connectionURL = connectionURL;
		try {
			mStreamConnection = (StreamConnection) Connector.open(connectionURL);
			if (mStreamConnection != null) {
				connected = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void sendCommand(String command) {
        writer.println(command);
        writer.flush();
        try {
            Thread.sleep(200);
        } catch(Exception e) {
            e.printStackTrace();
        }		
    }

	public void run() {
		System.out.println("Hello from thread");
		if (connected) {
			System.out.println("Connection succesful...");
		}
		try {
		    //TODO Error javax.bluetooth.BluetoothConnectionException: Failed to connect; [10048]
            // Only one usage of each socket address (protocol/network address/port) is normally permitted.
			out = mStreamConnection.openOutputStream(); //can cause null pointer exception in Thread-2
			writer = new PrintWriter(new OutputStreamWriter(out));
			new Thread(readFromServer).start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e1) {
		    e1.printStackTrace();
            System.exit(0);
        }
	}
	/**
     * Runnable that will read from the server on a thread
     */
    private Runnable readFromServer = new Runnable() {
        @Override
        public void run() {
            try {
                String buffer;
                System.out.println("Reading From Server"); 
                in = mStreamConnection.openInputStream();
                reader = new BufferedReader(new InputStreamReader(in));

                while ((buffer=reader.readLine())!=null) {
                    if (buffer.contains("NOTRECORDING")) {
                        CameraApp.setRecording(false);
                    } else if (buffer.contains("RECORDING")) {
                        CameraApp.setRecording(true);
                    } else if (buffer.contains("CONNECTED")) {
                        CameraApp.setStatus("CONNECTED");
                    } else if (buffer.contains("HOME:")) {
                        if (buffer.contains("DESTROYED") || buffer.contains("DETACHED")) {
                            System.out.println(buffer);
                            CameraApp.setStatus("NOTCONNECTED");
                            CameraApp.setRecording(false);
                        }
                    } else if (buffer.contains(".jpg")) {
                        System.out.println(buffer.toString());
                        CameraApp.setPhotoLabel(buffer.substring(12));
                    } else if (buffer.contains("B:")) {
                        System.out.println(buffer);
                        CameraApp.setBatteryLabel(buffer.substring(2));
                    } else if (buffer.contains("M:")) {
                        System.out.println(buffer);
                        CameraApp.setMemoryLabel(buffer.substring(2));
                    } else {
                        System.out.println(buffer);
                    }   
                }
            } catch (IOException e) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    };
} //end class
