package simsalabim;

/**
 * Has a node join at a given rate, with maintenance procedure inbetween.
 */

public class MJoinTask extends RunTask {

	private double joinRate;

	private double mRate;

	private long tjoins = 0;

	private long tjoincost = 0;

	private long tmaint = 0;

	private long tmaintcost = 0;

	public MJoinTask(long runTill, double joinRate, double mRate) {
		super(runTill);

		this.joinRate = joinRate;
		this.mRate = mRate;
	}

	/**
	 * String constructor for script. The values should be in the same order as
	 * above.
	 */
	public MJoinTask(String[] s) throws ScriptException {
		super(Long.parseLong(s[0]));

		if (s.length < 2)
			throw new ScriptException("JoinTask requires two parameters, not "
					+ s.length);
		this.joinRate = Double.parseDouble(s[1]);
		this.mRate = Double.parseDouble(s[2]);
	}

	public void done(long time, EventLogger ev) {
		ev.message(time, "MJoinTask Complete. " + tjoins + " joins ("
				+ ((float) tjoincost) / tjoins + " cost)." + tmaint
				+ " maintenance.");
	}

	/**
	 * Calculates the time until a task.
	 */
	public long timeTillTask(RandomEngine re) {
		return Math.max(
				(long) (Math.log(re.nextDouble()) / ((joinRate + mRate) * -1)),
				1);

	}

	/**
	 * Calculates the type of task to do after a call to time above.
	 */
	public int taskType(RandomEngine re) {
		return re.nextDouble() < (joinRate / (joinRate + mRate)) ? JOIN
				: MAINTENANCE;
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
