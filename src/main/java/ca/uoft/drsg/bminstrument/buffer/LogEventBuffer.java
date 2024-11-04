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

	public void put(long value, long id) {
		// LOG.info("[BM] ID={}, {} ", id, value);
		System.out.println("[BM][" + Thread.currentThread().getName() + "]ID=" + id + "," + value);
		RingBufferInternal<LogEvent> rb = getRingBuffer();
		long seq = rb.next();
		rb.get(seq).set(id, value);
	}

	public void putObject(Object o, long id) {
		long value = 0;
		if (o != null) {
			value = o.hashCode();
		}
		put(value, id);
	}

	public void putLoop(long value, long id) {
		// LOG.info("[BM] ID={}, {} ", id, value);
		System.out.println("[BM][" + Thread.currentThread().getName() + "]ID=" + id + ",loop=" + value);
		RingBufferInternal<LogEvent> rb = getRingBuffer();
		long seq = rb.next();
		rb.get(seq).set(id, value);
	}

	public void putEntry() {
		// LOG.info("[BM] ID={}, {} ", id, value);
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		StackTraceElement entry = stacks[2];
		System.out.println("[BM][" + Thread.currentThread().getName() + "][Method Entry]" + entry.toString());
		// RingBufferInternal<LogEvent> rb = getRingBuffer();
		// long seq = rb.next();
		// rb.get(seq).set(id, value);
	}
	
	public void putStack(long id) {
		// LOG.info("[BM] ID={}, {} ", id, value);
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		System.out.println("[BM][Start Stack Trace][" + Thread.currentThread().getName() + "]");
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < stacks.length; i++) {
			sb.append(stacks[i].toString());
			sb.append('\n');
			// System.out.println(stacks[i].toString());
		}
		String stackString = sb.toString();
		System.out.print(stackString);
		System.out.println("[BM][End Stack Trace][" + Thread.currentThread().getName() + "]");
		RingBufferInternal<LogEvent> rb = getRingBuffer();
		
		long seq = rb.next();
		long stack_id = rb.getStackId(stackString);
		rb.get(seq).set(id, stack_id);
	}


}
