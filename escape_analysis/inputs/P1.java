
class P1 {
	public static void main(String[] args) {
		try {
			 A x;
			 A y;
			 A z;
			 
			 x = new A();
			 y = new A();
			 synchronized(y) {
				 z = new A();
				 x.f = z;
				 x.start();
				 x.join();
				} 
			}catch (InterruptedException e) {
					
			} 
	}
}
	 
	class A extends Thread{
		A f;
		
		public void run() {
			try {
				A a;
				A b;
				a = this;
				synchronized(a) {
					b = new A();
					a.f = b;
				}
			}catch(Exception e) {
				
			}
		}
	}
