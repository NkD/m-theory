/**
 * 
 */
package cz.mtheory.planet.threads;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import cz.mtheory.planet.chunk.Chunk;

/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class TextureGenerator implements Runnable {

    private static BlockingQueue<Chunk> cekarna = new PriorityBlockingQueue<Chunk>(10000, new Comparator<Chunk>() {
        @Override
        public int compare(Chunk o1, Chunk o2) {
            synchronized (o1) {
                synchronized (o2) {
                    if (o1.isVisible() && !o2.isVisible()) return -1;
                    if (!o1.isVisible() && o2.isVisible()) return 1;
                    return o1.getPriority() < o2.getPriority() ? -1 : o1.getPriority() == o2.getPriority() ? 0 : 1;
                }
            }
        }
    });

    //private static BlockingQueue<Chunk> cekarna1 = new ArrayBlockingQueue<Chunk>(1000, true);

    static {
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new TextureGenerator());
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
    }

    public static void add(Chunk chunk) {
        cekarna.add(chunk);
    }

    public static void remove(Chunk chunk) {
        cekarna.remove(chunk);
    }

    private TextureGenerator() {
        //
    }

    @Override
    public void run() {
        while (true) {
            try {
                Chunk chunk = cekarna.take();

                //System.out.println(this.toString() + " prijmul " + chunk);
                chunk.generateTexture();
                System.out.println("Pool size : " + cekarna.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
