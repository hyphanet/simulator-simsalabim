package simsalabim;

import java.lang.reflect.*;
import java.util.StringTokenizer;

public class ScriptCommandType {

	private String name;

	public ScriptCommandType(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public ScriptCommand newCommand(StringTokenizer param)
			throws ScriptException {
		try {
			// Reflection is always ugly. Remind me how many times I have
			// written this code...

			Class c = Class.forName("simsalabim." + name);
			int n = param.countTokens();
			String[] params = new String[n];
			for (int i = 0; i < n; i++)
				params[i] = param.nextToken();

			Object o = c.getConstructor(new Class[] { String[].class })
					.newInstance(new Object[] { params });
			return (ScriptCommand) o;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof ScriptException)
				throw (ScriptException) t;
			else if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			else
				throw new ScriptException(e.toString());
		} catch (Exception e) {
			// Blanket catches are BAD. Bad Oskar, bad!
			e.printStackTrace();
			throw new ScriptException("Failed to construct script command: "
					+ e);
		}
	}

}
