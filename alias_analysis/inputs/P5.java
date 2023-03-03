//function calls
class P5 {
    public static void main(String[] args) {
        int a;
        a=0;
        System.out.println(a);
    }
}

class Data {
    Data d;
    public int set(Data c) {
        int i;
        i= 0;
        d = new Data();
        return i;
    }
    public Data get(Data a) {
        return d;        
    }
}

class MData {
    int d;
    Data type;
}

class FC extends Data {
    public int m1() {
        Data a;
        Data b;
        MData c;
        Data d;
        Data i;
        int j;
        a = new Data();
        i = this.get(a);
        j = this.set(a);
        b = this.get(a);
        c = new MData();
        d = new Data();
        c.type = d;
        return j;
    }
}

