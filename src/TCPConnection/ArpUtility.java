/**
 * Copyright 2018 - Onsite Developments
 * @author Matt Wynyard November 2018
 * @version 0.2
 */

package TCPConnection;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Class to obtain IP address of the android phone from the arp table when running on ad-hoc network
 */
class ArpUtility {

    String out;  
    BufferedReader in;
    ProcessBuilder pb;
    Process p;

    public ArpUtility() {

        // Create operating system process from arpe.bat file command
        try {
            pb = new ProcessBuilder("arpe.bat"); 
            System.out.println("arp started");
            p = pb.start();	
        } catch (IOException e) {
            e.printStackTrace();
        }			
                
        in = new BufferedReader(new InputStreamReader(p.getInputStream()));

    }   

    /** TODO fix for static ip
    *
    * Retreives the ip address of android phone when on the same network
    * @return String the ip address or "error" if no ad-hoc network available
    */
    public String getIPAddress() {
        try {
            String buffer;
            while ((buffer=in.readLine())!=null) {
                if (buffer.contains("192.168.173") && buffer.contains("dynamic")) {
                    String[] arr = buffer.split(" ");
                    for (String i : arr) {
                        if (i.contains("192.168")) {
                            return i;
                        }
                    }
                }   
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }
}