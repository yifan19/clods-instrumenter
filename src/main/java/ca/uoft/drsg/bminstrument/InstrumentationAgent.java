package ca.uoft.drsg.bminstrument;

import java.lang.instrument.Instrumentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ca.uoft.drsg.bminstrument.buffer.LogEventFactory;
import ca.uoft.drsg.bminstrument.buffer.LogEventBuffer;

public class InstrumentationAgent {
    private static final Logger LOG = LogManager.getLogger(InstrumentationAgent.class);
    public static LogEventBuffer buffer;

    public static Instrumentation instrumentation;
    public static void premain(String argument, 
                             Instrumentation instrumentation) {
        int bufferSize = 1024 * 1024;
        String logDataPath = "/data/";
        int portNumber = 8089;

        LOG.info("premain running...");
        InstrumentationAgent.instrumentation = instrumentation;
        if (argument != null) {
            Protocol p = new Protocol();
            p.process("add " + argument);
        }
        Listener listener = new Listener(portNumber);
        buffer = new LogEventBuffer(bufferSize, logDataPath,
                                    new LogEventFactory(), true);
        listener.setDaemon(true);
        listener.start();
        // Rule CHANGME = new Rule("bar", "foo", 0, "baz");
        // start registering instrumentation

    }

    public static void agentmain(String argument,
                                 Instrumentation instrumentation) {
        LOG.info("agent main running (will attach to a program)");
        premain(argument, instrumentation);
    }

}
