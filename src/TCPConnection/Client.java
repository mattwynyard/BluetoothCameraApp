package TCPConnection;
/**
* Initializes a client socket connection on the IP address and port number supplied.
* Then handles incoming and outgoing messages to Android phone server 
* @author Matt Wynyard October 2018
* @version 1.0
*/

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.UnknownHostException;

public class Client {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private static int TCP_SERVER_PORT;
    private static String IP_ADDRESS;
    //private PhotoClient photoClient;

    public Client(String ip, int port) {
        TCP_SERVER_PORT = port;
        IP_ADDRESS = ip;
        createConnection();         
    }

    public String getIP() {
        return IP_ADDRESS;
    }

    public void sendStopCommand() {
        out.println("STOP");
        
        try {
            Thread.sleep(200);
        } catch(Exception ex) {
            
        }
    }

    public void sendStartCommand() {
        out.println("START");
        
        try {
            Thread.sleep(200);
        } catch(Exception ex) {
            
        }		
    }

    public void sendStatusCommand() {
        out.println("STATUS");
    }

    public void createConnection() {
        //Create socket connection
        try{
            socket = new Socket(IP_ADDRESS, TCP_SERVER_PORT);          
            System.out.println("Socket Created");
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(readFromServer).start();
            
            Thread closeSocketOnShutdown = new Thread() {
                public void run() {
                    try {
                        socket.close();
                        System.out.println("Socket Shutdown");      
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(closeSocketOnShutdown);
        } 
        catch (UnknownHostException e) {
            System.out.println("Socket connection problem (Unknown host)"+ e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not initialize I/O on socket "+ e.getMessage());
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
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while ((buffer=in.readLine())!=null) {
                    if (buffer.toString().contains("NOTRECORDING")) {
                        CameraApp.setRecording(false);
                    } else if (buffer.toString().contains("RECORDING")) {
                        CameraApp.setRecording(true);
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
}