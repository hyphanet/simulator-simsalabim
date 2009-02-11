package simsalabim;

/**
 * Has a node join at a given rate.
 */

public class JoinTask extends RunTask {

	private double joinRate;

	private long tjoins = 0;

	private long tjoincost = 0;

	public JoinTask(long runTill, double joinRate) {

		super(runTill);

		this.joinRate = joinRate;
	}

	/**
	 * String constructor for script. The values should be in the same order as
	 * above.
	 */
	public JoinTask(String[] s) throws ScriptException {
		super(Long.parseLong(s[0]));

		if (s.length < 2)
			throw new ScriptException("JoinTask requires two parameters, not "
					+ s.length);
		this.joinRate = Double.parseDouble(s[1]);

	}

	public void done(long time, EventLogger ev) {
		ev.message(time, "JoinTask Complete. " + tjoins + " joins ("
				+ ((float) tjoincost) / tjoins + " cost).");
	}

	/**
	 * Calculates the time until a task.
	 */
	public long timeTillTask(RandomEngine re) {
		return Math
				.max((long) (Math.log(re.nextDouble()) / (joinRate * -1)), 1);

	}

	/**
	 * Calculates the type of task to do after a call to time above.
	 */
	public int taskType(RandomEngine re) {
		return JOIN;
	}

	public void feedback(int task, boolean success, double[] costarr) {
		int cost = (int) costarr[0];
		if (task == JOIN) {
			tjoins++;
			tjoincost += cost;
		}
	}

}
