class P1 {
	public static void main(String[] args) {
		try {
			 A x;
			 A y;
			 P1 z;
			 
			 x = new A();
			 y = new A(); 
			 z = new P1(); 
			 x.start();
//			 x.f1 = z;
			 synchronized (x) {
				 x.f1 = z;
				 x.wait();
				 y.f1 = z;
			 }
			 //x.f1 = z;
//			 y.start();
//			 y.join();
			 x.join();
			 
			}catch (Exception e) {
					
			} 
	}
}
	 
class A extends Thread{
		P1 f1;
		
		public void run() {
			try {
				A a;
				P1 b;
				a = this;
				synchronized (a) {
					b = new P1();
					a.f1 = b;
					notify();
				}
			}catch(Exception e) {
				
			}
		}
	}

