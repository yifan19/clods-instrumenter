package ca.uoft.drsg.bminstrument;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.PrintWriter; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader; 

import java.util.StringTokenizer;  
/* thread that listens to the static analysis tool analysis result 
 * format: Add instrumentation plan (.properties file)
 * returns ruleID if successful
 * format: Remove Rule ID
 * returns FAIL/SUCCESS
 * 
 */

public class Listener extends Thread {
    private int portNumber;
    public ServerSocket serverSocket;

    public Listener(int portNumber) {
        this.portNumber = portNumber;
    }

    public void parse(String line) {
        return;
    }

    public void run() {
        System.out.println("Starting listener thread");

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return;

        }

        while (!isInterrupted()) {
            try {
                // accept a connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted a connection " + clientSocket);

                PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
                
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                String userInput = null;

                while ((userInput = in.readLine()) != null) {
                    System.out.println("server received " + userInput);
                    out.println("OK");
                }

                System.out.println("Done reading");
                clientSocket.close();
                
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
           
        }

        try {
            System.out.println("server socket closed");

            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);

        }
    }
}
