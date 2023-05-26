package ca.uoft.drsg.bminstrument;

import com.sun.tools.attach.VirtualMachine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentLoader {
    private static final Logger LOG = LogManager.getLogger(AgentLoader.class);
    public void run(String[] args) {
        try {
            VirtualMachine jvm = VirtualMachine.attach(args[1]);
            jvm.loadAgent(args[0]);
            jvm.detach();
        } catch (Exception e) { 
            LOG.error(e);
        }
        
    }
}



