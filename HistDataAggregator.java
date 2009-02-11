package simsalabim;

import java.util.Vector;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;

public class HistDataAggregator extends DataAggregator {

	private int[] hist;

	private double start;

	private double step;

	private double end;

	private PrintStream out;

	private String fname;

	public HistDataAggregator(String name, double start, double step, double end) {
		super(name);
		initHist(start, step, end);
		out = System.out;
	}

	public HistDataAggregator(String name, double start, double step,
			double end, String fname) throws ScriptException {
		super(name);
		initHist(start, step, end);
		this.fname = fname;
	}

	private void initHist(double start, double step, double end) {
		this.start = start;
		this.step = step;
		this.end = end;

		hist = new int[((int) ((end - start) / step))];
	}

	public void next(long data) {
		next((double) data);
	}

	public void next(double data) {
		if (data < start || data >= end) {
			System.err.println("WARNING: HIST DATA OUT OF BOUNDS: !" + start
					+ " < " + data + " < " + end);
		}

		hist[(int) Math.ceil((data - start) / step) - 1]++; // not so nice
	}

	public void complete() {
		PrintStream ps;

		if (fname != null) {
			try {
				ps = new PrintStream(new FileOutputStream(fname));
			} catch (IOException e) {
				throw new SimulationException("File error : " + e);
			}
		} else {
			ps = this.out;
		}

		print(ps);

		if (fname != null)
			ps.close();
	}

	public void print(PrintStream ps) {
		ps.println("#Created by Simsalabim. Data: " + name());
		ps.println("#name: " + name() + "_y");
		ps.println("#type: matrix");
		ps.println("#rows: " + hist.length);
		ps.println("#columns: 1");

		for (int i = 0; i < hist.length; i++) {
			ps.println(hist[i]);
		}

		ps.println("#Created by Simsalabim. Data: " + name());
		ps.println("#name: " + name() + "_x");
		ps.println("#type: matrix");
		ps.println("#rows: " + hist.length);
		ps.println("#columns: 1");

		for (int i = 0; i < hist.length; i++) {
			ps.println(start + step / 2.0 + i * step);
		}
	}

	public void print(PrintWriter pw) {
		pw.println("#Created by Simsalabim. Data: " + name());
		pw.println("#name: " + name() + "_y");
		pw.println("#type: matrix");
		pw.println("#rows: " + hist.length);
		pw.println("#columns: 1");

		for (int i = 0; i < hist.length; i++) {
			pw.println(hist[i]);
		}

		pw.println("#Created by Simsalabim. Data: " + name());
		pw.println("#name: " + name() + "_x");
		pw.println("#type: matrix");
		pw.println("#rows: " + hist.length);
		pw.println("#columns: 1");

		for (int i = 0; i < hist.length; i++) {
			pw.println(start + step / 2.0 + i * step);
		}
	}

}
