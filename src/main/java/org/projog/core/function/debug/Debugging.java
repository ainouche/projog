/*
 * Copyright 2013-2014 S. Webber
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

import java.io.PrintStream;
import java.util.Map;

import org.projog.core.PredicateKey;
import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;

/* TEST
 %LINK prolog-debugging
 */
/**
 * <code>debugging</code> - lists current spy points.
 * <p>
 * The list of spy points currently set is printed as a side effect of <code>debugging</code> being satisfied.
 * </p>
 */
public final class Debugging extends AbstractSingletonPredicate {
   @Override
   protected boolean evaluate() {
      PrintStream currentOutputStream = getFileHandles().getCurrentOutputStream();
      Map<PredicateKey, SpyPoints.SpyPoint> map = getSpyPoints().getSpyPoints();
      for (Map.Entry<PredicateKey, SpyPoints.SpyPoint> e : map.entrySet()) {
         if (e.getValue().isEnabled()) {
            currentOutputStream.println(e.getKey());
         }
      }
      return true;
   }
}
