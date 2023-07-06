package ca.uoft.drsg.bminstrument;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Protocol {
    private static final Logger LOG = LogManager.getLogger(Protocol.class);

    private String command;
    private String predicate;
    public Protocol() {
    }
    
    public boolean parse(String op) {
        StringTokenizer tokenizer = new StringTokenizer(op, " ");
        if(tokenizer.countTokens() != 2) {
            return false;
        }

        command = tokenizer.nextToken();
        predicate = tokenizer.nextToken();
        return true;
    }
    private Rule processAdd() {
        LOG.info("adding rule " + predicate);

        try (InputStream input = new FileInputStream(predicate)) {

            Properties prop = new Properties();
            prop.load(input);

            // get the property value and print it out
            String ID = prop.getProperty("ID");
            String className = prop.getProperty("className");
            String methodName = prop.getProperty("methodName");
            // System.out.println(prop.getProperty("parameterType"));
            String lineNumber = prop.getProperty("lineNumber");
            String variableName = prop.getProperty("variableName");
            String byteCodeIndex = prop.getProperty("byteCodeIndex");

            if (ID == null ||
                className == null || methodName == null ||
                lineNumber == null || variableName == null ||
                byteCodeIndex == null) {
                    return null;
            }
            int lineNumber_int = -1;
            if (!lineNumber.equalsIgnoreCase("entry")) {
                try {
                    lineNumber_int = Integer.parseInt(lineNumber);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            int ID_int = 0;
            int byteCodeIndex_int = 0;
            try {
                ID_int = Integer.parseInt(ID);
                byteCodeIndex_int = Integer.parseInt(byteCodeIndex);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            Rule r = new Rule(ID_int, className, methodName, lineNumber_int, byteCodeIndex_int, variableName);
            
            if (prop.containsKey("parameterTypes")) {
                String parameter_result = prop.getProperty("parameterTypes");
                String[] params = parameter_result.split(",\\s*", 0);
                if (params.length <= 1 && params[0].equals("")) {
                    params = new String[0];
                }
                r.setParameters(params);
            }
            if (prop.containsKey("strategy")) {
                // "after" specifies we wish to print after a store
                // (default) "before" specifies we wish to print before a load 
                String strategyResult = prop.getProperty("strategy");
                r.setStrategy(strategyResult);
            }

            if (prop.containsKey("LoopID")) {
                String loopID = prop.getProperty("LoopID");
                int loopID_int = -1;
                try {
                    loopID_int = Integer.parseInt(loopID);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                r.setLoopId(loopID_int);
            }



            RuleBook.getInstance().add(r);
            return r;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String processDelete() {
        LOG.info("deleting rule " + predicate);
        int id;
        boolean result = false;
        try {
            id = Integer.parseInt(predicate);
             result = RuleBook.getInstance().removeById(id);
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result ? "OK": "FAIL";
    }

    private String processCollect() {
        LOG.info(" " + predicate);
        if (InstrumentationAgent.buffer != null) {
            long result[] = InstrumentationAgent.buffer.collectData();
            LOG.info(result);
            return "OK" + result;
        }
        return "FAIL";

    }

    public String process(String op) {
        if (!parse(op)) {
            return "FAIL: PARSE ERROR";
        }
        if (command.equals("add")) {
            Rule r = processAdd();
            if (r == null) {
                return "FAIL";
            } else {
                return "OK " + Integer.toString(r.getId());
            }

        }
        else if (command.equals("delete")) {
            return processDelete();
        }
        else if (command.equals("collect")) {
            return processCollect();
        }

        return "FAIL: UNKNOWN";
    }
}

