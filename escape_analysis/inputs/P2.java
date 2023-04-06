
class P2 {
    public static void main(String[] args) {
        try {
            A x;
            A y;
            B z;

            x = new A();
            y = new A();
            z = new B();
            x.f = z;
            synchronized(y) {
                z = new B();
                x.f = z;
                x.start();
                x.join();
            }
        }catch (InterruptedException e) {

        }
    }
}

class B {
    B f;
    public void some() {
        B b;
        b = new B();
        b.f = new B();
    }
}

class A extends Thread{
    B f;

    public void run() {
        try {
            A a;
            B b;
            a = this;
            synchronized(a) {
                b = new B();
                a.f = b;
            }
        }catch(Exception e) {

        }
    }
}
