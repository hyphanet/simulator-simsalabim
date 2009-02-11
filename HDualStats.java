package simsalabim;

/**
 * Print some stats comparing dual networks in a simulator
 */

public class HDualStats extends RunTask {

	int first, second;
	static int calls;
	double varDist1, varDist2, varDistFull, varDistMutual;

	/**
	 * String constructor for script. The values should be in the same order as
	 * above.
	 */
	public HDualStats(String[] s) throws ScriptException {
		super(Long.parseLong(s[0]));
		calls = 0;
		first = -1;
		second = -1;
		varDist1 = -1;
		varDist2 = -1;
		varDistFull = -1;
	}

	public void done(long time, EventLogger ev) {
		// ev.message(time, "HDualStats Complete. " + swaps_1 + " and " + swaps_2 + "swaps initiated in the networks. Swaps proposed between: " + cross_swaps + " of which " + cross_swaps_accepted + " were accepted.");

		ev.message(time, calls + "\tACTIVE1\tACTIVE2\tVARDIST1\tVARDIST2\tVARDISTFULL\tVARDISTMUTUAL");
		ev.message(time, "\t" + first + "\t" + second + "\t" +varDist1 +"\t" + varDist2 + "\t" + varDistFull);

	}

	/**
	 * Calculates the time until a task.
	 */
	public long timeTillTask(RandomEngine re) {
	    return 1;
	}

	/**
	 * Calculates the type of task to do after a call to time above.
	 */
	public int taskType(RandomEngine re) {
	    return DUALSTATS;
	}

	public void feedback(int task, boolean success, double[] costarr) {
		calls++;
		first = (int) costarr[0];
		second = (int) costarr[1];
		varDist1 = costarr[2];
		varDist2 = costarr[3];
		varDistFull = costarr[4];
		varDistMutual = costarr[5];
	}
}
