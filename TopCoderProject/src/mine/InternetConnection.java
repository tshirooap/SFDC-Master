package mine;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class InternetConnection {

	public int connect(int floors, int[] connections){
		//sort
		Arrays.sort(connections);

		boolean found = false;
		for(int i = 0 ; i < connections.length; i++) { 
			if(floors == connections[i]) { 
				found = true;
				break;
			}
		}

		int routers = 0;
		int previous = connections[0];
		for(int i = 1; i < connections.length; i++) { 
			int dif = connections[i] - previous;
System.out.println("######## " + dif);
previous = connections[i];
			if(dif <= 2) { 
				continue;
			} else { 
				int div = dif / 2;
				routers = routers + div;
			}
			System.out.println("######## P " + previous);
		}
		
		return routers;
	}
	
	public static void main(String[] args) { 
//		int ret = new InternetConnection().connect(10, new int[]{1,3,5,10});
		int ret = new InternetConnection().connect(5, new int[]{1,2,3,4,5});
		System.out.println(ret);
	}
}
