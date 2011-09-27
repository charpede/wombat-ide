package globals;

import util.KawaWrap;

public class WDefine extends Globals {
	/**
	 * Add methods related to (random n)
	 * 
	 * @param kawa The interpreter to add them to.
	 */
	@Override
	public void addMethods(KawaWrap kawa) throws Throwable {
		kawa.eval("(define (void) (values))");
    	kawa.eval("(set! $define$ define)");
    	kawa.eval(
			"(define-syntax define" + 
			"  (syntax-rules (lambda)" + 
			"    [(define name" + 
			"       (lambda (args ...)" + 
			"         bodies ...))" + 
			"     ($define$ (name args ...)" + 
			"       bodies ...)]" + 
			"    [(define name" + 
			"       (lambda (args ... . dotted)" + 
			"         bodies ...))" + 
			"     ($define$ (name args ... . dotted)" + 
			"       bodies ...)]" + 
			"    [(define name" + 
			"       (lambda arg" + 
			"         bodies ...))" + 
			"     ($define$ (name . arg)" + 
			"       bodies ...)]" + 
			"    [(define stuff ...)" + 
			"     ($define$ stuff ...)]))");
    	
    	kawa.eval("(define $indent-level$ 0)");

    	kawa.eval("(define (newline) (display \"\\n\"))");
    	
    	kawa.eval(
			"(define ($indent$ args)" +
    		"  (let loop ([i (min 10 $indent-level$)])" +
    	    "    (when (positive? i)" +
    	    "      (display \"  \")" +
    	    "      (loop (sub1 i))))" +
    	    "  (when (> $indent-level$ 10) (display \"|\") (display $indent-level$) (display \"| \"))" +
    	    "  (cond" +
    	    "    [(null? args) (void)]" +
    	    "    [(null? (cdr args)) (display (car args))]" +
    	    "    [else" +
    	    "     (display (car args))" +
    	    "     (map (lambda (each) (display \" \") (display each)) (cdr args))])" +
    	    "  (newline))");

    	kawa.eval("(define ($indent++$ . args) ($indent$ args) (set! $indent-level$ (add1 $indent-level$)))");

		kawa.eval("(define ($indent--$ . args) (set! $indent-level$ (sub1 $indent-level$)) ($indent$ args))");

		kawa.eval(
			"(define-syntax trace-define" +
    	    "  (syntax-rules ()" +
    	    "   [(_ name body)" +
    	    "    (define name" +
    	    "      (let ([n 0]" +
    	    "            [f body])" +
    	    "        (lambda args" +
    	    "          ($indent++$ (cons 'name args))" +
    	    "            (let ([result (apply f args)])" +
    	    "              ($indent--$ \"=> \" result)" +
			"              result))))]))");
	}
}
