//accessing functions fields
class P4 {
    public static void main(String[] args) {
        int a;
        a=0;
        System.out.println(a);
    }
}

class Global {
    Data o1;
    public int m1() {
        Data a;
        Data b;
        Data c;
        Data d;
        Data e;
        int r;
        int i;
        boolean g;
        a = new Data();
        b = o1;
        d = a.type;
        r = 0;
        i = 1000;
        c = b.type;
        g = r < i;
        while(g) {
            c = c.type;
            r = r+r;
            g = r<i;
        }
        return r;
    }
}

class Second_Global extends Global {
    public int m2(Data u) {
        Data a;
        Data b;
        Data c;
        Data d;
        int r;
        int i;
        boolean g;
        a = new Data();
        b = u;
        d = a.type;
        r = 0;
        i = 1000;
        c = b.type;
        g = r < i;
        while(g) {
            c = c.type;
            r = r+r;
            g = r<i;
        }
        return r;
    }
}

class Data {
    int q;
    Data type;
}
