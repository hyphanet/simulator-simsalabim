package simsalabim;

public abstract class RunTask implements ScriptCommand, Feedback {

	private static final String DELIM;
	static {
		StringBuffer sb = new StringBuffer("|");
		for (int i = 0; i < 78; i++)
			sb.append('-');
		sb.append('|');
		DELIM = sb.toString();
	}

	public static final int JOIN = 0;

	public static final int SEARCH = 1;

	public static final int PLACE = 2;

	public static final int LEAVE = 3;

	public static final int GSEARCH = 4;

	public static final int MAINTENANCE = 5;

	public static final int VERYUGLYHACK = 6;

        public static final int DUALSTATS = 7;

	public long runTill;

	public RunTask(long runTill) {
		this.runTill = runTill;
	}

	/**
	 * Calculates the time until a task. Default version calls with
	 * RandomEngine.
	 */
	public long timeTillTask(Simulator s) {
		return timeTillTask(s.random());
	}

	/**
	 * Calculates the type of task to do after a call to time above. Default
	 * version calls with randomEngine
	 */
	public int taskType(Simulator s) {
		return taskType(s.random());
	}

	/**
	 * This or timeTillTask(Simulator) must be overridden. Default throws error.
	 */
	protected long timeTillTask(RandomEngine re) {
		throw new Error("TimeTillTask unimplemented in " + this.getClass());
	}

	/**
	 * This or taskType(Simulator) must be overriden. Default throws error.
	 */
	protected int taskType(RandomEngine re) {
		throw new Error("taskType unimplemented in " + this.getClass());
	}

	/**
	 * Called when this is started.
	 */
	public void started(long time, EventLogger ev) {
	}

	/**
	 * Called when this has finished running.
	 */
	public void done(long time, EventLogger ev) {
	}

	public long runTill() {
		return runTill;
	}

	public final void execute(Simulator s) {
		long runTill = runTill();
		long startTime = s.time();
		long runTime = runTill - s.time();

		DataSpace ds = s.dataspace();
		int ntasks = 0;
		long realTime = System.currentTimeMillis();
		System.err.println("Task " + this);
		System.err.println(DELIM);
		started(s.time(), s.log());
		while (s.time() < runTill) {
			long col = 80 * (s.time() - startTime) / runTime;
			s.step(timeTillTask(s));
			
			long ncol = 80 * (s.time() - startTime) / runTime;

			if (ncol > col)
				System.err.print("+");

			// System.err.println(time);

			if (s.time() >= runTill)
				break;

			int t = taskType(s);
			ntasks++;

			if (t == RunTask.JOIN) {
				// System.err.println("Join! \t");
				Node n = s.newNode();
				// if (nodes.isEmpty()) {
				// nodes.add(n);
				// } else {
				boolean r = s.join(n, s.chooseNode(), this);
				if (r) {
					s.addNode(n);
					n.activate();
				}
				// }
			} else if (t == RunTask.SEARCH || t == RunTask.GSEARCH) {
				Node n = s.chooseNode(); 
				if (n != null) {
					Key k = s.chooseKey();
					if (k != null) {
						Data d = s.search(k, n, t == RunTask.GSEARCH, this);
						if (d != null)
							d.retrieved();
					}
				}
			} else if (t == RunTask.PLACE) {
				Node n = s.chooseNode();
				if (n != null) {
					Data<Key> d = ds.newData(s, s.newKey());

					s.place(d, n, this);
				}
			} else if (t == RunTask.LEAVE) {
				// System.err.println("Leave! \t");
				Node n = s.chooseNode();
				if (n != null) {
				    // Remove from active
				    s.removeNode(n);              
				    s.leave(n, this, s.leavePermanent(n));
				    n.deactivate();

				}
			} else if (t == RunTask.MAINTENANCE) {
				s.maintenance(this);
			} else if (t == RunTask.DUALSTATS) {
				s.dualstats(this);
			}
		}
		System.err.println();
		System.err.println("Task " + this + " (with " + ntasks + " attempted tasks) complete in " + (System.currentTimeMillis() - realTime) + "ms.");
		done(s.time(), s.log());
	}
}
