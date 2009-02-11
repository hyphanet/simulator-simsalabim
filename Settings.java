package simsalabim;
import java.util.*;
import java.io.*;

public class Settings {
	private HashMap<String, String> hm = new HashMap<String, String>();

	public Settings(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			String s;
			while ((s = br.readLine()) != null) {
				if (!s.trim().startsWith("#") && !"".equals(s.trim())) {
					int j = s.indexOf('=');
					hm.put(s.substring(0,j).trim(),s.substring(j+1).trim());
				}
			}
		} catch (IOException e) {
			System.err.println("Exception reading settings: " + e);
		}
	}
	
	/** Creates an empty Settings object. */
	public Settings() {
	}

	public boolean isSet(String s) {
		return hm.containsKey(s);
	}

	/** Default 0 * */
	public int getInt(String s) {
		return getInt(s, 0);
	}

	public int getInt(String s, int def) {
		if (!hm.containsKey(s)) {
			System.err.println("Using default value " + s + " = " + def);
			return def;
		}
		try {
			return Integer.parseInt(hm.get(s));
		} catch (NumberFormatException e) {
			System.err.println("Numeric setting " + s + " malformatted.");
			return def;
		}
	}


	/** Default 0.0 * */
	public double getDouble(String s) {
		return getDouble(s, 0.0);
	}

	public double getDouble(String s, double def) {
		if (!hm.containsKey(s)) {
			System.err.println("Using default value " + s + " = " + def);
			return def;
		}
		try {
			return Double.parseDouble(hm.get(s));
		} catch (NumberFormatException e) {
			System.err.println("Numeric setting " + s + " malformatted.");
			return def;
		}
	}

	/** Default null * */
	public String get(String s) {
		return hm.get(s);
	}

	public String get(String s, String def) {
		String r = hm.get(s);
		if (r == null) {
			System.err.println("Using default value " + s + " = " + def);
			r = def;
		}
		return r;
	}
	

	/** Lines start with ls * */
	public String writeOut(String ls) {
		StringBuffer sb = new StringBuffer();
		for (String s : hm.keySet()) {
			sb.append(ls).append(s).append(" = ").append(hm.get(s)).append("\n");
		}
		return sb.toString();
	}

}
