package ca.uoft.drsg.bminstrument;

import java.net.Socket;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; 

import java.util.ArrayList;


public class PseudoClient extends Thread {
    private String hostname;
    private int portNumber;
    private String[] cmds;
    
    /* to test the server's response */
    public ArrayList<String> results;

    public PseudoClient(String hostName, int portNumber, String[] cmds) {
        this.hostname = hostName;
        this.portNumber = portNumber;
        this.cmds = cmds;
        results = new ArrayList<>();
    }
    public void run() {
        System.out.println("Starting pseudoclient thread");
        Socket clientSocket = null;

        try {
            clientSocket = new Socket(hostname, portNumber);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            for (String cmd: cmds) {
                System.out.println("client writing:" + cmd);
                out.println(cmd);
                String response = in.readLine();
                System.out.println("adding response:" + response);
                results.add(response);
            }
            clientSocket.close();
   
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
