//for loading type statements and storing type
class P7 {
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
        int i;
        boolean f;
        int j;
        i = 1;
        j = 0;
        f = i < j;
        a = new Data();
        d = a;
        b = new Data();
        j = a.foo();
        if(f) {
            a = new Data();
        }
        a.type = b;
        c = a.type;
        return j;
    }
}

class Data {
    int q;
    Data type;
    public int foo() {
        int a;
        a = 0;
        return a;
    }
}