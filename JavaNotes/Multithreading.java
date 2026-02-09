package JavaNotes;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Multithreading {

    public static class Helper {

        AtomicInteger counter = new AtomicInteger(0);

        public void increment(){
            counter.incrementAndGet();
            System.out.println(counter);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Helper helper = new Helper();

        Thread t1 = new Thread(() -> {
                for(int i=0;i<10000;i++)
                    helper.increment();
                });
        Thread t2 = new Thread(() -> {
            for(int i=0;i<10000;i++)
                helper.increment();
        });

        t1.start();
        t2.start();
    }


}
