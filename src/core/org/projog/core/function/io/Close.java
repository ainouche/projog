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
package org.projog.core.function.io;

import org.projog.core.ProjogException;
import org.projog.core.term.Term;

/* SYSTEM TEST
 % %LINK% prolog-io
 */
/**
 * <code>close(X)</code> - closes a stream.
 * <p>
 * <code>close(X)</code> closes the stream represented by <code>X</code>. The stream is closed and can no longer be
 * used.
 * </p>
 */
public final class Close extends org.projog.core.function.AbstractSingletonPredicate {
   @Override
   public boolean evaluate(Term... args) {
      return evaluate(args[0]);
   }

   /**
    * Overloaded version of {@link #evaluate(Term...)} that avoids the overhead of creating a new {@code Term} array.
    * 
    * @see org.projog.core.Predicate#evaluate(Term...)
    */
   public boolean evaluate(Term argument) {
      try {
         getKnowledgeBase().getFileHandles().close(argument);
         return true;
      } catch (Exception e) {
         throw new ProjogException("Unable to close strean for: " + argument, e);
      }
   }
}