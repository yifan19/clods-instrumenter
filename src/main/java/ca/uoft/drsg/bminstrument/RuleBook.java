package ca.uoft.drsg.bminstrument;

import java.util.List;
import java.util.ArrayList;

// Singleton Pattern
public class RuleBook {
    private static RuleBook instance = new RuleBook();
    
    private List<Rule> rules;

    private RuleBook(){
        rules = new ArrayList<Rule>();
    }

    public static RuleBook getInstance(){
        return instance;
    }

    public int add(Rule r) {
        // assume all rules have unique ID
        rules.add(r);
        /* register it with the transformer */
        r.register();
        return r.getId();
    }
    public boolean removeById(int id) {
        int index = -1;
        int size = rules.size();
        for (int i = 0; i < size; i++) {
            if (rules.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        /* found the index */
        if (index != -1) {
            Rule tgt_rule = rules.get(index);
            tgt_rule.unregister(); 
            rules.remove(index);
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return rules.size();
    }

    public void clear() {
        for (Rule r: rules) {
            r.unregister();
        }
        rules.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Rule r : rules) {
            sb.append(r.toString()).append(", ");
        }
        if (rules.size() >= 1) {
            int len = sb.length();
            sb.delete(len - 2, len);
        }
        sb.append("]");
        return sb.toString();
    }
        

    
}