import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.io.*;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class PhotoClient implements Runnable {

private ServerSocket server;
    private Socket socket;
    private DataInputStream in;

    private static int TCP_SERVER_PORT;
    private static String IP_ADDRESS;

    // public PhotoClient() {

    //     System.out.println("photo client created");
    // }

    //@Override
    public void run() {
        System.out.println("photo client created");
        try {
            
            server = new ServerSocket(38500);
            System.out.println("created server socket");
            socket = server.accept();
            System.out.println("accept called");
            in = new DataInputStream(socket.getInputStream());

            Thread mReadThread = new Thread(readFromClient);
            mReadThread.setPriority(Thread.MAX_PRIORITY);
            mReadThread.start();
            System.out.println("started read thread");
            Thread closeSocketOnShutdown = new Thread() {
                public void run() {
                    try {
                        socket.close();
                        System.out.println("Server Socket Shutdown");      
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(closeSocketOnShutdown);

            
        } catch (UnknownHostException e) {
            System.out.println("Socket connection problem (Unknown host)"+ e.getMessage());
        } catch (IOException e) {
            System.out.println("Could not initialize I/O on server socket "+ e.getMessage());
        }
    }

     /**
     * Runnable that will read from the server on a thread
     */
    // private Runnable readFromClient = new Runnable() {
   
    public Runnable readFromClient  = new Runnable() {

        @Override
        public void run() {
            while (true) {
                try {
                    int len= in.readInt();                  
                    byte[] data = new byte[len];                   
                        if (len > 0) {
                            in.readFully(data,0,data.length);
                        }   
                    BufferedImage image = ImageIO.read(in);
                    ImageIO.write(image, "jpg", new File("C:\\androidapp.jpg"));
                    System.out.println("file written");
                    socket.close();
                } catch (IOException e) {
                    try {
                        //
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }       
    }; 
        
} //end class