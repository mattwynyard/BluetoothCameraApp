package TCPConnection;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TCPServer {

    /**
     * WebServer constructor.
     */

    private InputStream in;

    private ServerSocket server;
    private Socket client;
    private Thread mReadThread;
    private PrintWriter out;
    private PrintWriter writer;

    public TCPServer() {
        try {
            // create the main server socket
            server = new ServerSocket(38200, 0, InetAddress.getByName(null));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return;
        }
        start();
    }

    public void start() {

        System.out.println("Waiting for connection");
        try {
            // wait for a connection
            client = server.accept();
            // remote is now the connected socket
            System.out.println("Connection, sending data.");
            out = new PrintWriter(client.getOutputStream());

            mReadThread = new Thread(readFromClient);
            mReadThread.setPriority(Thread.MAX_PRIORITY);
            mReadThread.start();
            // read the data sent. We basically ignore it,
            // stop reading once a blank line is hit. This
            // blank line signals the end of the client HTTP

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

    }


    public void sendData(String message) {
        System.out.println(message);
        out.println(message);
        out.flush();
    }
    /**
     * To run in the background,  reads in comming data
     * from the client
     */
    private Runnable readFromClient = new Runnable() {

        @Override
        public void run() {
            System.out.println("Read Thread listening");
            int length;
            byte[] buffer = new byte[1024];
            try {
                in = client.getInputStream();
                while ((length = in.read(buffer)) != -1) {
                    String line = new String(buffer, 0, length);
                    if (line.equals("Start")) {
                        sendData(line);
                    } else if (line.equals("Stop")) {
                        sendData(line);
                    } else {

                    }
                }
                in.close();

            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };
}

