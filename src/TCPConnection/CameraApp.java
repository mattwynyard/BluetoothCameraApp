/**
 * Copyright 2018 - Onsite Developments
 * @author Matt Wynyard November 2018
 * @version 1.1
 */

package TCPConnection;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import Bluetooth.BluetoothManager;
//import TCPConnection.HTTPServer;

/**
 * Main application class for CameraApp
 * Builds user interface and intialises a bluetooth connection to Android App via the custom class Bluetooth Manager
 *
 *
 */
public class CameraApp {

    //text labels
    private static JLabel statusText;
    private static JLabel cameraText;
    private static JLabel photoText;
    private static JLabel memoryText;
    private static JLabel ipText;
    private static JLabel batteryText;

    //message labels
    private static JLabel statusLabel;
    private static JLabel cameraLabel;
    private static JLabel photoLabel;
    private static JLabel batteryLabel;
    private static JLabel memoryLabel;
    private static JLabel ipLabel;

    //buttons
	private static JButton startButton;
    private static JButton stopButton;
    private static JButton connectButton;

    //constants
    public static final Color DARK_GREEN = new Color(0,153,0);
    private static final Insets insets = new Insets(0, 0, 0, 0);

    private Scanner sc;

    private static String ip;
    //public static final CameraApp App = ;
    public static final CameraApp App = new CameraApp();
    private static boolean connected = false;
    private static boolean recording = false;
    private static String status;
    private static boolean DEBUG = true;
    private static String mode;
    private static BluetoothManager mBluetooth;
    private static TCPServer mTCP;
    private static int flag = 0;
    
    public static void main(String[] args) throws IOException {

        mode = "Bluetooth";
        ipLabel.setText(mode);
//        if (DEBUG) {
//            System.out.println("starting ADB");
//            App.startAdb();
//        }
        
        mBluetooth = new BluetoothManager();
        new Thread(Flash).start();
        mBluetooth.start();

        //final Thread Shutdown = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Flash = null;
                mBluetooth.mClient.mTCP.sendData("NOTRECORDING");
                mBluetooth.mClient.mTCP.sendData("NOTCONNECTED");
                mBluetooth.mClient.mTCP.sendData("ERROR");
                mBluetooth.mClient.mTCP.closeAll();
                mBluetooth.mClient.closeAll();

            }
        });

    }

    private static Runnable Flash = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (flag == 0 && recording == false) {
                    cameraLabel.setForeground(Color.YELLOW);
                    cameraLabel.setBackground(Color.RED);
                    flag = 1;
                    try {
                        Thread.sleep(1000);
                        //System.out.println("Thread sleeping");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (flag == 1 && recording == false) {
                    cameraLabel.setForeground(Color.RED);
                    cameraLabel.setBackground(Color.YELLOW);
                    flag = 0;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    };

    public static void setStatusLabel(String label) {
        connected = false;
        statusLabel.setText(label);
    }

    public static void setIPLabel(String label) {
        ipLabel.setText(label);
    }

    public static void setMemoryLabel(String label) {
        memoryLabel.setText(label);
    }

    public static void setPhotoLabel(String label) {
        photoLabel.setText(label);
    }

    public static void setBatteryLabel(String label) {
        batteryLabel.setText(label);
    }

    public static void setConnected(boolean state) {
        connected = state;
        if (connected == true) {
            statusLabel.setText("Connected");
            statusLabel.setForeground(DARK_GREEN);
            //ipLabel.setText(mClient.getIP());
        } else {
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.red);
            //mClient = null;
            ipLabel.setText("___.___.___.___");
        }
    }

    public synchronized static void setStatus(String state) {
        status = state;
        if (status.equals("CONNECTED")) {
            statusLabel.setText("Connected");
            statusLabel.setForeground(DARK_GREEN);
        } else if (status.equals("NOTCONNECTED")) {
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.red);
            //ipLabel.setText(mode);
        } else if (status.equals("SEARCHING")) {
            statusLabel.setText("Searching...");
            statusLabel.setForeground(Color.BLUE);
        } else if (state.equals("DISCOVERING")) {
            statusLabel.setText("Discovering...");
            statusLabel.setForeground(Color.BLUE);
        } else if (state.equals("NODEVICES")) {
            statusLabel.setText("No Devices Found");
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setText(state);
            statusLabel.setForeground(Color.RED);
        }
    }

    public synchronized static void setRecording(boolean state) {
        recording = state;
        if (recording == true) {
            cameraLabel.setText("Recording");
            cameraLabel.setForeground(DARK_GREEN);
            cameraLabel.setOpaque(false);
        } else {
            cameraLabel.setText("Not Recording");
            cameraLabel.setOpaque(true);
            cameraLabel.setForeground(Color.red);
            cameraLabel.setBackground(Color.YELLOW);
        }
    }

    private ActionListener startAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			mBluetooth.sendStartCommand();
			
		}
	};

	private ActionListener stopAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			mBluetooth.sendStopCommand();
		}
    };

    private ActionListener connectAction = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Status: " + status);
            if (status.equals("NOTCONNECTED") || status.equals("NODEVICES")) {
                mBluetooth = null;
                mBluetooth = new BluetoothManager();
                mBluetooth.start();
            }
        }
    };

    private static void addComponent(Container container, Component component, int gridx, int gridy,
      int gridwidth, int gridheight, double weightx, double weighty, int anchor, int fill, int ipadx, int ipady) {
        
        GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty,
        anchor, fill, insets, ipadx, ipady);
        container.add(component, gbc);
    }
    
	private CameraApp() {

        JFrame frame = new JFrame();
		//make sure the program exits when the frame closes
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("OnSite Recording Controller");
		frame.setLayout(new GridBagLayout());
		//This will center the JFrame in the middle of the screen
        frame.setLocationRelativeTo(null);
    
        ipText = new JLabel("Mode:");
        addComponent(frame, ipText, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        statusText = new JLabel("Status:");
        addComponent(frame, statusText, 2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        cameraText = new JLabel("Camera:");
        addComponent(frame, cameraText, 4, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        photoText = new JLabel("Photo:");
        addComponent(frame, photoText, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        memoryText = new JLabel("Memory:");
        addComponent(frame, memoryText, 2, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        batteryText = new JLabel("Battery:");
        addComponent(frame, batteryText, 4, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);

        ipLabel = new JLabel();
        ipLabel.setText(mode);
        addComponent(frame, ipLabel, 1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.red);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        //statusLabel.setPreferredSize(new Dimension(120, 20));
        addComponent(frame, statusLabel, 3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        cameraLabel = new JLabel("Not Recording");
        cameraLabel.setForeground(Color.red);
//        cameraLabel.setBackground(Color.yellow);
        cameraLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cameraLabel.setOpaque(true);
        //cameraLabel.setPreferredSize(new Dimension(80, 20));
        addComponent(frame, cameraLabel, 5, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        photoLabel = new JLabel("Not available");
        //photoLabel.setPreferredSize(new Dimension(120, 20));
        addComponent(frame, photoLabel, 1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        memoryLabel = new JLabel("Not available");
        //memoryLabel.setPreferredSize(new Dimension(20, 20));
        addComponent(frame, memoryLabel, 3, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);
        batteryLabel = new JLabel("Not available");
        //memoryLabel.setPreferredSize(new Dimension(20, 20));
        addComponent(frame, batteryLabel, 5, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 0);

        startButton = new JButton("Start Recording");
        addComponent(frame, startButton, 2, 2, 2, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 0, 0);
        //startButton.setPreferredSize(new Dimension(150, 55));
        stopButton = new JButton("Stop Recording");
        addComponent(frame, stopButton, 4, 2, 2, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 0, 0);
        // stopButton.setPreferredSize(new Dimension(150, 55));
        connectButton = new JButton("Connect");
        addComponent(frame, connectButton, 0, 2, 2, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 0, 0);
        // stopButton.setPreferredSize(new Dimension(150, 55));
		
        startButton.addActionListener(startAction);
        stopButton.addActionListener(stopAction);
        connectButton.addActionListener(connectAction);
        frame.setSize(480, 180);
        frame.setResizable(false);   
        //frame.pack();
		frame.setVisible(true);
    }

//    /**
//     * Execute a command
//     */
//    private static String executeCommand(String command) {
//		StringBuffer sb = new StringBuffer();
//        //String[] commands = new String[]{"/bin/sh","-c", command};
//        try {
//            Process proc = Runtime.getRuntime().exec(command);
//            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
//            String s = null;
//            while ((s = stdInput.readLine()) != null) 
//            {
//                sb.append(s);
//                sb.append("\n");
//            }
//            while ((s = stdError.readLine()) != null) 
//            {
//                sb.append(s);
//                sb.append("\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//		return sb.toString();
//    }
    
//    /**
//     * Start the adb bridge
//     */
//    private void startAdb() {
//		try {
//            Process process=Runtime.getRuntime().exec("C:\\and\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");
//			sc = new Scanner(process.getErrorStream());
//			if (sc.hasNext()) 
//			{
//				while (sc.hasNext()) 
//					System.out.print(sc.next()+" ");
//				System.out.println("\nCannot start the Android debug bridge");
//			}
//			sc.close();
//		} catch (IOException ioEx) {
//			System.out.println("\nCannot start the Android debug bridge, IOException");
//		}
//    }
}