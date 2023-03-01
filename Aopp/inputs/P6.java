// Special cases

class P1 {
	public static void main(String[] args) {
		int a;
		a = 10;
		System.out.println(a);
	}
}

class Test {
	public int list() {
		A head;
		A temp;
		A ptr;
		A x;
		A y;
		int i;
		int k;
		int l;
		int inc;
		k = 4;
		l = 2;
		inc = 1;
		head = new A();
		ptr = head;
		for (i = 0; i < k; i = i + inc) {
			x = new A();
			ptr.data = i;
			ptr.next = x;
			ptr = ptr.next;
		}
		y = head.next;
		x = y.next;
		k = 0;
		for (ptr = head; k < l; ptr = ptr.next) {
			x = ptr;
			temp = ptr.next;
			k = temp.data;
		}
		return inc;
	}
}

class A {
	int data;
	A next;
}
