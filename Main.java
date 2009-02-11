package simsalabim;

import java.io.*;
import java.util.*;

/**
 * The Main run class. The only arguments needed to run is the name of the
 * script file, and log file (which may be - ). Optionally a PRNG seed.
 */

public class Main {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage:  simsalabim.Main  <scriptfile> <settings> <logfile> [prng seed]");
			System.exit(1);
		}    

		int seed;
		if (args.length > 3)
			seed = Integer.parseInt(args[3]);
		else
			seed = (int) (System.currentTimeMillis() % 100000000l);

		System.err.println("Using PRNG seed: " + seed);

		RandomEngine re = new MersenneTwister(seed);

		Settings st = new Settings(args[1]);
		
		DataSpace ds = createDataSpace(st);

		Script sc;
		Simulator sim;
		PrintWriter out;

		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			sc = new Script(br);
			if (args[1].equals("-"))
				out = new PrintWriter(new OutputStreamWriter(System.out));
			else
				out = new PrintWriter(new FileWriter(args[2]));
			EventLogger ev = new StringEventLogger(out);

			sim = sc.makeSim(re, ds, st, ev);

			long rtime = System.currentTimeMillis();

			ScriptCommand scom;
			while ((scom = sc.nextCommand()) != null) {
				System.err.println("Executing command: " + scom);
				scom.execute(sim);
			}

			System.err.println("Simulation ran in: " 
					+ (System.currentTimeMillis() - rtime) + " ms.");

		} catch (IOException e) {
			System.err.println("IO Error reading script: " + e);
			System.exit(1);
			return; // for compiler
		} catch (ScriptException e) {
			System.err.println("Error in script: " + e.getMessage());
			System.exit(1);
			return; // for compiler
		}

		out.println(sim.info());
		out.println(ds.info());

		out.flush();
		out.close();
	}

	private static DataSpace createDataSpace(Settings st) {
		String name = st.get("dsType", "PopularityDataSpace");
		if (name.equals("LimitedDataSpace")) {
			return new LimitedDataSpace(st.getInt("dsMaxDocs", 10000), st.getInt(
					"dsMinSize", 1000), st.getInt("dsMaxSize", 2000));
			/* /* */

		} else if (name.equals("PopularityDataSpace")) {
			return new PopularityDataSpace(st.getInt("dsMaxDocs", 10000), st
					.getDouble("dsPopDecay", 1.0),
					st.getInt("dsMinSize", 1000), st.getInt("dsMaxSize", 2000));
		} else {
			throw new SimulationException("DataSpace type " + name
					+ " unknown.");
		}
	}

}
