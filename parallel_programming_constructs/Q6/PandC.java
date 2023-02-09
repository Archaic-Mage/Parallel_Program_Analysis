import java.util.concurrent.CyclicBarrier;

public class PandC {
    public static void main(String[] args) throws InterruptedException {

        //item to produce and consume
        Items item = new Items();

        //Producer Barrier
        CyclicBarrier Pbarrier = new CyclicBarrier(2, new Runnable() {
             public void run() {
                System.out.println("Producer has produced the item.");
             }
        });

        //Consumer Barrier
        CyclicBarrier Cbarrier = new CyclicBarrier(2, new Runnable() {
            public void run() {
                System.out.println("Consumer has consumed the item.");
            }
        });

        //producer thread
        Thread producer = new Thread(new Runnable() {
            public void run() {
                //initial production
                item.quantity = item.quantity+1;
                System.out.println("Item Quanitity: " + item.quantity);
                //signals production
                try {
                    Pbarrier.await();
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println(e);
                }

                while(true /*indefinite execution*/) {
                    //waits for consumption of item
                    try {
                        Cbarrier.await();
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    item.quantity = item.quantity+1;
                    System.out.println("Item Quanitity: " + item.quantity);
                    //signals production
                    try {
                        Pbarrier.await();
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        });

        Thread consumer = new Thread(new Runnable() {
            public void run() {
                while(true/*indefinite execution*/) {
                    //waits for producer to produce item
                    try {
                        Pbarrier.await();
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    item.quantity = item.quantity-1;
                    System.out.println("Item Quanitity: " + item.quantity);
                    //signals production
                    try {
                        Cbarrier.await();
                        Thread.sleep(500);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        });

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
    }
}

class Items {
    int quantity = 0;
}

