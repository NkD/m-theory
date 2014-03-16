/**
 * 
 */
package cz.mtheory.example.concurent.holic;

import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal NkD Nikodim
 */
public class HolicExample {

    private static class Holic implements Runnable {

        private BlockingQueue<Zakaznik> cekarna;
        private String name;

        private Holic(String name, BlockingQueue<Zakaznik> cekarna) {
            this.name = name;
            this.cekarna = cekarna;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Zakaznik zakaznik = cekarna.take();
                    System.out.println(this + " prijmul " + zakaznik.getName());
                    zakaznik.ostrihej();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class Zakaznik {
        private String name;

        private Zakaznik(String name) {
            this.name = name;
        }

        private void ostrihej() {
            System.out.println(toString() + " - je prave strihan");
            long s = System.currentTimeMillis();
            while (s + 1000 > System.currentTimeMillis()) {
                Thread.yield();
            }
            System.out.println(toString() + " - je jiz ostrihan");
        }

        @Override
        public String toString() {
            return name;
        }

        private String getName() {
            return name;
        }
    }

    public static void main(String[] args) {
        BlockingQueue<Zakaznik> cekarna = new ArrayBlockingQueue<HolicExample.Zakaznik>(10, true);

        for (int i = 0; i < 100; i++) {
            new Thread(new Holic("Holic " + i, cekarna), "holic " + i).start();
        }

        int i = 0;
        boolean mistoVCekarne = true;
        Zakaznik zakaznik = null;
        long s = System.nanoTime();
        while (i < 100) {
            if (mistoVCekarne) {
                i = i + 1;
                zakaznik = new Zakaznik("Zakaznik " + i);
            }
            try {
                System.out.println("Prichazi zakaznik " + zakaznik);
                mistoVCekarne = cekarna.offer(zakaznik, 100, TimeUnit.MILLISECONDS);
                if (!mistoVCekarne) System.out.println(zakaznik + " odesel, protoze cekarna je plna");
            } catch (InterruptedException e) {
                System.out.println("Problem");
                e.printStackTrace();
            }
        }
        while (cekarna.peek() != null) {
            Thread.yield();
        }
        long tt = System.nanoTime() - s;
        BigDecimal bd = new BigDecimal(tt).divide(new BigDecimal(1000000000l));
        System.out.println("Ostrihani lidi trvalo - " + bd.toPlainString() + " sec");
        System.exit(1);
    }

}
