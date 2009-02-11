package simsalabim.rembre;

import java.util.*;

public class test {

	static long M = 0x7FFFFFFFFFFFFFFFl;

	public static void main(String[] args) {
		Random r = new Random();

		long n1 = r.nextLong();

		int[] ct = new int[10];
		for (int i = 0; i < 10000; i++) {
			double val = ((double) (r.nextLong() & M) / (double) Long.MAX_VALUE);
			ct[(int) (val * 10)]++;
		}
		for (int i = 0; i < 10; i++) {
			System.err.println(ct[i] + "\t" + (ct[i] / 10000.0));
		}
	}

	public static String toBString(long n) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 64; i++) {
			sb.append(n & 1);
			n >>= 1;
			// System.err.println("t" + n + "\t" + Integer.toBinaryString(n));
		}
		return sb.reverse().toString();
	}

}
