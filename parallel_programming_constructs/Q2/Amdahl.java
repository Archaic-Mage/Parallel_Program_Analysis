class Amdahl {
    public static void main(String[] args) throws InterruptedException{

        long startTime;
        long endTime;
        long duration;

        startTime = System.nanoTime();

        //serial part
        long k = 0;
        while(k < 1e10) k++;

        Thread[] t = new Thread[8];

        //Parallelizable Part
        for(int i = 0; i<8; i++) t[i] = new Thread(new HeavyWork());

        for(int i = 0; i<8; i++) t[i].start();
        
        for(int i = 0; i<8; i++) t[i].join();


        endTime = System.nanoTime();

        duration = endTime-startTime;

        System.out.println((double) duration/1000000000);

    }
}

class HeavyWork implements Runnable {
    public void run(){
        long k = 0;
        while(k < 1e10) k++;
    }
}