package ca.uoft.drsg.bminstrument;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PseudoClient extends Thread {
    private String hostname;
    private int portNumber;
    private String[] cmds;
    private boolean commandFile;
    private static final Logger LOG = LogManager.getLogger(PseudoClient.class);

    /* to test the server's response */
    public ArrayList<String> results;

    public PseudoClient(String hostName, int portNumber, String[] cmds, boolean commandFile) {
        this.hostname = hostName;
        this.portNumber = portNumber;
        this.cmds = cmds;
        this.commandFile = commandFile;
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
            if (commandFile == true) {
                List<String> lines = Files.readAllLines(Paths.get(cmds[1]));
                for (String cmd: lines) {
                    LOG.info("client writing:" + cmd);
                    out.println(cmd);
                    String response = in.readLine();
                    LOG.info("adding response:" + response);
                    results.add(response);
                }
            } else {
                for (String cmd: cmds) {
                    LOG.info("client writing:" + cmd);
                    out.println(cmd);
                    String response = in.readLine();
                    LOG.info("adding response:" + response);
		    System.out.println(response);
                    results.add(response);
                }
            }
            clientSocket.close();
   
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    public static void main(String[] args) {
        PseudoClient pc;
        if (args.length == 2 && args[0].equals("file")) {
            LOG.info("taking file!");
            pc = new PseudoClient("localhost", 8089, args, true);
        } else {
            pc = new PseudoClient("localhost", 8089, args, false);
        }
        pc.run();
    }
}
