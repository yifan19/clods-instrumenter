package ca.uoft.drsg.bminstrument;


public class Rule {
    static int nextID = 0;
    
    private int id;
    private String className;
    private String methodName;
    private String[] parameterType;
    private int lineNumber;

    Rule(String className, String methodName, int lineNumber) {
        id = nextID;
        nextID++;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return className + '.' + methodName + '#' + lineNumber; 
    }
}
