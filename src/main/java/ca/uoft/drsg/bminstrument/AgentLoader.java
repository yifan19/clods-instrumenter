package ca.uoft.drsg.bminstrument;

import com.sun.tools.attach.VirtualMachine;


public class AgentLoader {
    public void run(String[] args) {
        try {
            VirtualMachine jvm = VirtualMachine.attach(args[1]);
            jvm.loadAgent(args[0]);
            jvm.detach();
        } catch (Exception e) { 
            System.out.println(e);
        }
        
    }
}



