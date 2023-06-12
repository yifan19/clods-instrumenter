package ca.uoft.drsg.bminstrument.buffer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RingBufferInternal<E extends DataPersistable> {
    private static final Logger LOG = LogManager.getLogger(RingBufferInternal.class);
    private Object[] buf;
    private long sequence;
    private long lastElementFlushed; 

    private int size;
    private int mask;
    private String id;
    private String dir;
    private int fileIndex;
    private boolean flush;
    public RingBufferInternal(
        int initial_size, 
        String threadId, 
        String dirPath, 
        EventFactory<E> eventFactory,
        boolean flushToDisk

    ) {
        // assumed to be power of 2
        buf = new Object[initial_size];
        size = initial_size;
        for (int i = 0; i < size; i++) {
            buf[i] = eventFactory.newInstance();
        } 
        mask = (size - 1);
        sequence = -1;
        lastElementFlushed = -1;
        id = threadId;
        dir = dirPath;
        flush = flushToDisk;
        fileIndex = -1;
    }
    public long flushToDisk_normal() {
        // System.out.println(dir);
        // System.out.println(id + '_' + Integer.toString(fileIndex));
        boolean done = false;
        if (( (lastElementFlushed + 1) & mask) == 0) {
            fileIndex++;
        }
        if (flush) {
            
            while (!done) {
                LOG.info("flushToDisk: sequence = {},  lastElementFlushed = {}", sequence, lastElementFlushed);
                Path file = Paths.get(dir, id + '_' + Integer.toString(fileIndex));
                boolean haveWrittenDataInCycle = false;
                try {
                    FileOutputStream fos = new FileOutputStream(file.toString());
                    long start = lastElementFlushed + 1;
                    long finish = sequence + 1;
                    long i;
                    for (i = start ; i < finish; i++) {
                        if ((i & mask) == 0 && haveWrittenDataInCycle == true){
                            LOG.info("rotating to a new file");

                            // we are writing slot #0 again
                            // abort the write to a new file
                            lastElementFlushed = i - 1;
                            fileIndex++;
                            break;
                        }
                        haveWrittenDataInCycle = true;
                        get(i).persistData(fos);
                    }
      
                    // normal termination, we don't need to start a new file
                    if (i == finish) {
                        lastElementFlushed = finish - 1;
                        done = true;
                    }
                    fos.close();

                } catch (IOException e) {
                    System.err.println(e);
                }

            }
        }
        return lastElementFlushed;
    }

    // private void flushToDisk_mmap() {
    //     // System.out.println(dir);
    //     // System.out.println(id + '_' + Integer.toString(fileIndex));
    //     if (flush) {
    //         Path file = Paths.get(dir, id + '_' + Integer.toString(fileIndex));
    //         Charset cs = Charset.forName("UTF-8");
    //         String s;
            
    //         try (RandomAccessFile fd
    //              = new RandomAccessFile(file.toString(), "rw")){
                    
    //             MappedByteBuffer mapped_buff = fd.getChannel().map(
    //             FileChannel.MapMode.READ_WRITE, 0, size * 4);

                
    //             for (int i = 0; i < size; i++) {
    //                 @SuppressWarnings("unchecked")

    //                 byte[] temp_bytebuf = ((E) buf[i]).toSerial();
    //                 // System.out.println(temp_bytebuf[3] + "..." + buf[i].toString());

    //                 // s = buf[i].toString() + "\n";
    //                 for (int j = 0; j < 4; j++) {
    //                     mapped_buff.put(temp_bytebuf[j]);
    //                 }
    //             }
                
    //         } catch (IOException e) {
    //             System.err.println(e);
    //         }
    //     }
    // }

    public E get(long seq) {

        int i = (int) (seq & mask);
        @SuppressWarnings("unchecked")
        // unsafe casting... workaround??
        final E e = (E) buf[i];
        return e;
    }

    public long next() {
        // the current sequence is just about to overtake

        if (sequence >= lastElementFlushed + size) {
            flushToDisk_normal();
        }

        sequence++;
        

        return sequence;
    }
    public void publish(long seq) {
        return;
    }

    public long getCursor() {
        return sequence;
    }
}
