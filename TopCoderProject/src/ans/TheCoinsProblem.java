package ans;

public class TheCoinsProblem {

	public int calculate(int[] values) {
	    // Max coin is 1000
	    boolean[] greedy = new boolean[2001]; // The "greedy" way
	    boolean[] dp = new boolean[2001];     // The "right" way
	    greedy[0] = dp[0] = true;  // We can always make a total of 0.
	    for (int i = 1; i < 2001; i++) {
	        for (int j = 0; j < values.length; j++)
	            // For each coin c, if we can make x - c, then we can make x
	            if (values[j] <= i && dp[i - values[j]]) dp[i] = true;
	        // Find the largest coin less than or equal to the total we are
	        // trying to make, i.e. the greedy approach
	        int j = -1;
	        while (j + 1 < values.length && values[j + 1] <= i) j++;
	        // Having found coin c, if we can make x - c, then we can make x
	        if (j > -1) greedy[i] = greedy[i - values[j]];
	        // Did we find a case where greedy fails us?
	        if (dp[i] && !greedy[i]) return i;
	    }
	    // Did not find a case where greedy fails
	    return -1;
	}

	public static void main(String[] args) { 
		TheCoinsProblem coin = new TheCoinsProblem();
		//int ret = coin.calculate(new int[]{1,5,10,50});
		int ret = coin.calculate(new int[]{15,5});
		System.out.println("###### " + ret);
	}
}
