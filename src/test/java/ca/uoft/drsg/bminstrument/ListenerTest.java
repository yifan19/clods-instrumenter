package ca.uoft.drsg.bminstrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;


import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
  
public class ListenerTest { 


  @Test
  public void testStartListener() { 
    String[] cmd = {"foo"};
    Listener t1 = new Listener(8089);    
    PseudoClient c1 = new PseudoClient("localhost", 8089, cmd);
    // this will call run() method  
    t1.start();
    c1.start();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {}
   
    t1.stop();
    c1.stop();

    try {
      t1.serverSocket.close();
    } catch (IOException e) {}
  
    
  }
  @Test
  public void testSendRequest() {
    String[] cmd = {"hello", "bye"};
    Listener t1 = new Listener(8089);
    PseudoClient c1 = new PseudoClient("localhost", 8089, cmd);
    t1.start();
    c1.start();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {}
   
    t1.stop();
    c1.stop();

    try {
      t1.serverSocket.close();
    } catch (IOException e) {}
    

    assertEquals(2, c1.results.size());
    assertEquals("OK", c1.results.get(0));
    assertEquals("OK", c1.results.get(1));


  }
}
