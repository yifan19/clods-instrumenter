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
    private Transformer transformer;

    Rule(int id, String className, String methodName, int lineNumber, String variableName) {
        this.id = id;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.variableName = variableName;
        // TODO: leave the parameter to null
        this.parameterTypes = null;
        this.transformer = null;
    }

    public void setParameters(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
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
    public String getVariableName() {
        return variableName;
    }
    public void register() {
        if (InstrumentationAgent.instrumentation == null) {
            LOG.error("Error: Rule {} instrumentation is NULL", this);
            return;
        }
        transformer = new Transformer(this);
        InstrumentationAgent.instrumentation.addTransformer(transformer, true);
        trigger_retransformation();

        return;
    }

    private Class<?> findClass() {
        Class<?> targetCls;
        try {
            targetCls = Class.forName(className);
            return targetCls;
        } catch (Exception ex) {
            LOG.error("Class [{}] not found with Class.forName", className);
        }
        // otherwise iterate all loaded classes and find what we want
        for(Class<?> clazz: InstrumentationAgent.instrumentation.getAllLoadedClasses()) {
            if(clazz.getName().equals(className)) {
                targetCls = clazz;
                return targetCls;
            }
        }
        return null;
    }
    private void trigger_retransformation() {
        Class<?> classObject = findClass();
        LOG.info("Triggering retransformation for {}", className);

        if (classObject != null) {
            try {
                InstrumentationAgent.instrumentation.retransformClasses(classObject);
            } catch (Exception e) {
                LOG.error("Cannot trigger retransformation for rule {}", this);
            }
        }
    }
    public boolean unregister() {
        if (transformer == null) {
            LOG.error("Error: Rule {} was never registered", this);
            return false;
        }
            boolean res = InstrumentationAgent.instrumentation.removeTransformer(transformer);
            trigger_retransformation();
            return res;
    }
    @Override
    public String toString() {
        return className + '.' + methodName + '#' + lineNumber + ":" + variableName;
    }
}
