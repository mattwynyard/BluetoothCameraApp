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
		// TODO Auto-generated constructor stub
		this.connectionURL = connectionURL;
		try {
			mStreamConnection = (StreamConnection) Connector.open(connectionURL);
			if (mStreamConnection != null) {
				connected = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Hello from thread");
		if (connected) {
			System.out.println("Connection succesful...");
		}
		try {
			out = mStreamConnection.openOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(out));
			new Thread(readFromServer).start();
			writer.write("START");
			writer.flush();
			
	        //os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
                    if (buffer.toString().contains("NOTRECORDING")) {
                        CameraApp.isRecording(false);
                    } else if (buffer.toString().contains("RECORDING")) {
                        CameraApp.isRecording(true);
                    } else if (buffer.toString().contains("CONNECTED")) {
                        CameraApp.setConnected(true);
                    } else if (buffer.toString().contains("HOME:")) {
                        if (buffer.toString().contains("DESTROYED")) {
                            System.out.println(buffer.toString());
                            CameraApp.setConnected(false);
                        }
                    } else if (buffer.toString().contains(".jpg")) {
                        //System.out.println(buffer.toString());
                        CameraApp.setPhotoLabel(buffer.toString().substring(12));
                    } else if (buffer.toString().contains("B:")) {
                        //System.out.println(buffer.toString());
                        CameraApp.setBatteryLabel(buffer.toString().substring(2));
                    } else if (buffer.toString().contains("M:")) {
                        //System.out.println(buffer.toString());
                        CameraApp.setMemoryLabel(buffer.toString().substring(2));
                    } else {
                        System.out.println(buffer.toString());
                    }   
                }     

            } catch (IOException e) {
                try {
                    //socket.close();
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    };
	
} //end class
