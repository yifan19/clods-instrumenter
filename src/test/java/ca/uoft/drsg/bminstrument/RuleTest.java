package ca.uoft.drsg.bminstrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;


import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
  
public class RuleTest { 
  static private RuleBook rb;
  
  @BeforeAll
  public static void init() {
    System.out.println("wow");
    rb = RuleBook.getInstance();
    // System.out.println(rb);
  }

  @AfterEach
  public void teardownEach() {
    rb.clear();
  }

  @Test
  public void testRuleCreation() {  
    
    Rule r = new Rule("hello", "bye", 10, "foo");
    rb.add(r);
    assertEquals(1, rb.size()); 
    
    assertEquals("[hello.bye#10:foo]", rb.toString());

    rb.add(r);
    assertEquals(2, rb.size()); 
    assertEquals("[hello.bye#10:foo, hello.bye#10:foo]",rb.toString());

    rb.clear();
    assertEquals(0, rb.size());
    assertEquals("[]", rb.toString());


  }

  @Test  
  public void testRuleDeletion() {  
    // Rule r = new Rule("hello", "bye", 10);
    // rb.insert(r);
    // System.out.println(rb);
    // assertTrue(false);
    rb.clear();
  }

}
