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
package org.projog.core.function.debug;

import org.projog.core.term.Term;

/* SYSTEM TEST
 % %LINK% prolog-debugging
 */
/**
 * <code>trace</code> - enables exhaustive tracing.
 * <p>
 * By enabling exhaustive tracing the programmer will be informed of every goal their program attempts to resolve.
 * </p>
 */
public final class Trace extends org.projog.core.function.AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term... args) {
      return evaluate();
   }

   /**
    * Overloaded version of {@link #evaluate(Term...)} that avoids the overhead of creating a new {@code Term} array.
    * 
    * @see org.projog.core.Predicate#evaluate(Term...)
    */
   public boolean evaluate() {
      getKnowledgeBase().getSpyPoints().setTraceEnabled(true);
      return true;
   }
}