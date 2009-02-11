package simsalabim;

/**
 * En efficient system for output from the simulator.
 */
public abstract class EventLogger {

	public abstract void nodeJoined(long time, Node n);

	public abstract void nodeJoinError(long time, Node n, int error);

	public abstract void dataPlaced(long time, Data d);

	public abstract void dataPlaceError(long time, Data d, int error);

	public abstract void dataFound(long time, Data d);

	public abstract void dataNotFound(long time, Key k, int error);

	public abstract void dataFindError(long time, Key k, int error);

	public abstract void warning(long time, int error);

	public abstract void message(long time, String message);

	public String error(int errnum) {
		if (errnum >= errors.length)
			return "Unknown";
		else
			return errors[errnum];
	}

	private static final String[] errors = { "Boohoo, I lost my mommy.", // 1
			"Iceage happened.", // 2
			"Oskar couldn't code to save his life.", // 3
			"Route established, but could not find data.", // 4
			"Routing failed." // 5
	};

}
