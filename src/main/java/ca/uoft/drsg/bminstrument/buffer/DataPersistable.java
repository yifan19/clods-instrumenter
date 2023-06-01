package ca.uoft.drsg.bminstrument.buffer;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
public interface DataPersistable {
    public void persistData(FileOutputStream out) throws IOException;
    public void retrieveData(FileInputStream in) throws IOException;

}
