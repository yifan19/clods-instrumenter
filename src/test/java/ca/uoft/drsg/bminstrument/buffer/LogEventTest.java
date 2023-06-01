package ca.uoft.drsg.bminstrument.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
public class LogEventTest {

  static public final String dirPath = "/data/";
  @BeforeAll
  static public void init() {
    File dir = new File(dirPath);
    for(File file: dir.listFiles()) {
      if (!file.isDirectory()) 
        file.delete();
    }
  }


  public boolean fileExists(String fileName) {
    File dir = new File(dirPath);
    for(File file: dir.listFiles()) {
      if (file.getName().equals(fileName)) {
        return true;
      } 
    }
    return false;

  }
  @Test
  public void testBasic() {
    LogEvent e = new LogEventFactory().newInstance();
    e.set(0, 3);
    assertEquals("LogEvent{id=0, value=3}", e.toString());
  }


  @Test
  public void testWriteExternalMultiple() throws IOException, ClassNotFoundException {
    LogEvent event1 = new LogEventFactory().newInstance();
    LogEvent event2 = new LogEventFactory().newInstance();
    LogEvent event1_clone = new LogEventFactory().newInstance();
    LogEvent event2_clone = new LogEventFactory().newInstance();

    event1.set(0xDEADBEEFCAFEBABEL, 0xA5A5A5A5A5A5A5A5L);
    event2.set(0xDECAFBADCAFEBABEL,0xFEEDFACECAFEBEEFL);  

    File f= new File(dirPath + "Test.txt");
    FileOutputStream out = new FileOutputStream(f);    
    event1.persistData(out);
    event2.persistData(out);
    out.close();

    FileInputStream in = new FileInputStream(f);
    event1_clone.retrieveData(in);
    event2_clone.retrieveData(in);

    in.close();
    assertEquals(event1_clone.toString(), event1.toString());
    assertEquals(event2_clone.toString(), event2.toString());


  }


  
}
