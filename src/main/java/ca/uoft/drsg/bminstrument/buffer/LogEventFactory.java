package ca.uoft.drsg.bminstrument.buffer;

/*
 * An event factory is used to prepopulate the ringbuffer data
 */
public class LogEventFactory extends EventFactory<LogEvent> {
    
    LogEvent newInstance() {
        return new LogEvent();
    }
}
