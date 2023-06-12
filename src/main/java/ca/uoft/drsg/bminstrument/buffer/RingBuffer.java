package ca.uoft.drsg.bminstrument.buffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RingBuffer<T extends DataPersistable>
{
    private final ConcurrentLinkedQueue<RingBufferInternal<T>> headBuffer;
    // ConcurrentHashMap does partial locking only when updating
    private final ConcurrentHashMap<Long, RingBufferInternal<T>> tokenTable;
    private EventFactory<T> eventFactory;
    final int ringBufferSize;
    final boolean flushToDisk;
    final String dirPath;
    // final ThreadFactory threadFactory;
    // final ProducerType producerType;
    // final WaitStrategy waitStrategy;

    public RingBuffer(
        final int ringBufferSize,
        final String dirPath,
        final EventFactory<T> eventFactory,
        final boolean flushToDisk
    )
    {
        this.eventFactory = eventFactory;
        this.ringBufferSize = ringBufferSize;
        this.flushToDisk = flushToDisk;
        this.dirPath = dirPath;
        // this.threadFactory = threadFactory;
        // this.producerType = producerType;
        // this.waitStrategy = waitStrategy;

        headBuffer = new ConcurrentLinkedQueue<>();
        tokenTable = new ConcurrentHashMap<>();
        // = RingBuffer.create(producerType, eventFactory, ringBufferSize, waitStrategy);
    }
    @Deprecated
    public RingBufferInternal<T> start()
    {

        return null;
    }
    
    /* returns the last sequence flushed */
    /* will force flush everytime */
    public long[] collectData() {
        long[] result = new long[headBuffer.size()];
        int i = 0;
        for (RingBufferInternal<T> r: headBuffer) {
            long last_index = r.flushToDisk_normal();
            result[i] = last_index;
            i++;
        }
        return result;

    }

    public RingBufferInternal<T> getRingBuffer()
    {
        RingBufferInternal<T> ringBuffer;
        long tid = Thread.currentThread().getId();
        ringBuffer = tokenTable.get(tid);
        if (ringBuffer == null)
        {
            ringBuffer = new RingBufferInternal<T>(
                ringBufferSize,
                Thread.currentThread().getName(),
                dirPath,
                eventFactory,
                flushToDisk
                );
            tokenTable.put(tid, ringBuffer);
            headBuffer.add(ringBuffer);
        }
        return ringBuffer;
    }


    public void getStats()
    {
        for (RingBufferInternal<T> r : headBuffer)
        {
            System.out.println(r.getCursor());
        }
    }

    @Override
    public String toString()
    {
        return "ok...";
    }
}
