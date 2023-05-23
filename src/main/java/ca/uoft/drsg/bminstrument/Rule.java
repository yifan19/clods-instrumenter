package ca.uoft.drsg.bminstrument;


public class Rule {
    static int nextID = 0;
    
    private int id;
    private String className;
    private String methodName;
    private String[] parameterType;
    private int lineNumber;
    private String variableName;

    Rule(String className, String methodName, int lineNumber, String variableName) {
        id = nextID;
        nextID++;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.variableName = variableName;
        // TODO: leave the parameter to null
        this.parameterType = null;
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
    public boolean register() {
        return false;
    }

    public boolean unregister() {
        return false;
    }
    @Override
    public String toString() {
        return className + '.' + methodName + '#' + lineNumber + ":" + variableName;
    }
}
