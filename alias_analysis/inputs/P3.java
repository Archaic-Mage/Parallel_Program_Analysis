//for loading type statements and storing type
class P3 {
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
        Data c;
        Data d;
        Data e;
        Data g;
        int k;
        int j;
        boolean f;
        k = 0;
        j = 1;
        a = new Data();
        b = new Data();
        e = new Data();
        a.type = b;
        b.type = a;
        k = k+k;
        f = k < j;
        if(f) {
            c = a;
            d = new Data();
        } else {
            c = b;
            d = a.type;
        }
        d.type = e;
        g = b.type;
        return k;
    }
}

class Data {
    int q;
    Data type;
}