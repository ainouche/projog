/*
 * Copyright 2013 S. Webber
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
package org.projog.example;

import static org.projog.core.term.TermUtils.castToNumeric;

import org.projog.core.ArithmeticOperator;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.Term;

public class CalculatableExample implements ArithmeticOperator {
   @Override
   public Numeric calculate(Term[] args) {
      Numeric input = castToNumeric(args[0]);
      long rounded = Math.round(input.getDouble());
      return new IntegerNumber(rounded);
   }
}
