//allocations only
class P2 {
    public static void main(String[] args) {
        int a;
        a=0;
        System.out.println(a);
    }
}

class Alloc {
    public int list() {
        Data a;
        Data b;
        Data c;
        Data d;
        Data e;
        int k;
        int j;
        boolean f;
        k = 0;
        j = 1;
        a = new Data();
        b = new Data();
        k = k+k;
        f = k < j;
        if(f) {
            c = a;
            d = new Data();
        } else {
            c = b;
            d = new Data();
        }
        e = d;
        d = c;
        return k;
    }
}

class Data {
    int q;
    Data type;
}