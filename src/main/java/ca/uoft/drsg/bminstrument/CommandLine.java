package ca.uoft.drsg.bminstrument;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ca.uoft.drsg.bminstrument.Protocol;
public class CommandLine {
  private static final Logger LOG = LogManager.getLogger(CommandLine.class);

  public static void main(String[] args) {

    String className = null;
    String instrFolder = null ;

    Protocol protocol = new Protocol();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-i")) {
        instrFolder = args[++i]; 
      } else {
        if (className == null) {
          className = args[i];
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
        String res = protocol.process("add " + folder + '/' + listOfFiles[i].getName());
        LOG.info("add " + listOfFiles[i].getName() + ": " + res);

      }
    }
  }
 
}
