
class P1 {
  void m1() {
	 A x,y;
	 x = new A();
	 y = new A();
	 x.foo();
	 y.foo();
	 
 }
  void m2() {
	  A a,b,z;
	  boolean c = true;
		 a = new A();
		 b = new A();
		 if(c)
		  z = a;
		 else
			z = b;
		 z.foo();
 }
}
class A{
	void foo() {
		System.out.println(10);
	}
}
