
class P4 {
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
            ptr = x;
            while(f) {
                z = new A();
                ptr.next = z;
                ptr = ptr.next;
            }
            y = z;
            x = ptr;
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
    A next;
    public void run() {
        try {
            A a;
            A b;
            a = this;
            synchronized(a) {
                b = new A();
                a.f = b;
            }
        }catch(Exception e) {

        }
    }
}
