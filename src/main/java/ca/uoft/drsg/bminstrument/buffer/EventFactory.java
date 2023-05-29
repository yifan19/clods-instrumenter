package ca.uoft.drsg.bminstrument.buffer;

import java.io.Externalizable;

/*
 * An event factory is used to prepopulate the ringbuffer data
 */
public abstract class EventFactory<T extends Externalizable> {
    
    abstract T newInstance();
}
