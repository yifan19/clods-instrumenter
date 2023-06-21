package ca.uoft.drsg.bminstrument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class Rule {
    private static final Logger LOG = LogManager.getLogger(Rule.class);
    // private static int nextID = 0;
    
    private int id;
    private String className;
    private String methodName;
    private String[] parameterTypes;
    private int lineNumber;
    private String variableName;
    private int byteCodeIndex;
    private String strategy;
    ;

    Rule(int id, String className, String methodName, int lineNumber, int byteCodeIndex, String variableName) {
        this.id = id;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.variableName = variableName;
        this.parameterTypes = null;
        this.strategy = "before";
        this.byteCodeIndex = byteCodeIndex;
    }

    public void setParameters(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }    
    public String getStrategy() {
        return strategy;
    }
    public String[] getParameters() {
        return parameterTypes;
    }
    public int getId() {
        return id;
    }
    public int getlineNumber() {
        return lineNumber;
    }

    public String getMethodName() {
        return methodName;
    }
    public String getFullMethodName() {
        return className + '.' + methodName;
    }
    public String getClassName() {
        return className;
    }
    public int getByteCodeIndex() {
        return byteCodeIndex;
    }
    public String getVariableName() {
        return variableName;
    }

    @Override
    public String toString() {
        return ":" + lineNumber + "#" + variableName ;
    }
}
