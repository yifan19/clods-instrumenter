package ca.uoft.drsg.bminstrument;


import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// Singleton Pattern
public class RuleBook {
    private static final Logger LOG = LogManager.getLogger(RuleBook.class);
    private static RuleBook instance = new RuleBook();

    private Map<String, ClassRules> rules;

    private RuleBook(){
        rules = new HashMap<>();
    }

    public static RuleBook getInstance(){
        return instance;
    }

    private String genKey(Rule rule) {
        StringBuilder keyBuilder = new StringBuilder(rule.getClassName());
        return keyBuilder.toString();
    }

    public int add(Rule r) {
        String key = genKey(r);
        ClassRules value;
        LOG.info("adding rule {} to rulebook", r);
        if (rules.containsKey(key)) {
            value = rules.get(key);
        } else {
            value = new ClassRules(r.getClassName());
            rules.put(key,value);
        }
        value.add(r);
        LOG.info("creating new class {}", value);
        LOG.debug("current rules: {}", rules);

        /* register it with the transformer */
        value.register();

        return r.getId();
    }

    // public int add(List<Rule> rules) {
    //     for (Rule r: rules) {
    //         add(r);
    //     }
    // }

    
    public Rule searchById(int id) {
        int index;
        for (String k1: rules.keySet()) {
            ClassRules v1 = rules.get(k1);
            Map<String, List<Rule>> methodRules = v1.getMethodRules();
            for (String k2: methodRules.keySet()) {
                List<Rule> v2 = methodRules.get(k2);
                index = ClassRules.indexById(id, v2);
                if (index != -1) {
                    return v2.get(index);
                }
                
            }
        }
        return null;

    }
    public boolean removeById(int id) {
        int index;
        /* found the index */
        for (String k1: rules.keySet()) {
            ClassRules v1 = rules.get(k1);
            Map<String, List<Rule>> methodRules = v1.getMethodRules();
            for (String k2: methodRules.keySet()) {
                List<Rule> v2 = methodRules.get(k2);
                index = ClassRules.indexById(id, v2);
                if (index != -1) {
                    v2.remove(index);
                    if (v2.size() == 0) {
                        methodRules.remove(k2,v2);
                        if(methodRules.size() == 0) {
                            rules.remove(k1,v1);
                        }
                    }
                    return true;
                }
                
            }
        }
        return false;
    }

    public int size() {
        int count = 0;
        for (String k1: rules.keySet()) {
            ClassRules v1 = rules.get(k1);
            Map<String, List<Rule>> methodRules = v1.getMethodRules();
            for (String k2: methodRules.keySet()) {
                List<Rule> v2 = methodRules.get(k2);
                for (Rule r: v2) {
                    count++;
                }
            }
        }
        return count;
    }

    public void clear() {
        for (String k1: rules.keySet()) {
            ClassRules v1 = rules.get(k1);
            Map<String, List<Rule>> methodRules = v1.getMethodRules();
            v1.unregister();
            // for (String k2: methodRules.keySet()) {
            //     List<Rule> v2 = methodRules.get(k2);
            //     for (Rule r: v2) {
            //         r.unregister();
            //     }
            // }
            methodRules.clear();
            methodRules = null;

        }
        rules.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        
        for (String k : rules.keySet()) {
            sb.append("{");
            sb.append(k);
            sb.append("}: ");
            ClassRules v = rules.get(k);
            sb.append(v.toString());
            sb.append(", ");
        }
        if (rules.keySet().size() >= 1) {
            int len = sb.length();
            sb.delete(len - 2, len);
        } else {
            sb.append("<empty>");
        }
        return sb.toString();
    }
        

    
}