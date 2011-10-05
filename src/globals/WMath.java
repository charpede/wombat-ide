package globals;

import gnu.mapping.Procedure1;
import gnu.math.IntNum;
import util.KawaWrap;

public class WMath extends Globals {
	/**
	 * Add methods related to (random n)
	 * 
	 * @param kawa The interpreter to add them to.
	 * @throws Throwable If anything breaks while adding them.
	 */
	@Override
	public void addMethods(final KawaWrap kawa) throws Throwable {
    	kawa.eval("(define (sub1 n) (- n 1))");
    	kawa.eval("(define (add1 n) (+ n 1))");
    	
    	kawa.bind(new Procedure1("even?") {
			@Override
			public Object apply1(Object arg1) throws Throwable {
				if (arg1 instanceof IntNum)
					return !((IntNum) arg1).isOdd();
				else if (arg1 instanceof Integer)
					return ((Integer) arg1).intValue() % 2 == 0;
				else if (arg1 instanceof Long)
					return ((Long) arg1).longValue() % 2 == 0;
				else
					throw new IllegalArgumentException("Error in even?, expected integer as first argument, got '" + KawaWrap.formatObject(arg1) + "'.");
			}
    	});
    	
    	kawa.bind(new Procedure1("odd?") {
			@Override
			public Object apply1(Object arg1) throws Throwable {
				if (arg1 instanceof IntNum)
					return ((IntNum) arg1).isOdd();
				else if (arg1 instanceof Integer)
					return ((Integer) arg1).intValue() % 2 != 0;
				else if (arg1 instanceof Long)
					return ((Long) arg1).longValue() % 2 != 0;
				else
					throw new IllegalArgumentException("Error in odd?, expected integer as first argument, got '" + KawaWrap.formatObject(arg1) + "'.");
			}
    	});
	}
}
