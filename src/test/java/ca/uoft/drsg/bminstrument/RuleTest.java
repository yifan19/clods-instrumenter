package ca.uoft.drsg.bminstrument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
  
public class RuleTest { 
  static private RuleBook rb;
  @BeforeAll
  static public void init() {
    rb = RuleBook.getInstance();
  }

  @BeforeEach
  public void eachInit() {
    RuleBook.getInstance().clear();
  }

  @Test
  public void testRuleCreation() {  
    
    Rule r = new Rule(0, "hello", "bye", 10, "foo");
    rb.add(r);
    assertEquals(1, rb.size()); 
    
    assertEquals("{hello}: (bye()): [:10#foo]", rb.toString());

    Rule r2 = new Rule(1, "hello", "bye", 11, "bar");

    rb.add(r2);
    assertEquals(2, rb.size()); 
    assertEquals("{hello}: (bye()): [:10#foo, :11#bar]",rb.toString());

    rb.clear();
    assertEquals(0, rb.size());
    assertEquals("<empty>", rb.toString());


  }

  @Test  
  public void testRuleDeletion() {  
    Rule r1 = new Rule(0, "hello", "bye", 10, "foo");
    Rule r2 = new Rule(1, "hello", "bye", 11, "foo2");
    rb.add(r1);
    rb.add(r2);
    assertEquals(2, rb.size());
    assertEquals("{hello}: (bye()): [:10#foo, :11#foo2]",rb.toString());
    rb.removeById(r1.getId());
    assertEquals(1, rb.size());
    assertEquals("{hello}: (bye()): [:11#foo2]",rb.toString());

   }

  @Test  
  public void testRuleDeletionDiffMethod() {  
    Rule r1 = new Rule(0, "hello", "bye", 10, "foo");
    Rule r2 = new Rule(1, "hello", "bye2", 11, "foo2");
    rb.add(r1);
    rb.add(r2);
    assertEquals(2, rb.size());
    assertEquals("{hello}: (bye()): [:10#foo], (bye2()): [:11#foo2]",rb.toString());
    rb.removeById(r1.getId());
    assertEquals(1, rb.size());
    assertEquals("{hello}: (bye2()): [:11#foo2]",rb.toString());

  }

  @Test  
  public void testRuleDeletionDiffClass() {  
    Rule r1 = new Rule(0, "hello", "bye", 10, "foo");
    Rule r2 = new Rule(1, "hello2", "bye2", 11, "foo2");
    rb.add(r1);
    rb.add(r2);
    assertEquals(2, rb.size());
    String expected1 = "{hello}: (bye()): [:10#foo], {hello2}: (bye2()): [:11#foo2]";
    String expected2 = "{hello2}: (bye2()): [:11#foo2], {hello}: (bye()): [:10#foo]";
    System.out.println(rb);
    assertTrue(expected1.equals(rb.toString()) || expected2.equals(rb.toString()));
    rb.removeById(r1.getId());
    assertEquals(1, rb.size());
    assertEquals("{hello2}: (bye2()): [:11#foo2]",rb.toString());

  }

  @Test  
  public void testRealScenario() {  
    String className = "org.apache.hadoop.hdfs.server.blockmanagementBlockPlacementPolicyDefault";
    String params_str = "int,java.lang.String,java.util.Set,long,int,java.util.List,boolean,org.apache.hadoop.hdfs.StorageType";
    String[] paramTypes = params_str.split(",");
    String methodName = "chooseRandom";
    List<Rule> ruleList = new ArrayList<>();
    Rule r0 = new Rule(0, className, methodName, 528, "newExcludedNodes");
    Rule r1 = new Rule(1, className, methodName, 524, "i");
    Rule r2 = new Rule(2, className, methodName, 518, "Set.add");
    Rule r3 = new Rule(3, className, methodName, 515, "numOfReplicas");
    Rule r4 = new Rule(4, className, methodName, 515, "numOfAvailableNodes");

    ruleList.add(r0);
    ruleList.add(r1);
    ruleList.add(r2);
    ruleList.add(r3);
    ruleList.add(r4);

    for (Rule r: ruleList) {
      r.setParameters(paramTypes);
      rb.add(r);
    }

    assertEquals(5, rb.size());
    StringBuilder sb = new StringBuilder();
    sb.append("{").append(className).append("}: ")
      .append("(").append(methodName).append("(").append(params_str).append(")")
      .append("): ");
    sb.append("[");
    sb.append(":").append(528).append("#newExcludedNodes").append(", ");
    sb.append(":").append(524).append("#i").append(", ");
    sb.append(":").append(518).append("#Set.add").append(", ");
    sb.append(":").append(515).append("#numOfReplicas").append(", ");
    sb.append(":").append(515).append("#numOfAvailableNodes").append("]");
    assertEquals(sb.toString(), rb.toString());
    // assertEquals("{hello}: (bye()): [:10#foo], {hello2}: (bye()): [:10#foo]",rb.toString());
    // rb.removeById(r1.getId());
    // assertEquals(1, rb.size());
    // assertEquals("{hello}: (bye2()): [:11#foo2]",rb.toString());
  }


}
