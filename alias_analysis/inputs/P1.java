
class P1 {
	public static void main(String[] args) {
		int a;
		a=0;
		System.out.println(a);
	}
}

class A2 {
	public int m1() {
		A x;
		A y;
		int a;
		a=0;
		x = new A();
		y = new A();
		a = x.foo();
		a = y.foo();
		return a;
	}
	public int m2() {
		A a;
		A b;
		A z;
		int k;
		boolean c;
		k = 0;
		c = true;
		a = new A();
		b = new A();
		if(c) z = a;
		else z = b;
		k = z.foo();
		return k;
	}
}
class A{
	public int foo() {
		int a;
		a=10;
		System.out.println(a);
		return a;
	}
}
