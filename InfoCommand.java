package simsalabim;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.*;

public class InfoCommand implements ScriptCommand {

	String type;
	String filename;
	
	public InfoCommand(String[] st) {
		type = st[0];
		filename = st[1];
	}
	
	
	public void execute(Simulator s) throws ScriptException {
		try {
			PrintWriter pw = new PrintWriter("-".equals(filename) ? System.err : new FileOutputStream(filename));
			if (type.equals("nodes")) { 
				pw.println("#" + s.info());
				writeInfo(s.allNodes(), pw);
			} else if (type.equals("data")) {
				pw.println("#" + s.ds.info());
				writeInfo(s.ds.allData(), pw);
			} else {
				System.err.println("Can't output data about: " + type);
			}
			pw.flush();
		} catch (IOException e) {
			throw new ScriptException("Error writing info: " + e);
		}
	}


	public static void writeInfo(Iterator<? extends InfoObject> i, PrintWriter pw) {
		if (i.hasNext()) {
			InfoObject io = i.next();

			pw.println("#" + tabbed(io.fields()));
			pw.println(tabbed(io.info()));

			while (i.hasNext()) {
				io = i.next();
				pw.println(tabbed(io.info()));
			}
		}
	}

	private static String tabbed(String[] s) {
		if (s.length < 1)
			return "";
		StringBuffer sb = new StringBuffer(s[0]);
		for (int i = 1 ; i < s.length ; i++) {
			sb.append('\t').append(s[i]);
		}
		return sb.toString();
	}
	
}
