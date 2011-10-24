package globals;

import gnu.mapping.*;
import util.KawaWrap;

public class WLists extends Globals {
    /**
     * Add methods related to lists.
     * 
     * @param kawa The interpreter to add them to.
     */
    public void addMethods(final KawaWrap kawa) throws Throwable {
	kawa.eval(
"(define (filter p ls)" +
"  (cond" + 
"    [(null? ls) '()]" + 
"    [else (cons (p (car ls)) (filter p (cdr ls)))]))");

	kawa.eval("(define (id x) x)");
	kawa.eval("(define (any p ls) (and (not (null? ls)) (or (p (car ls)) (any? (cdr ls)))))");
	kawa.eval("(define (all p ls) (or (null? ls) (and (p (car ls)) (all? (cdr ls)))))");

	kawa.eval(
"(define (fold-left p z . lol)" + 
"  (let loop ([acc z]" +
"             [lol lol])" +
"    (let ([nulls (map null? lol)])" +
"      (cond" +
"        [(all id nulls) acc]" +
"        [(any id nulls) (error 'fold-left \"all lists must be the same length\")]" +
"	 [else (loop" +
"                (apply p (cons acc (map car lol)))" +
"                (map cdr lol))]))))");

	kawa.eval(
"(define (fold-right p z . lol)" + 
" (let loop ([lol lol])" + 
"  (let ([nulls (map null? lol)])" + 
"   (cond" + 
"    [(all id nulls) z]" +
"    [(any id nulls) (error 'fold-right \"all lists must be the same length\")]" +
"    [else (apply p (append (map car lol) (list (loop (map cdr lol)))))]))))");
    }
}
