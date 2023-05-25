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
    rb = RuleBook.getInstance();
    rb.clear();
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
    Rule r1 = new Rule("hello", "bye", 10, "foo");
    Rule r2 = new Rule("hello", "bye2", 11, "foo2");
    rb.add(r1);
    rb.add(r2);
    assertEquals(2, rb.size());
    assertEquals("[hello.bye#10:foo, hello.bye2#11:foo2]",rb.toString());
    rb.removeById(r1.getId());
    assertEquals(1, rb.size());
    assertEquals("[hello.bye2#11:foo2]",rb.toString());

   }
}
