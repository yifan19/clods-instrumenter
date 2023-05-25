package ca.uoft.drsg.bminstrument;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

public class Protocol {
    String command;
    String predicate;
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


    public Rule process(String op) {
        if (!parse(op)) {
            return null;
        }
        if (command.equals("add")) {
            Rule r = processAdd();
            return r;

        }
        else if (command.equals("delete")) {

        } else {
        }


        return null;
    }
}

