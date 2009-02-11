package simsalabim;

/**
 * Task which builds a simulates a living network.
 */

public class MBuildTask extends RunTask {

	private double joinRate;

	private double leaveRate;

	private double searchRate;

	private double placeRate;

	private double maintRate;

	private long poolSize;

	private long tsearches = 0;

	private long ssearches = 0;

	private long tplaces = 0;

	private long splaces = 0;

	private long tmaint = 0;

	private long tjoin = 0;

	private long tleave = 0;

	private long tsearchcost = 0;

	private long ssearchcost = 0;

	private long tplacecost = 0;

	private long splacecost = 0;

	private long tmaintcost = 0;

	/**
	 * The rates are per node per clocktick. They should be low (the sum of all
	 * the rates times poolSize should be less than << 1.
	 * 
	 * @param runTill
	 *            Run until this clockbeat
	 * @param searchRate
	 *            The rate at which each active node initiates searches
	 * @param placeRate
	 *            The rate at which each active node initiates placings.
	 * @param poolSize
	 *            The size of the pool of available nodes.
	 * @param joinRate
	 *            The rate at which each inactive node joins.
	 * @param leaveRate
	 *            The rate at which each
	 * @param maintRate
	 *            The rate at which to run maintenance.
	 */
	public MBuildTask(long runTill, double searchRate, double placeRate,
			int poolSize, double joinRate, double leaveRate, double maintRate) {

		super(runTill);

		this.searchRate = searchRate;
		this.placeRate = placeRate;
		this.poolSize = poolSize;
		this.joinRate = joinRate;
		this.leaveRate = leaveRate;
		this.maintRate = maintRate;

	}

	/**
	 * String constructor for script. The values should be in the same order as
	 * above.
	 */
	public MBuildTask(String[] s) throws ScriptException {
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
		this.maintRate = Double.parseDouble(s[6]);

		// System.err.println(searchRate + " " + placeRate);

	}

	public void started(long time, EventLogger ev) {
		ev.message(time, "BuildTask Started. Poolsize " + poolSize
				+ " join/leave rate: " + joinRate + "/" + leaveRate
				+ ". Insert/request rate: " + placeRate + "/" + searchRate
				+ ". Maintenance rate: " + maintRate);
	}

	public void done(long time, EventLogger ev) {
		ev.message(time, "BuildTask Complete. " + tsearches + " searches ("
				+ ((float) ssearches) / tsearches + " succ, "
				+ ((double) tsearchcost) / tsearches + " cost)." + tplaces
				+ " places (" + ((float) splaces) / tplaces + " succ, "
				+ ((double) tplacecost) / tplaces + " cost)." + tjoin
				+ " joins, " + tleave + " leave," + tmaint
				+ " maintenance tasks.");
	}

	/**
	 * Calculates the time until a task.
	 */
	public long timeTillTask(Simulator s) {
		RandomEngine re = s.random();

		long joinLeft = Math.max(0, poolSize - s.netSize());
		int netSize = s.netSize();

		double rate = joinLeft * joinRate + netSize
				* (searchRate + placeRate + leaveRate + maintRate);
		// try { Thread.sleep(100); } catch (Exception e) {}
		// System.err.println("rate: " + rate + " netsize: " + netSize);
		// Exponentially distributed.
		return Math.max((long) (Math.log(re.nextDouble()) / (rate * -1)), 1);

	}

	/**
	 * Calculates the type of task to do after a call to time above.
	 */
	public int taskType(Simulator s) {
		RandomEngine re = s.random();
		long joinLeft = Math.max(0, poolSize - s.netSize());
		int netSize = s.netSize();

		double joinP = joinLeft * joinRate;
		double leaveP = netSize * leaveRate;
		double searchP = netSize * searchRate;
		double placeP = netSize * placeRate;
		double maintP = netSize * maintRate;

		double norm = joinP + searchP + placeP + maintP + leaveP;
		double val = re.nextDouble() * norm;

		if (val <= joinP) {
			return JOIN;
		} else if (val <= searchP + joinP) {
			return SEARCH;
		} else if (val <= searchP + joinP + placeP) {
			return PLACE;
		} else if (val <= searchP + joinP + placeP + maintP) {
			return MAINTENANCE;
		} else {
			return LEAVE;
		}
	}

	public void feedback(int task, boolean success, double[] costarr) {
		int cost = (int) costarr[0];

		if (task == SEARCH) {
			tsearches++;
			ssearches += success ? 1 : 0;
			tsearchcost += cost;
		} else if (task == PLACE) {
			tplaces++;
			splaces += success ? 1 : 0;
			tplacecost += cost;
		} else if (task == MAINTENANCE) {
			tmaint++;
			tmaintcost += cost;
		} else if (task == LEAVE) {
			tleave++;
		} else if (task == JOIN) {
			tjoin++;
		}
	}

}
