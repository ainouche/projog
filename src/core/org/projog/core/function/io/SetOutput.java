package org.projog.core.function.io;

import org.projog.core.term.Term;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>set_output(X)</code> - sets the current output.
 * <p>
 * <code>set_output(X)</code> sets the current output to the stream represented by <code>X</code>.
 * </p>
 * <p>
 * <code>X</code> will be a term returned as the third argument of <code>open</code>, or the atom
 * <code>user_input</code>, which specifies that output is to go to the computer display.
 * </p>
 */
public final class SetOutput extends org.projog.core.function.AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term arg) {
      getKnowledgeBase().getFileHandles().setOutput(arg);
      return true;
   }
}