package ca.uoft.drsg.bminstrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;
import java.io.OutputStream;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
  
public class ProtocolTest {

  static String filename;
  static String filename2;

  @BeforeAll
  static public void setup() {
    filename = "/tmp/data.properties";
    try (OutputStream output = new FileOutputStream(filename)) {

      Properties prop = new Properties();
  
      // set the properties value
      prop.setProperty("ID", "10");
      prop.setProperty("className", "org.test");
      prop.setProperty("methodName", "foo");
      // prop.setProperty("parameterType");
      prop.setProperty("lineNumber", "100");
      prop.setProperty("byteCodeIndex", "99");

      prop.setProperty("variableName", "bar");
  
      prop.store(output, "for testing purposes");
      System.out.println(prop);
  
    } catch (IOException e) {
        e.printStackTrace();
    }

    filename2 = "/tmp/data2.properties";
    try (OutputStream output = new FileOutputStream(filename2)) {

      Properties prop = new Properties();
  
      // set the properties value
      prop.setProperty("ID", "99");
      prop.setProperty("className", "org.test");
      prop.setProperty("methodName", "foo");
      // prop.setProperty("parameterType");
      prop.setProperty("byteCodeIndex", "0");
      prop.setProperty("lineNumber", "entry");
      prop.setProperty("variableName", "bar");
  
      prop.store(output, "for testing entry");
      System.out.println(prop);
  
    } catch (IOException e) {
        e.printStackTrace();
    }

  }

  @BeforeEach
  public void init() {
    RuleBook.getInstance().clear();
  }

  @Test
  public void testBasicProtocol() { 
    Protocol proto = new Protocol();
    
    assertTrue(proto.parse("add " + filename));
    assertFalse(proto.parse("z add " + filename));
  }
  @Test
  public void testSimpleProperties() { 
    Protocol proto = new Protocol();
    
    String res = proto.process("add " + filename);
    assertEquals("OK", res.substring(0, 2));
    Integer.parseInt(res.substring(3));
    assertEquals("{org.test}: (foo()): [:100#bar]", RuleBook.getInstance().toString());
    assertEquals(RuleBook.getInstance().searchById(10).getByteCodeIndex(), 99);
  }
  @Test
  public void testDelete() {
    Protocol proto = new Protocol();
    String res = proto.process("add " + filename);
    assertEquals("{org.test}: (foo()): [:100#bar]", RuleBook.getInstance().toString());
    int id = Integer.parseInt(res.substring(3));
    String res2 = proto.process("delete " + Integer.toString(id));
    assertEquals("OK", res2.substring(0, 2));
    assertEquals("<empty>", RuleBook.getInstance().toString());
  }

  @Test
  public void testAddEntry() {
    Protocol proto = new Protocol();
    String res = proto.process("add " + filename2);
    // -1 also means entry
    assertEquals("{org.test}: (foo()): [:-1#bar]", RuleBook.getInstance().toString());
    int id = Integer.parseInt(res.substring(3));
    Rule r = RuleBook.getInstance().searchById(id);
    assertEquals(99, r.getId());
    String res2 = proto.process("delete " + Integer.toString(id));
    assertEquals("OK", res2.substring(0, 2));
    assertEquals("<empty>", RuleBook.getInstance().toString());
  }

}
