package simsalabim;

/**
 * Exceptions that signify a broken simulation.
 */
public class SimulationException extends RuntimeException {

	public SimulationException() {
	}

	public SimulationException(String s) {
		super(s);
	}

}
