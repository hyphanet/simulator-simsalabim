package simsalabim.utils;

import simsalabim.*;
import java.util.*;

public class RandTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RandomEngine re = new MersenneTwister((int) System.currentTimeMillis());
		int N = 300; 
		int[] f = new int[N];
		int[] k = new int[N];
		for (int i = 0 ; i < N ; i++)
			k[i] = i;
		HashSet<Integer> hs = new HashSet<Integer>();
		for (int i = 0 ; i < 100 * N ; i++) {			
			int num = (int) Math.pow(N, re.nextDouble());
			int lowest = N+1;

			for (int j = 0 ; j < num ; j++) {
				int idx = j + re.nextInt(N - j);
				int t = k[j];
				k[j] = k[idx];
				k[idx] = t;
				if (k[j] < lowest)
					lowest = k[j];
			}
			f[lowest]++;
			hs.clear();
		}
		
		StringBuffer sb = new StringBuffer();
		for (int n : f)
			sb.append(n).append(" ");
		System.err.println(sb.toString());
	}
}
