package util;

import java.util.HashMap;
import java.util.Map;

import util.errors.ErrorManager;
import globals.*;

import gnu.kawa.lispexpr.ReadTable;
import gnu.mapping.*;
import kawa.lang.NamedException;
import kawa.standard.Scheme;

/**
 * Wrapper around Kawa.
 */
public class KawaWrap {
	Scheme kawa;

	Map<String, String> ErrorTypeRenameMap;

	/**
	 * Connect to Kawa.
	 */
	public KawaWrap() {
		ErrorTypeRenameMap = new HashMap<String, String>();

		ErrorTypeRenameMap.put("(())", "(empty-list)");

		ErrorTypeRenameMap.put("gnu.lists.LList", "list");
		ErrorTypeRenameMap.put("gnu.lists.Pair", "list");
		ErrorTypeRenameMap.put("gnu.lists.ImmutablePair", "list");
		ErrorTypeRenameMap.put("gnu.lists.PairWithPosition", "list");

		ErrorTypeRenameMap.put("gnu.lists.SimpleVector", "vector");
		ErrorTypeRenameMap.put("gnu.lists.FVector", "vector");

		ErrorTypeRenameMap.put("gnu.lists.FString", "string");
		ErrorTypeRenameMap.put("java.lang.String", "string");

		ErrorTypeRenameMap.put("gnu.math.IntNum", "integer");
		ErrorTypeRenameMap.put("java.lang.Integer", "integer");
		ErrorTypeRenameMap.put("java.lang.Long", "integer");

		ErrorTypeRenameMap.put("gnu.math.IntFraction", "rational-number");
		ErrorTypeRenameMap.put("gnu.math.CComplex", "complex-number");
		ErrorTypeRenameMap.put("gnu.math.DComplex", "complex-number");
		ErrorTypeRenameMap.put("gnu.math.DFloNum", "real-number");

		ErrorTypeRenameMap.put("gnu.expr.ModuleMethod", "procedure");

		ErrorTypeRenameMap.put("java.lang.Boolean", "boolean");

		ErrorTypeRenameMap.put("globals.ImageShell", "image");
		ErrorTypeRenameMap.put("java.awt.Color", "color");
		ErrorTypeRenameMap.put("util.Tree", "tree");

		// ErrorTypeRenameMap.put("", "");

		reset();
	}

	/**
	 * Reset the stored Scheme interpreter and environment.
	 */
	public void reset() {
		ReadTable.defaultBracketMode = 0; // allow square brackets

		Scheme.registerEnvironment();
		kawa = new Scheme();

		// Load globals.
		for (Globals g : new Globals[] { 
			new WDefine(), new WRandom(),
			new WMath(), new WTree(), new WImage(), 
			new WLists()
		                }) {
			try {
				g.addMethods(this);
			} catch (Throwable ex) {
				ErrorManager.logError("Unable to load globals from "
						+ g.getClass().getName() + ": " + ex.getMessage());
			}
		}
	}

	/**
	 * Bind a new function from the Java end of things.
	 * 
	 * @param proc
	 *            The function to bind.
	 */
	public void bind(Named proc) {
		try {
			kawa.defineFunction(proc);
		} catch (Exception e) {
			// Shouldn't suppress this, but for now it works.
		}
	}

	/**
	 * Get something out of the environment.
	 * 
	 * @param name
	 *            The name to fetch.
	 * @return The object.
	 */
	public Object get(String name) {
		return kawa.getEnvironment().get(name, null);
	}

	/**
	 * Evaluate a command, given as a string.
	 * 
	 * @param s
	 *            The string to evaluate.
	 * @return The result.
	 */
	public String eval(String cmd) {
		Throwable o_ex = null;
		String err = null;

		try {
			Object result = Scheme.eval(cmd, kawa.getEnvironment());

			// Return the final result.
			if (result == null)
				return null;
			else
				return formatObject(result);

		} catch (StackOverflowError ex) {
			o_ex = ex;
			err = "Possible infinite loop detected.";

		} catch (UnboundLocationException ex) {
			o_ex = ex;
			err = "Error: " + ex.getMessage().replace("location", "variable");

		} catch (WrongArguments ex) {
			o_ex = ex;
			err = "Error: " + ex.getMessage();

		} catch (IllegalArgumentException ex) {
			o_ex = ex;
			err = ex.getMessage();

		} catch (NamedException ex) {
			o_ex = ex;
			err = ex.toString();

		} catch (WrongType ex) {
			o_ex = ex;
			if (("car".equals(ex.procname) || "cdr".equals(ex.procname)) && 
					"()".equals(ex.argValue.toString()))
				err = "Error in " + ex.procname + ": cannot take the " + ex.procname + " of an empty list.";
			else
				err = "Error: " + ex.toString();
		
		} catch (ArrayIndexOutOfBoundsException ex) {
			o_ex = ex;
			err = "Error: Array index out of bounds (" + ex.getMessage() + ")";
		
		} catch (RuntimeException ex) {
			o_ex = ex;
			err = "Error: " + ex.getMessage();

		} catch (Throwable ex) {
			ex.printStackTrace();
			ErrorManager.logError("Unknown error handled ("
					+ ex.getClass().getName() + "): " + ex.toString());
			err = "Error: " + ex.toString();
		}

		if (o_ex != null)
			ErrorManager.logError("Error handled (" + o_ex.getClass().getName() + "): " + o_ex.toString());
		
		err = err.replace(';', ',');
		err = err.replace("<string>", "<repl>");

		for (String key : ErrorTypeRenameMap.keySet())
			err = err.replace(key, ErrorTypeRenameMap.get(key));

		return err;
	}

	/**
	 * Format an object using Scheme rules.
	 * 
	 * @param v
	 *            The object to format.
	 * @return
	 */
	public static String formatObject(Object v) {
		if (v == null)
			return "";

		else if (v instanceof String)
			return '"' + ((String) v) + '"';

		else if (v instanceof gnu.lists.FString)
			return '"' + v.toString() + '"';

		else if (v instanceof Boolean)
			return ((((Boolean) v).booleanValue()) ? "#t" : "#f");

		else if (v instanceof gnu.mapping.Values) {
			gnu.mapping.Values val = (gnu.mapping.Values) v;

			StringBuilder sb = new StringBuilder();
			for (Object obj : val.objects) {
				String temp = formatObject(obj);
				if (temp != null && !temp.isEmpty()) {
					sb.append(temp);
					sb.append(", ");
				}
			}
			if (sb.length() >= 2)
				sb.delete(sb.length() - 2, sb.length());
			return sb.toString();
		}

		else if (v instanceof gnu.text.Char)
			return "#\\" + (((gnu.text.Char) v).charValue());

		else if (v instanceof gnu.lists.Pair) {
			gnu.lists.Pair p = (gnu.lists.Pair) v;

			if (p.getCdr() instanceof gnu.lists.LList) {
				if (((gnu.lists.LList) p.getCdr()).isEmpty())
					return "(" + formatObject(p.getCar()) + ")";
				else
					return "(" + formatObject(p.getCar()) + " "
							+ formatObject(p.getCdr()).substring(1);
			} else
				return "(" + formatObject(p.getCar()) + " . "
						+ formatObject(p.getCdr()) + ")";
		}

		else if (v instanceof kawa.lang.Quote)
			return "#<macro quote>";
		else if (v instanceof kawa.lang.Lambda)
			return "#<macro lambda>";

		else if (v instanceof gnu.lists.FVector) {
			gnu.lists.FVector vec = (gnu.lists.FVector) v;

			StringBuilder sb = new StringBuilder();
			sb.append("#(");
			for (Object o : vec) {
				sb.append(formatObject(o));
				sb.append(" ");
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(")");

			return sb.toString();
		}

		else if (v instanceof gnu.mapping.Procedure) {
			gnu.mapping.Procedure proc = (gnu.mapping.Procedure) v;

			String name = proc.getName();
			if (name == null)
				return "#<procedure>";
			else
				return "#<procedure " + name + ">";
		}

		else if (v instanceof java.awt.Color) {
			java.awt.Color c = (java.awt.Color) v;

			return "[color " + c.getRed() + " " + c.getGreen() + " "
					+ c.getBlue() + "]";
		}

		else if (v instanceof gnu.expr.ModuleMethod) {
			gnu.expr.ModuleMethod m = (gnu.expr.ModuleMethod) v;

			if (m.getName() != null)
				return "#<procedure " + m.getName() + ">";
			else if (m.getSymbol() != null)
				return "#<procedure " + m.getSymbol() + ">";
			else
				return "#<procedure>";
		}

		else if (v instanceof gnu.lists.EofClass) {
			return null;
		}

		else if (v instanceof gnu.kawa.lispexpr.LangObjType) {
			return "#<procedure "
					+ ((gnu.kawa.lispexpr.LangObjType) v).getName()
							.toLowerCase() + ">";
		}

		else {
			return v.toString();
		}

	}
}
