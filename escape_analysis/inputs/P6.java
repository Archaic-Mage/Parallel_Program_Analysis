class P6 {
    public static void main(String[] args) {

        A a;
        A b;
        a = new B();
        a.bar();
        b = a.f;
        synchronized (b) {

        }

    }
}

class A {
    A f;
    public void foo() {

    }
    public void bar() {
        try {
            T t;
            A temp;
            t = new T();
            t.start();
            temp = new A();
            this.f = temp;
            temp = this.f;
            t.join();
        } catch (InterruptedException e) {

        }
    }
}

class B extends A {
    A g;
    @Override
    public void foo() {

    }
}

class T extends Thread {
    A k;
    @Override
    public void run() {
        try {
            A a;
            T s;
            this.k = new B();
            a = this.k;
            s = new T();
            synchronized (a) {

            }
        } catch (Exception e) {

        }
    }
}