package mine;

import java.util.Arrays;

public class TheCoinsProblem {
	public int calculate(int[] values) { 
		Arrays.sort(values);
		final int maxArrayIndex = values.length - 1;
		for(int i = maxArrayIndex; i > 0 ; i--) { 
			int rem = values[i] % values[i - 1];
			System.out.println("values[i] " + values[i]);
			System.out.println("values[i-1] " + values[i-1]);
			System.out.println(rem);
			if(rem == 0) { 
				return -1;
			}
		}
		
		int i = 2;
		while(true) {
			int val = values[0] * i;
			if(val > values[maxArrayIndex]) { 
				return val;
			} else { 
				i++;
			}
		}
		
	}

	
	public static void main(String[] args) { 
		int ret1 = new TheCoinsProblem().calculate(new int[]{1,5,10,25,50});
		int ret2 = new TheCoinsProblem().calculate(new int[]{10, 15});
		int ret3 = new TheCoinsProblem().calculate(new int[]{1,7,10});
		int ret4 = new TheCoinsProblem().calculate(new int[]{2,3});
		int ret5 = new TheCoinsProblem().calculate(new int[]{994,995,996,997,998,999,1000});
		int ret6 = new TheCoinsProblem().calculate(new int[]{1});
		
		
		
		
		System.out.println("## ret1 = " + ret1);
		System.out.println("## ret2 = " + ret2);
		System.out.println("## ret3 = " + ret3);
		System.out.println("## ret4 = " + ret4);
		System.out.println("## ret5 = " + ret5);
		System.out.println("## ret6 = " + ret6);
	}
}
