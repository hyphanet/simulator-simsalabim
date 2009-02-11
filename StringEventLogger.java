package simsalabim;

import java.io.PrintWriter;

public class StringEventLogger extends EventLogger {

	public PrintWriter pw;

	public StringEventLogger(PrintWriter pw) {
		this.pw = pw;
	}

	public void nodeJoined(long time, Node n) {
		pw.println(time + "\tNew Node Joined: " + n.toString());
	}

	public void nodeJoinError(long time, Node n, int error) {
		pw.println(time + "\tJoin error for node: " + n.toString() + " error "
				+ error + ": " + error(error));
	}

	public void dataPlaced(long time, Data d) {
		pw.println(time + "\tData Placed: " + d.toString());
	}

	public void dataPlaceError(long time, Data d, int error) {
		pw.println(time + "\tPlace error for data: " + d.toString() + " error "
				+ error + ": " + error(error));
	}

	public void dataFound(long time, Data d) {
		pw.println(time + "\tData Found: " + d.toString());
	}

	public void dataNotFound(long time, Key k, int error) {
		pw.println(time + "\tData Not Found: " + k.toString() + " error "
				+ error + ": " + error(error));
	}

	public void dataFindError(long time, Key k, int error) {
		pw.println(time + "\tFind error for key: " + k.toString() + " error "
				+ error + ": " + error(error));
	}

	public void warning(long time, int error) {
		pw.println(time + "\tWARNING error " + error + ": " + error(error));
		pw.flush();
	}

	public void message(long time, String message) {
		pw.println(time + "\tMESSAGE: " + message);
		pw.flush();
	}

}
