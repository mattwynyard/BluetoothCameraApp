import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
//import java.awt.GridBagLayout;
import java.awt.GridLayout;

public class CameraApp {

    private static JLabel statusLabel;
    private static JLabel cameraLabel;
    private static JLabel photoLabel;
    private static JLabel batteryLabel;
    private static JLabel memoryLabel;
	private static JButton startButton;
    private static JButton stopButton;

    private final JLabel statusText;
    private final JLabel cameraText;
    private final JLabel photoText;
    private final JLabel memoryText;
    public static final Color DARK_GREEN = new Color(0,153,0);

    private Scanner sc;

    private static Client mClient;
    private static String ip;
    public static CameraApp App;
    private static boolean connected = false;
    private static boolean recording = false;

    private static boolean DEBUG = true;
    
    public static void main(String[] args) {
   	
        App = new CameraApp();
        if (!DEBUG) {
            ArpUtility arp = new ArpUtility();
            ip = arp.getIPAddress();
        } else {
            ip = "localhost";
            App.startAdb();
        }
        if (ip != "error") {
            System.out.println("connecting to: " + ip);
            mClient = new Client(ip, 38300);
        } else {
            setStatusLabel("No Network");
        }
    }

    public static void setStatusLabel(String label) {
        connected = false;
        statusLabel.setText(label);
    }

    public static void setMemoryLabel(String label) {
        memoryLabel.setText(label);
    }

    public static void setPhotoLabel(String label) {
        photoLabel.setText(label);
    }

    public static void setConnected(boolean state) {
        connected = state;
        if (connected == true) {

            statusLabel.setText("Connected");
            statusLabel.setForeground(DARK_GREEN);
        } else {
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.red);
        }
    }

    public static void isRecording(boolean state) {
        recording = state;
        if (recording == true) {
            cameraLabel.setText("Recording");
            cameraLabel.setForeground(DARK_GREEN);
        } else {
            cameraLabel.setText("Not Recording");
            cameraLabel.setForeground(Color.red);
        }
    }

    private ActionListener startAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			//System.out.println("c: START ACTION");
			mClient.sendStartCommand();
		}
	};

	private ActionListener stopAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			//System.out.println("c: STOP ACTION");
			mClient.sendStopCommand();
		}
    };

	private CameraApp() {

        JFrame guiFrame = new JFrame();     
        JPanel guiPanel = new JPanel();
        JPanel textPanel = new JPanel(new GridLayout(0, 4));
		
		//make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("OnSite Recording Controller");
        guiFrame.setSize(400, 200);
		
		//This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
    
        statusText = new JLabel("Status:");
        statusText.setPreferredSize(new Dimension(20, 20));
        cameraText = new JLabel("Camera:");
        cameraText.setPreferredSize(new Dimension(20, 20));
        photoText = new JLabel("Photo:");
        photoText.setPreferredSize(new Dimension(20, 20));
        memoryText = new JLabel("Memory:");
        memoryText.setPreferredSize(new Dimension(20, 20));

        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.red);
        statusLabel.setPreferredSize(new Dimension(80, 20));
        cameraLabel = new JLabel("Not Recording");
        cameraLabel.setForeground(Color.red);
        cameraLabel.setPreferredSize(new Dimension(80, 20));
        photoLabel = new JLabel("no photo");
        photoLabel.setPreferredSize(new Dimension(120, 20));
        memoryLabel = new JLabel("");
        memoryLabel.setPreferredSize(new Dimension(20, 20));
        //photoLabel.setHorizontalAlignment(JLabel.LEFT);

		startButton = new JButton("Start Recording");
        startButton.setPreferredSize(new Dimension(150, 55));
        stopButton = new JButton("Stop Recording");
        stopButton.setPreferredSize(new Dimension(150, 55));
		
        startButton.addActionListener(startAction);
        stopButton.addActionListener(stopAction);
        
        textPanel.add(statusText);
        textPanel.add(statusLabel);
        textPanel.add(cameraText);
        textPanel.add(cameraLabel);
        textPanel.add(photoText);
        textPanel.add(photoLabel);
        textPanel.add(memoryText);
        //textPanel.add(memoryLabel);
		guiPanel.add(startButton);
        guiPanel.add(stopButton);
		     
        guiFrame.add(textPanel, BorderLayout.NORTH);
        guiFrame.add(guiPanel, BorderLayout.SOUTH);
        guiFrame.pack();
		guiFrame.setVisible(true);
    }

    /**
     * Execute a command
     */
    private String executeCommand(String command) {
		StringBuffer sb = new StringBuffer();
        String[] commands = new String[]{"/bin/sh","-c", command};
        try {
            Process proc = new ProcessBuilder(commands).start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) 
            {
                sb.append(s);
                sb.append("\n");
            }
            while ((s = stdError.readLine()) != null) 
            {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return sb.toString();
    }
    
    /**
     * Start the adb bridge
     */
    private void startAdb() {
		try {
            //Process process=Runtime.getRuntime().exec("C:\\and_tools\\platform-tools-latest-windows\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");
            Process process=Runtime.getRuntime().exec("C:\\and\\platform-tools\\adb.exe forward tcp:38300 tcp:38300");
			sc = new Scanner(process.getErrorStream());
			if (sc.hasNext()) 
			{
				while (sc.hasNext()) 
					System.out.print(sc.next()+" ");
				System.out.println("\nCannot start the Android debug bridge");
			}
			sc.close();
		} catch (IOException ioEx) {
			System.out.println("\nCannot start the Android debug bridge, IOException");
		}
    }
}