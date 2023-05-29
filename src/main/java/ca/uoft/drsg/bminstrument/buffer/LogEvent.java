package ca.uoft.drsg.bminstrument.buffer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

// import java.util.concurrent.locks;

public class LogEvent implements Externalizable
{
    private long id;
    private long value;

    public void set(long id, int value)
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
    public void writeExternal(ObjectOutput out)
        throws IOException
    {
        
    out.writeLong(id);
    out.writeLong(value);

    

    }

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id=in.readLong();
        value=in.readLong();

	}
}