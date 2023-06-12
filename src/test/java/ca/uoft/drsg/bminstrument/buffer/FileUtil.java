package ca.uoft.drsg.bminstrument.buffer;

import java.io.File;

public class FileUtil {

  static public void deleteAll(String dirPath) {
    File dir = new File(dirPath);
    for(File file: dir.listFiles()) {
      if (!file.isDirectory()) 
        file.delete();
    }
  }

  
  static public boolean fileExists(String fileName, String dirPath) {
    File dir = new File(dirPath);
    for(File file: dir.listFiles()) {
      if (file.getName().equals(fileName)) {
        return true;
      } 
    }
    return false;

  }  
}
