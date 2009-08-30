package raptor.gui.board;

public class Test {
	static int[] b = new int[2];
	static double log2 = Math.log(2.0);

	public static int testIf(int x,int a) {
		if (x != 0) {
			return a++;
		}
		else {
			return a;
		}
	}
	
//	static int conditionalIncrement(int x, int a)
//	{
//		b[0] = a;
//		int s = (int)(Math.log(x)/log2);
//		x >>>= s;
//		b[x]++;
//		return b[0];
//	}

	static int conditionalIncrement(int x, int a)
	{
		return 1 - a + x;
	}
	
	public static void main(String args[]) {
		if (testIf(1,1) != conditionalIncrement(1,1) && 
		    testIf(0,1) != conditionalIncrement(0,1)) {
			throw new AssertionError();
		}
		else {
			int n = 1000000;
			long accumulator = 0;
			for (int i = 0; i < n; i++) {
				long start = System.nanoTime();
				conditionalIncrement(1,1);
				conditionalIncrement(0,1);
				accumulator += System.nanoTime() - start;				
			}

			
			System.out.println("conditionalIncrement avg=" + accumulator/n);
			
			accumulator = 0;
			for (int i = 0; i < n;i++) {
				long start = System.nanoTime();
				accumulator += System.nanoTime() - start;				
			}
			System.out.println("numberOfTrailingZeros avg=" + accumulator/n);	
			
			accumulator = 0;
			for (int i = 0; i < n;i++) {
				long start = System.nanoTime();
				accumulator += System.nanoTime() - start;				
			}
			System.out.println("[] alloc avg=" + accumulator/n);	
			
			accumulator = 0;
			for (int i = 0; i < n;i++) {
				long start = System.nanoTime();
				testIf(1,1);
				testIf(0,1); 
				accumulator += System.nanoTime() - start;				
			}
			
			System.out.println("if avg=" + accumulator/n);
		}
	}
}
