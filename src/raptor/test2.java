package raptor;

import java.util.Stack;

public class test2 {

	static Stack<Integer> stack = new Stack<Integer>();

	public static void main(String args[]) {
		System.out.println(whoKnows(5));
	}

	static int whoKnows(int n) {
		System.out.println("Winding stack");

		for (int i = n; i > 0; i--) {
			System.out.println("Pushing " + i + " onto the stack.");
			stack.push(i);
		}

		System.out.println("Unwinding stack");

		int result = 0;
		while (!stack.empty()) {
			int value = stack.pop();
			System.out.println("Popped " + value + " from the stack.");
			result += value;
		}

		System.out.println("The answer is " + result);

		return result;
	}

}
