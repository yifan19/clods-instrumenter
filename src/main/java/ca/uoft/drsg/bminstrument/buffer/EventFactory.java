package ca.uoft.drsg.bminstrument.buffer;


/*
 * An event factory is used to prepopulate the ringbuffer data
 */
public abstract class EventFactory<T> {
    
    abstract T newInstance();
}
