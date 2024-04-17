package ca.uoft.drsg.bminstrument;

import java.io.File;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ca.uoft.drsg.bminstrument.Protocol;
import ca.uoft.drsg.bminstrument.Transformer;
import java.io.IOException;

public class CommandLine {
  private static final Logger LOG = LogManager.getLogger(CommandLine.class);
  
  public static void main(String[] args) {
    
    String targetClassFolder = null;
    String instrFolder = null ;

    Protocol protocol = new Protocol();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-i")) {
        instrFolder = args[++i]; 
      } else {
        if (targetClassFolder == null) {
          targetClassFolder = args[i];
        } else {
          LOG.error("wrong cmdline format");
          return;
        }
      }
    }      
    
    File folder = new File(instrFolder);
    File[] listOfFiles = folder.listFiles();
    if(listOfFiles == null) {
      LOG.error("instrumentation folder cannot be opened");
      return;
    }
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        if ( listOfFiles[i].getName().endsWith(".properties") == false) {
          continue;
        }
        String res = protocol.process("add " + folder + '/' + listOfFiles[i].getName());
        LOG.info("add " + listOfFiles[i].getName() + ": " + res);

      }
    }
    Transformer[] transformers = RuleBook.getInstance().getTransformers();
    
    for (Transformer t : transformers) {
      String className = t.getClassRules().getClassName().replace('.', '/');
      File localClass = new File(targetClassFolder + "/" + className + ".class" );
      LOG.info("loading " + localClass);
      byte[] classfileBuffer;
      try {
        classfileBuffer = Files.readAllBytes(localClass.toPath());
        
        t.transform(null, className, null, null, classfileBuffer);
      } catch(Exception e) {
        LOG.error(e);
      }
    }
    // ok now we should be manually triggering the transformation
    
  }
  
}
