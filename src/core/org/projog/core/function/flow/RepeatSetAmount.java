/*
 * Copyright 2013 S Webber
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.projog.core.function.flow;

import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.Numeric;
import org.projog.core.term.Term;
import org.projog.core.term.TermUtils;

/* SYSTEM TEST
 % %QUERY% repeat(3), write('hello, world'), nl
 % %OUTPUT% 
 % hello, world
 %
 % %OUTPUT%
 % %ANSWER/% 
 % %OUTPUT% 
 % hello, world
 %
 % %OUTPUT%
 % %ANSWER/% 
 % %OUTPUT% 
 % hello, world
 %
 % %OUTPUT%
 % %ANSWER/% 

 % %QUERY% repeat(1)
 % %ANSWER/%
 % %QUERY% repeat(2)
 % %ANSWER/%
 % %ANSWER/%
 % %QUERY% repeat(3)
 % %ANSWER/%
 % %ANSWER/%
 % %ANSWER/%
 % %FALSE% repeat(0)
 % %FALSE% repeat(-1)

 % %QUERY% repeat(X)
 % %EXCEPTION% Expected Numeric but got: NAMED_VARIABLE with value: X
*/
/**
 * <code>repeat(N)</code> - succeeds <code>N</code> times.
 */
public final class RepeatSetAmount extends AbstractRetryablePredicate {
   private final int limit;
   private int ctr;

   public RepeatSetAmount() {
      this(0);
   }

   /**
    * Sets number of times it will successfully evaluate.
    * 
    * @param limit the number of times to successfully evaluate
    */
   public RepeatSetAmount(int limit) {
      this.limit = limit;
   }

   @Override
   public boolean evaluate(Term... args) {
      return evaluate();
   }

   /**
    * Overloaded version of {@link #evaluate(Term...)} that avoids the overhead of creating a new {@code Term} array.
    * 
    * @see org.projog.core.Predicate#evaluate(Term...)
    */
   public boolean evaluate(Term arg) {
      return evaluate();
   }

   /**
    * Public no-arg overloaded version of {@code evaluate}.
    * <p>
    * <b>Note:</b> {@code public} as this overloaded version will be called directly for static user defined predicates
    * that have a number of clauses, all of which will always evaluate successfully exactly once. (e.g. {@code a. a. a.}
    * or {@code p(). p(). p().}
    * 
    * @return {@code true} if this instance has not yet been successfully evaluated for the number of times specified
    * when it was created, else {@code false}
    * @see org.projog.core.udp.StaticUserDefinedPredicateFactory
    */
   public boolean evaluate() {
      return ctr++ < limit;
   }

   @Override
   public RepeatSetAmount getPredicate(Term... args) {
      if (args.length == 1) {
         return getPredicate(args[0]);
      } else {
         return getPredicate();
      }
   }

   /**
    * Overloaded version of {@link #getPredicate(Term...)} that avoids the overhead of creating a new {@code Term}
    * array.
    * 
    * @see org.projog.core.PredicateFactory#getPredicate(Term...)
    */
   public RepeatSetAmount getPredicate(Term arg) {
      Numeric n = TermUtils.castToNumeric(arg);
      return new RepeatSetAmount(n.getInt());
   }

   /**
    * Public no-arg overloaded version of {@code getPredicate}.
    * <p>
    * <b>Note:</b> {@code public} as this overloaded version will be called directly for static user defined predicates
    * that have a number of clauses, all of which will always evaluate successfully exactly once. (e.g. {@code a. a. a.}
    * or {@code p(). p(). p().}
    * 
    * @return copy of this instance
    * @see org.projog.core.udp.StaticUserDefinedPredicateFactory
    */
   public RepeatSetAmount getPredicate() {
      return new RepeatSetAmount(limit);
   }

   @Override
   public boolean couldReEvaluationSucceed() {
      return ctr < limit;
   }
}