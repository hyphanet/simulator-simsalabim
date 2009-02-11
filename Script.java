package simsalabim;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
/**
 * Scripts that run the simulator. Look at script.txt for the format.
 */
public class Script {

	private static HashMap<String, ScriptCommandType> commandTypes = new HashMap<String, ScriptCommandType>();

	static {
		commandTypes.put("BuildTask",new ScriptCommandType("BuildTask"));
		commandTypes.put("JoinTask",new ScriptCommandType("JoinTask"));
		commandTypes.put("BlaTask",new ScriptCommandType("BlaTask"));
		commandTypes.put("NodeAggregate", new AggregateCommand.Type());
		commandTypes.put("MJoinTask", new ScriptCommandType("MJoinTask"));
		commandTypes.put("MBuildTask", new ScriptCommandType("MBuildTask"));
		commandTypes.put("InfoCommand", new ScriptCommandType("InfoCommand"));
		commandTypes.put("HJoinTask", new ScriptCommandType("HJoinTask"));
		commandTypes.put("HBuildTask", new ScriptCommandType("HBuildTask"));
		commandTypes.put("HDualStats", new ScriptCommandType("HDualStats"));
	}

	private Class simClass;
	private String simArgs;

	private LinkedList<ScriptCommand> cqueue = new LinkedList<ScriptCommand>();

	public Script(BufferedReader br) throws IOException, ScriptException {
		this(readcmds(br));
	}


	public Script(String[] cmds) throws ScriptException {
		if (cmds.length < 2)
			throw new ScriptException("Nothing in file");
		try {
			int n = cmds[0].indexOf(" ");
			String cl = n == -1 ? cmds[0] : cmds[0].substring(0, n);
			simClass = Class.forName(cl);

			if (!Simulator.class.isAssignableFrom(simClass))
				throw new ScriptException("Class " + cl + " not a simulator.");
			simArgs = n == -1 ? cmds[0]: cmds[0].substring(n + 1).trim();
		} catch (ClassNotFoundException e) {
			throw new ScriptException("No such simulator class: " + cmds[0]);
		}
		for (int i = 1 ; i < cmds.length ; i++) {
			StringTokenizer st = new StringTokenizer(cmds[i],"\t ");
			String cmd = st.nextToken();

			ScriptCommandType ct = commandTypes.get(cmd);
			if (ct == null)
				throw new ScriptException("No such command as: " + cmd);

			cqueue.addLast(ct.newCommand(st));
		}
	}



	public Simulator makeSim(RandomEngine re,  DataSpace ds, Settings st, EventLogger ev) throws ScriptException {
	    
	    // Simulator s = new Darknet(re,ds,st,ev,simArgs);
	    // return s;

		try {

		    Constructor c = simClass.getConstructor(new Class[] {RandomEngine.class, 
									 DataSpace.class, Settings.class,
									 EventLogger.class, String.class});
		    return (Simulator) c.newInstance(new Object[] {re, ds, st, ev, simArgs});

		} catch (InvocationTargetException e) {
			throw new ScriptException("Simulator construction failed: " + e.getTargetException());
		} catch (NoSuchMethodException e) {
			throw new ScriptException("Simulator class missing constructor.");
		} catch (InstantiationException e) {
			throw new ScriptException("Borked: " + e);
		} catch (IllegalAccessException e) {
			throw new SimulationException("WTF? : " + e);
		}


	}

	public ScriptCommand nextCommand() {
		if (cqueue.size() != 0)
			return cqueue.removeFirst();
		else
			return null;
	}


	private static String[] readcmds(BufferedReader br) throws IOException {
		String l;
		Vector<String> v = new Vector<String>();
		while ((l = br.readLine()) != null) {
			if (!l.trim().equals("") && !l.trim().startsWith("#"))
				v.add(l);
		}

		String[] cmds = new String[v.size()];
		v.copyInto(cmds);
		return cmds;
	}
}
