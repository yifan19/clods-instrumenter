package ca.uoft.drsg.bminstrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* All rules are divided into different hash
 * All sorted array of all the rules for a particular method
 * 
 */
public class ClassRules {
    private static final Logger LOG = LogManager.getLogger(ClassRules.class);
    // private static int nextID = 0;
    private Map<String, List<Rule> > methodRules;
    private String className;

   

    public String getClassName() {
        return className;
    }

    public Map<String, List<Rule>> getMethodRules() {
        return methodRules;
    }


    public ClassRules(String className) {
        this.methodRules = new HashMap<>();
        this.className = className;
    }

    private String genKey(Rule rule) {
        StringBuilder keyBuilder = new StringBuilder(rule.getMethodName());
        keyBuilder.append('(');
        String[] params = rule.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++)
            {
                keyBuilder.append(params[i]);
                if (i < params.length - 1 ) {
                    keyBuilder.append(',');
                }
            }

        }
        keyBuilder.append(')');
        String out = keyBuilder.toString();
        LOG.debug("key = {}", out);
        return out;
    }

    public void add(Rule rule) {
        String key = genKey(rule);
        List<Rule> value;
        if (methodRules.containsKey(key)) {
            value = methodRules.get(key);
        } else {
            value = new ArrayList<>();
            methodRules.put(key, value);
        }
        value.add(rule);

    }

    static public int indexById(int id, List<Rule> rules) {
        int index = -1;
        int size = rules.size();
        for (int i = 0; i < size; i++) {
            if (rules.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        /* found the index */
        return index;
    }

    public List<Rule> get(Rule rule) {
        return methodRules.getOrDefault(genKey(rule), null);        
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String k : methodRules.keySet()) {
            sb.append("(");
            sb.append(k);
            sb.append("): [");
            List<Rule> rules = methodRules.get(k);
            for (Rule r: rules) {
                sb.append(r.toString());
                sb.append(", ");
            }
            if (rules.size() >= 1) {
                int len = sb.length();
                sb.delete(len - 2, len);
            }
            sb.append("], ");
        }

        if (methodRules.keySet().size() >= 1) {
            int len = sb.length();
            sb.delete(len - 2, len);
        }
        return sb.toString();
    }
        

}
