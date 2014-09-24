package ans;

public class InternetConnection {

	public int connect(int floors, int[] connections) {
	    // Floors below the first router.
	    int ret = (connections[0] - 1) / 2;
	  
	    // Each gap between existing routers.
	    for (int i = 0; i < connections.length - 1; i++)
	        ret += (connections[i + 1] - connections[i] - 1) / 2;
	  
	    // Finally, any floors above the last router.
	    ret += (floors - connections[connections.length - 1]) / 2;
	  
	    // Return our answer.
	    return ret;
	}

	public static void main(String[] args) { 
		InternetConnection con = new InternetConnection();
		int ret = con.connect(10, new int[]{3,5});
		System.out.println("####### ret "  +ret);
	}
}
