package simsalabim;

/**
 * A simple interface whereby a task can give feedback on it's process for
 * study.
 */
public interface Feedback {

	/**
	 * @param task
	 *            The task that has been run (@see RunTask for numbers).
	 * @param success
	 *            The success state.
	 * @param cost
	 *            The cost of the task (typically the number of nodes involved)
	 * @see RunTask
	 */
	public abstract void feedback(int task, boolean success, double[] msg);

}
