package org.projog.core.function.compound;

import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.Term;

/* SYSTEM TEST
 % %TRUE% once(repeat)
 % %TRUE% once(true)
 % %FALSE% once(fail)
 */
/**
 * <code>once(X)</code> - calls the goal represented by a term.
 * <p>
 * The <code>once(X)</code> goal succeeds if an attempt to satisfy the goal represented by the term <code>X</code>
 * succeeds. No attempt is made to retry the goal during backtracking - it is only evaluated once.
 * </p>
 */
public final class Once extends AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term... args) {
      return evaluate(args[0]);
   }

   /**
    * Overloaded version of {@link #evaluate(Term...)} that avoids the overhead of creating a new {@code Term} array.
    * 
    * @see org.projog.core.Predicate#evaluate(Term...)
    */
   public boolean evaluate(Term t) {
      Predicate e = KnowledgeBaseUtils.getPredicate(getKnowledgeBase(), t);
      return e.evaluate(t.getArgs());
   }
}