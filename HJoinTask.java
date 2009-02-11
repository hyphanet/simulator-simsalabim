package simsalabim;

/**
 * Has a node join at every timestep (thus call with number of nodes to join from the underlying social network)
 */

public class HJoinTask extends RunTask {

	private double joinRate;

	private double mRate;

	private long tjoins = 0;

	private long tjoincost = 0;

	private long tmaint = 0;

	private long tmaintcost = 0;

	/**
	 * String constructor for script. The values should be in the same order as
	 * above.
	 */
	public HJoinTask(String[] s) throws ScriptException {
		super(Long.parseLong(s[0]));

		if (s.length < 1)
			throw new ScriptException("JoinTask requires one parameter, not "
						  + s.length);

	}

	public void done(long time, EventLogger ev) {
		ev.message(time, "HJoinTask Complete. " + tjoins + " joins ("
				+ ((float) tjoincost) / tjoins + " cost)." + tmaint
				+ " maintenance.");
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
	    return JOIN;
	    /* return re.nextDouble() < (joinRate / (joinRate + mRate)) ? JOIN
				: MAINTENANCE;
	    */
	}

	public void feedback(int task, boolean success, double[] costarr) {
		int cost = (int) costarr[0];

		if (task == JOIN) {
			tjoins++;
			tjoincost += cost;
		} else if (task == MAINTENANCE) {
			tmaint++;
			tmaintcost += cost;
		}
	}
}