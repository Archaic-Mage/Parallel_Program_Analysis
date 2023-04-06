
class P3 {
    public static void main(String[] args) {
        try {
            A x;
            A y;
            A z;
            int i;
            int k;
            boolean f;
            k = 2;
            i = 1;
            f = k < i;
            x = new A();
            y = new A();
            x.f = y;
            if(f) {
                y = new A();
            } else {
                x = new A();
            }
            synchronized(y) {
                z = new A();
                x.start();
                x.join();
            }
        }catch (InterruptedException e) {

        }
    }
}

class A extends Thread{
    A f;

    public void run() {
        try {
            A a;
            A b;
            a = this;
            b = new A();
            synchronized(b) {
                a.f = b;
            }
        }catch(Exception e) {

        }
    }
}
