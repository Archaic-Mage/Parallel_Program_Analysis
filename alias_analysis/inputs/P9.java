class P9 {
    public static void main(String[] args) {
        int a;
        a=0;
        System.out.println(a);
    }
}

class LS {
    public int list() {
        Data a;
        Data b;
        Data g;
        Data f;
        Data j;
        int e;
        int d;
        int k;
        int[] c;
        boolean test;
        int t1;
        int t2;
        t1 = 0;
        t2 = 10;
        k = 2;
        c = new int[t2];
        f = new Data();
        b = new Data();
        g = new Data();
        e = 1;
        d = 3;
        //assignment
        a = b;
        //arrayassignment
        c[d] = e;
        //field assignment
        a.type = f;
        f.type = b;
        test = e < d;
        //if statement
        if(test) {
            a = new Data();
        } else {
            a = new Data();
            a.type = b;
        }
        //while
        while(test) {
            g = new Data();
            a.type = g;
            a = a.type;
        }
        //print
        System.out.println(e);
        d = t1 & t2;
        test = t1 < t2;
        d = e + k;
        d = e - k;
        d = e * k;
        d = c[e];
        e = c.length;
        j = new Data();
        d = j.foo(f, g);
        return e;
    }
}

class Data {
    int q;
    Data type;
    public int foo(Data b, Data c) {
        int a;
        Data g;
        g = new Data();
        a = 0;
        return a;
    }
}