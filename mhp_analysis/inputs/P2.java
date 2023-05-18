class P2 {
    public static void main(String[] args) {
        A a;
        A b;
        A c;
        int k;
        int l;
        Boolean f;
        a = new A();
        b = new A();
        c = new A();
        k = 5;
        l = 4;
        f = k < l;
        if(f) {
            a = new A();
            b = a;}
//        } else {
//            a = new A();
//            c = a;
//        }
        System.out.println(a.val);
        System.out.println(c.val);
        System.out.println(b.val);
    }
}

class A {
    A next;
    int val;
}