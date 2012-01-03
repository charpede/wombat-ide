package wombat.scheme.values;

/**
 * Identifiers.
 */
public class SchemeSymbol extends SchemeObject<String> {
	private static final long serialVersionUID = 2473326947081851400L;
	
	/**
	 * Create a new Symbol.
	 * @param value The string version of the name.
	 */
	public SchemeSymbol(String value) {
		super(value);
	}
	
	/**
	 * Check if I'm a dot.
	 * @return Am I a dot? DOTDOTDOT
	 */
	public boolean isDot() {
		return ".".equals(Value);
	}
}
