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
    public Rule processAdd() {
        LOG.info("adding rule " + predicate);

        try (InputStream input = new FileInputStream(predicate)) {

            Properties prop = new Properties();
            prop.load(input);

            // get the property value and print it out
            String className = prop.getProperty("className");
            String methodName = prop.getProperty("methodName");
            // System.out.println(prop.getProperty("parameterType"));
            String lineNumber = prop.getProperty("lineNumber");
            String variableName = prop.getProperty("variableName");
            
            if (className == null || methodName == null ||
                lineNumber == null || variableName == null) {
                    return null;
                }
            int lineNumber_int = 0;
            try {
                lineNumber_int = Integer.parseInt(lineNumber);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            
            Rule r = new Rule(className, methodName, lineNumber_int, variableName);
            RuleBook.getInstance().add(r);
            return r;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean processDelete() {
        LOG.info("deleting rule " + predicate);
        int id;
        try {
            id = Integer.parseInt(predicate);
            return RuleBook.getInstance().removeById(id);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Rule process(String op) {
        if (!parse(op)) {
            return null;
        }
        if (command.equals("add")) {
            Rule r = processAdd();
            return r;

        }
        else if (command.equals("delete")) {
            processDelete();
            return null;
        }
        return null;
    }
}

