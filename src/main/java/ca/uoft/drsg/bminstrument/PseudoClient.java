package ca.uoft.drsg.bminstrument;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PseudoClient extends Thread {
    private String hostname;
    private int portNumber;
    private String[] cmds;
    private static final Logger LOG = LogManager.getLogger(PseudoClient.class);

    /* to test the server's response */
    public ArrayList<String> results;

    public PseudoClient(String hostName, int portNumber, String[] cmds) {
        this.hostname = hostName;
        this.portNumber = portNumber;
        this.cmds = cmds;
        results = new ArrayList<>();
    }
    public void run() {
        LOG.info("Starting pseudoclient thread");
        Socket clientSocket = null;

        try {
            clientSocket = new Socket(hostname, portNumber);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            for (String cmd: cmds) {
                LOG.info("client writing:" + cmd);
                out.println(cmd);
                String response = in.readLine();
                LOG.info("adding response:" + response);
                results.add(response);
            }
            clientSocket.close();
   
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    public static void main(String[] args) {

        PseudoClient pc = new PseudoClient("localhost", 8089, args);
        pc.run();
    }
}
