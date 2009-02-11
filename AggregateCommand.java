package simsalabim;

import java.util.StringTokenizer;

/**
 * AggregateCommand <aggname> <aggtype> ...
 * 
 * <aggtype> one of "collector"
 */
public class AggregateCommand implements ScriptCommand {

	public static class Type extends ScriptCommandType {
		public Type() {
			super("AggregateCommand");
		}

		public ScriptCommand newCommand(StringTokenizer params)
				throws ScriptException {
			return new AggregateCommand(params);
		}
	}

	public DataAggregator da;

	public AggregateCommand(StringTokenizer params) throws ScriptException {
		// first param is name
		String name = params.nextToken();
		// next is type
		String type = params.nextToken();

		if (type.equals("collector")) {
			if (params.hasMoreTokens()) {
				da = new CDataAggregator(name, params.nextToken());
			} else {
				da = new CDataAggregator(name);
			}
		}
		if (type.equals("histogram")) {
			double start = Double.parseDouble(params.nextToken());
			double step = Double.parseDouble(params.nextToken());
			double end = Double.parseDouble(params.nextToken());
			if (params.hasMoreTokens()) {
				da = new HistDataAggregator(name, start, step, end, params
						.nextToken());
			} else {
				da = new HistDataAggregator(name, start, step, end);
			}
		} else
			throw new ScriptException("No agg type as: " + type);
	}

	public void execute(Simulator god) {
		god.aggregateNodeData(da);

		da.complete();
	}

}
