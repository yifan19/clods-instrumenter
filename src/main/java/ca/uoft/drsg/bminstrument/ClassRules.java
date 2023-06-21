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
public class ClassRules extends HashMap<String, List<Rule>> {
    private static final Logger LOG = LogManager.getLogger(ClassRules.class);
    // private static int nextID = 0;
    private String className;
    private Transformer transformer;
   

    public String getClassName() {
        return className;
    }

    public ClassRules(String className) {
        super();
        this.className = className;
        this.transformer = null;
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
        if (containsKey(key)) {
            value = get(key);
        } else {
            value = new ArrayList<>();
            put(key, value);
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

    public void register() {
        if (InstrumentationAgent.instrumentation == null) {
            LOG.error("Error: Rule {} instrumentation is NULL", this);
            return;
        }
        if (transformer != null) {
            boolean res = InstrumentationAgent.instrumentation.removeTransformer(transformer);
            if (!res) {
                LOG.error("Error: removing tranformer failed");
            }
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
        StringBuilder sb = new StringBuilder();

        for (String k : keySet()) {
            sb.append("(");
            sb.append(k);
            sb.append("): [");
            List<Rule> rules = get(k);
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

        if (keySet().size() >= 1) {
            int len = sb.length();
            sb.delete(len - 2, len);
        }
        return sb.toString();
    }
        

}
