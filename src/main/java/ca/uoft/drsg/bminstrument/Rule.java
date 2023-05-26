package ca.uoft.drsg.bminstrument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class Rule {
    private static final Logger LOG = LogManager.getLogger(Rule.class);
    private static int nextID = 0;
    
    private int id;
    private String className;
    private String methodName;
    private String[] parameterType;
    private int lineNumber;
    private String variableName;
    private Transformer transformer;

    Rule(String className, String methodName, int lineNumber, String variableName) {
        id = nextID;
        nextID++;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.variableName = variableName;
        // TODO: leave the parameter to null
        this.parameterType = null;
        this.transformer = null;
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
    public String getVariableName() {
        return variableName;
    }
    public void register() {
        if (InstrumentationAgent.instrumentation == null) {
            LOG.error("Error: Rule {} instrumentation is NULL", this);
            return;
        }
        transformer = new Transformer(this);
        InstrumentationAgent.instrumentation.addTransformer(transformer);
        return;
    }

    public boolean unregister() {
        if (transformer == null) {
            LOG.error("Error: Rule {} was never registered", this);
            return false;
        }
        return InstrumentationAgent.instrumentation.removeTransformer(transformer);
    }
    @Override
    public String toString() {
        return className + '.' + methodName + '#' + lineNumber + ":" + variableName;
    }
}
