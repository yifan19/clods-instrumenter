package ca.uoft.drsg.bminstrument.buffer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
// import java.util.concurrent.locks;

public class LogEvent implements DataPersistable 
{
    private long id;
    private long value;

    public void set(long id, long value)
    {
        this.value = value;
        this.id = id;
        // this.tid = Thread.currentThread().getId();
    }

    public void clear() {
        this.value = 0;
        this.id = 0xDEADBEAF;
    }

    @Override
    public String toString()
    {
        return "LogEvent{" + "id=" + id + ", " + "value=" + value + '}';

    }
    @Override
    public void persistData(FileOutputStream out) throws IOException
    {
        ByteBuffer bf = ByteBuffer.allocate(16);
        bf.putLong(id);
        bf.putLong(value);
        out.write(bf.array());
    }
    @Override
    public void retrieveData(FileInputStream in) throws IOException
    {
        ByteBuffer bf = ByteBuffer.allocate(16);
        in.read(bf.array());
        id = bf.getLong();
        value = bf.getLong();
    }
}