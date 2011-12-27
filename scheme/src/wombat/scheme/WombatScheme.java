package wombat.scheme;

import java.util.List;

/**
 * Wrapper for the parser and evaluator together.
 */
public class WombatScheme {
	Environment Env;
	boolean Running;
	
	/**
	 * Create a new Wombat Scheme engine.
	 */
	public WombatScheme() {
		Env = Environment.empty();
		Running = true;
	}
	
	/**
	 * Run code using this Scheme.
	 * @param code The code to run.
	 * @return A string representing the result.
	 */
	public String run(String code) {
		StringBuilder sb = new StringBuilder();
		
		List<SExpression> blocks = Parser.parse(code);
		for (SExpression block : blocks) {
			sb.append(Evaluator.evaluate(block, Env));
			sb.append("\n");
		}
		
		return sb.toString();	
	}
	
	/**
	 * Check if the Scheme engine is currently running.
	 * @return True or false.
	 */
	public boolean isRunning() {
		return Running;
	}
}