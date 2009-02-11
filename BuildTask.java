package simsalabim;

/**
 * Task which builds a simulates a living network.
 */

public class BuildTask extends RunTask {

	private double joinRate;

	private double leaveRate;

	private double searchRate;

	private double placeRate;

	private long poolSize;

	private long netSize = 1;

	private long tsearches = 0;

	private long ssearches = 0;

	private long tplaces = 0;

	private long splaces = 0;

	private long tsearchcost = 0;

	private long tplacecost = 0;

	/**
	 * The rates are per node per clocktick. They should be low (the sum of all
	 * the rates times poolSize should be less than << 1.
	 * 
	 * @param runTill
	 *            Run until this clockbeat
	 * @param searchRate
	 *            The rate at which each active node iniates searches
	 * @param placeRate
	 *            The rate at which each active node iniates placings.
	 * @param poolSize
	 *            The size of the pool of available nodes.
	 * @param joinRate
	 *            The rate at which each inactive node joins.
	 * @param leaveRate
	 *            The rate at which each
	 */
	public BuildTask(long runTill, double searchRate, double placeRate,
			int poolSize, double joinRate, double leaveRate) {

		super(runTill);

		this.searchRate = searchRate;
		this.placeRate = placeRate;
		this.poolSize = poolSize;
		this.joinRate = joinRate;
		this.leaveRate = leaveRate;

	}

	/**
	 * String constructor for script. The values should be in the same order as
	 * above.
	 */
	public BuildTask(String[] s) throws ScriptException {
		super(Long.parseLong(s[0]));

		// System.err.println(Main.tabbed(s));

		if (s.length < 6)
			throw new ScriptException("BuildTask requires six parameters, not "
					+ s.length);

		this.searchRate = Double.parseDouble(s[1]);
		this.placeRate = Double.parseDouble(s[2]);

		this.poolSize = Long.parseLong(s[3]);
		this.joinRate = Double.parseDouble(s[4]);
		this.leaveRate = Double.parseDouble(s[5]);

		// System.err.println(searchRate + " " + placeRate);

	}

	public void done(long time, EventLogger ev) {
		ev.message(time, "BuildTask Complete. " + tsearches + " searches ("
				+ ((float) ssearches) / tsearches + " succ, "
				+ ((double) tsearchcost) / tsearches + " cost)." + tplaces
				+ " places (" + ((float) splaces) / tplaces + " succ, "
				+ ((double) tplacecost) / tplaces + " cost).");
	}

	/**
	 * Calculates the time until a task.
	 */
	public long timeTillTask(RandomEngine re) {
		double rate = (poolSize - netSize) * joinRate + netSize
				* (searchRate + placeRate + leaveRate);
		// try { Thread.sleep(100); } catch (Exception e) {}
		// System.err.println("rate: " + rate + " netsize: " + netSize);
		// Exponentially distributed.
		return Math.max((long) (Math.log(re.nextDouble()) / (rate * -1)), 1);

	}

	/**
	 * Calculates the type of task to do after a call to time above.
	 */
	public int taskType(RandomEngine re) {
		double norm = (poolSize - netSize) * joinRate + netSize
				* (searchRate + placeRate + leaveRate);

		double val = re.nextDouble() * norm;

		double joinP = (poolSize - netSize) * joinRate;
		double searchP = netSize * searchRate;
		double placeP = netSize * placeRate;

		if (val <= joinP) {
			netSize++;
			return JOIN;
		} else if (val <= searchP + joinP) {
			return SEARCH;
		} else if (val <= searchP + joinP + placeP) {
			return PLACE;
		} else {
			netSize--;
			return LEAVE;
		}
	}

	public void feedback(int task, boolean success, double[] cost) {
		if (task == SEARCH) {
			tsearches++;
			ssearches += success ? 1 : 0;
			tsearchcost += cost[0];
		} else if (task == PLACE) {
			tplaces++;
			splaces += success ? 1 : 0;
			tplacecost += cost[0];
		}
	}

}
