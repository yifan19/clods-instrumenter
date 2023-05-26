package ca.uoft.drsg.bminstrument;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.PrintWriter; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* thread that listens to the static analysis tool analysis result 
 * format: Add instrumentation plan (.properties file)
 * returns ruleID if successful
 * format: Remove Rule ID
 * returns FAIL/SUCCESS
 * 
 */

public class Listener extends Thread {
    private static final Logger LOG = LogManager.getLogger(Listener.class);
    private int portNumber;
    public ServerSocket serverSocket;

    public Listener(int portNumber) {
        this.portNumber = portNumber;
    }

    public void parse(String line) {
        return;
    }
    @Override
    public void run() {
        LOG.info("Starting listener thread");

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
                LOG.info("Accepted a connection " + clientSocket);

                PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
                
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                String userInput = null;

                while ((userInput = in.readLine()) != null) {
                    LOG.info("server received " + userInput);
                    out.println("OK");
                }

                LOG.info("Done reading");
                clientSocket.close();
                
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
           
        }

        try {
            LOG.info("server socket closed");

            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);

        }
    }
}
