package ca.uoft.drsg.bminstrument.buffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogEventBuffer extends RingBuffer<LogEvent>
{
	private static final Logger LOG = LogManager.getLogger(LogEventBuffer.class);
		public static LogEventBuffer buffer;
	
	public LogEventBuffer(
			int ringBufferSize, 
			String dirPath, 
			EventFactory<LogEvent> eventFactory,
			boolean flushToDisk)
	{
		super(ringBufferSize, dirPath, eventFactory, flushToDisk);
	}

	public void put(long id, long value) {
		LOG.info("[BM] {} {} ", id, value);
		RingBufferInternal<LogEvent> rb = getRingBuffer();
		long seq = rb.next();
		rb.get(seq).set(id, value);
	}
		
}
