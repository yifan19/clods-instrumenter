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
    // final ThreadFactory threadFactory;
    // final ProducerType producerType;
    // final WaitStrategy waitStrategy;

    public RingBuffer(
        final EventFactory<T> eventFactory,
        final int ringBufferSize,
        final boolean flushToDisk
    )
    {
        this.eventFactory = eventFactory;
        this.ringBufferSize = ringBufferSize;
        this.flushToDisk = flushToDisk;
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
    
    /* returns the files of all the logs we have collected*/ 
    /* will force flush everytime */
    public String collectData() {
        return null;

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
                "/data/tmp",
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
