import java.util.concurrent.CyclicBarrier;

public class ThreadSharingHeap {
    public static void main(String[] args) throws InterruptedException {
        MyData data = new MyData(0);
        CyclicBarrier barrier = new CyclicBarrier(2);

        Thread t1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("Data Value is " + data.get_val() + " from Thread 1.");
                try {
                    barrier.await();
                } catch (Exception e) {
                    System.out.println(e);
                }
                synchronized (data) {
                    data.set_val(10);
                    System.out.println("Data Value changed to " + data.get_val() + " from Thread 1.");
                    data.notify();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                System.out.println("Data Value is " + data.get_val() + " from Thread 2.");
                try {
                    barrier.await();
                } catch (Exception e) {
                    System.out.println(e);
                }
                synchronized (data) {
                    try {
                        while (data.get_val() == 0)
                            data.wait();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    System.out.println("Data Value is " + data.get_val() + " from Thread 2.");
                }
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}

class MyData {
    private int val_data;

    MyData(int a) {
        val_data = a;
    }

    void set_val(int a) {
        val_data = a;
    }

    int get_val() {
        return val_data;
    }
}