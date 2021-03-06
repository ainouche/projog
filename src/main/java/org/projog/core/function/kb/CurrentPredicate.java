/*
 * Copyright 2018 S. Webber
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
package org.projog.core.function.kb;

import java.util.Iterator;
import java.util.Set;

import org.projog.core.Predicate;
import org.projog.core.PredicateKey;
import org.projog.core.function.AbstractPredicateFactory;
import org.projog.core.term.Term;

/* TEST
%TRUE_NO current_predicate(!/0)

%QUERY current_predicate(!/X)
%ANSWER X = 0
%NO

%FALSE current_predicate(!/1)

%FALSE current_predicate(doesnt_exist/1)

%QUERY current_predicate(call/X)
%ANSWER X = 1
%ANSWER X = 2
%ANSWER X = 3
%ANSWER X = 4
%ANSWER X = 5
%ANSWER X = 6
%ANSWER X = 7
%ANSWER X = 8
%ANSWER X = 9
%ANSWER X = 10
%NO
*/
/**
 * <code>current_predicate(X)</code> - unifies with defined predicates.
 * <p>
 * <code>current_predicate(X)</code> attempts to unify <code>X</code> against all currently defined predicates.
 */
public final class CurrentPredicate extends AbstractPredicateFactory {
   @Override
   protected Predicate getPredicate(Term arg) {
      Set<PredicateKey> keys = getPredicates().getAllDefinedPredicateKeys();
      return new Retryable(arg, keys);
   }

   private static class Retryable implements Predicate {
      private final Term arg;
      private final Iterator<PredicateKey> iterator;

      private Retryable(Term arg, Set<PredicateKey> keys) {
         this.arg = arg;
         this.iterator = keys.iterator();
      }

      @Override
      public boolean evaluate() {
         while (iterator.hasNext()) {
            Term next = iterator.next().toTerm();

            arg.backtrack();
            if (arg.unify(next)) {
               return true;
            }
         }
         return false;
      }

      @Override
      public boolean couldReevaluationSucceed() {
         return iterator.hasNext();
      }
   }
}
