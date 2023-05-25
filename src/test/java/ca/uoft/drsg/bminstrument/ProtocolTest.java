package ca.uoft.drsg.bminstrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;

import java.util.Properties;
import java.io.OutputStream;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
  
public class ProtocolTest {

  static String filename;

  @BeforeAll
  static public void setup() {
    filename = "/tmp/data.properties";
    try (OutputStream output = new FileOutputStream(filename)) {

      Properties prop = new Properties();
  
      // set the properties value
      prop.setProperty("className", "org.test");
      prop.setProperty("methodName", "foo");
      // prop.setProperty("parameterType");
      prop.setProperty("lineNumber", "100");
      prop.setProperty("variableName", "bar");
  
      prop.store(output, "for testing purposes");
      System.out.println(prop);
  
  } catch (IOException e) {
      e.printStackTrace();
  }
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
    
    Rule r = proto.process("add " + filename);
    assertEquals("org.test.foo#100:bar", r.toString());
  }

}
