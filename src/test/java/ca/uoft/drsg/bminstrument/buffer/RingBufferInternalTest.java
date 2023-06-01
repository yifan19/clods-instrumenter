package ca.uoft.drsg.bminstrument.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
public class RingBufferInternalTest {

  static public final String dirPath = "/data/";
  RingBufferInternal<LogEvent> rbi;
  @BeforeAll
  static public void init() {
    File dir = new File(dirPath);
    for(File file: dir.listFiles()) {
      if (!file.isDirectory()) 
        file.delete();
    }
  }

  @BeforeEach
  public void initEach() {
    rbi = new RingBufferInternal<>(
      64,
      Thread.currentThread().getName(),
      dirPath,
      new LogEventFactory(),
      true);

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
    
    assertEquals(rbi.getCursor(), -1);
    long seq = rbi.next();
    assertEquals(0, seq);
    LogEvent e = rbi.get(seq);
    e.set(0, 3);
    assertEquals("LogEvent{id=0, value=3}", rbi.get(seq).toString());
    e.clear();
    assertEquals("LogEvent{id=-559038801, value=0}", rbi.get(seq).toString());

  }

  @Test
  void testAddData() {

    // 64 items, should just be just enough to not rotate
    for (int i = 0; i < 64; i++) {
      long seq = rbi.next();
      rbi.get(seq).set(seq, i);
    }
    assertFalse(fileExists("main_0"));
    assertFalse(fileExists("main_1"));

    // + 1 item now, should cause the first file to save
    for (int i = 0; i < 1; i++) {
      long seq = rbi.next();
      rbi.get(seq).set(seq, i);
    }
    assertTrue(fileExists("main_0"));
    assertFalse(fileExists("main_1"));

    // + 64 item now, should cause the first 2 to save
    for (int i = 0; i < 64; i++) {
      long seq = rbi.next();
      rbi.get(seq).set(seq, i);
    }
    assertTrue(fileExists("main_0"));
    assertTrue(fileExists("main_1"));
    assertFalse(fileExists("main_2"));

  }
  
}
