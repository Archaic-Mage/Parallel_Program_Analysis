
class P5 {
    public static void main(String[] args) {
        try {
            A x;
            A y;
            A z;

            x = new A();
            y = new A();
            x.f = y;
            z = y.something(x);
            synchronized(y) {
                z = new A();
                x.f = z;
                x.start();
                x.join();
            }
        }catch (InterruptedException e) {

        }
    }
}

class A extends Thread{
    A f;

    public A something(A a) {
        try {
            A b;
            A c;
            b = a.f;
            b.f = a;
            c = new A();
            c.f = b;
            c.start();
            c.join();
            return c;
        } catch (InterruptedException e) {

        }
    }

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
