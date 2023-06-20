package ca.uoft.drsg.bminstrument.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
public class LogEventBufferTest {

  static public final String dirPath = "/data/";
  static public LogEventBuffer lb;

  static public class DummyWorker extends Thread {
    int numOfIterations;

    public DummyWorker(String name, int numOfIterations) {
      super(name);  
      this.numOfIterations = numOfIterations;
      
    }
    @Override
    public void run() {
      for (int i = 0; i < numOfIterations; i++) {
        lb.put(i,0);
      }
    }
  }

  static public void startDummies(int n, int numOfIterations) {
    DummyWorker[] dws = new DummyWorker[n];
    for (int i = 0; i < n; i++) {
      dws[i] = new DummyWorker("worker" + i, numOfIterations);
      dws[i].start();
    }
    for (int i = 0; i < n; i++) {
      try{
      dws[i].join();
      } catch(InterruptedException e) {
        System.out.println(e);
      }
    }
    
  }

  @BeforeAll
  static public void init() {
  }

  @BeforeEach
  public void initEach() {
    FileUtil.deleteAll(dirPath);
    lb = new LogEventBuffer(64, "/data/", 
                            new LogEventFactory(), true);
  }


  @Test
  public void testBasic() {
    
    lb.put(1, 0);
    long res[] = lb.collectData();
    assertEquals(1, res.length);
    assertEquals(0, res[0]);
  }

  @Test
  public void testBasicMultipleWorker() {
    
    startDummies(10, 1);
    long res[] = lb.collectData();
    assertEquals(10, res.length);
    for (int i = 0; i < 10; i++) {
      assertEquals(0, res[i]);
    }
  }

  @Test
  public void testFlush() {
    
    startDummies(10, 10);
    long res[] = lb.collectData();
    assertEquals(10, res.length);
    for (int i = 0; i < 10; i++) {
      assertTrue(FileUtil.fileExists("worker" + i + "_0", dirPath));
    }
  }

}
