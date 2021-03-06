/*
 * Copyright 2020 S. Webber
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
package org.projog.core.udp.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.projog.TestUtils.array;
import static org.projog.TestUtils.assertClass;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.createClauseModel;
import static org.projog.core.term.TermUtils.EMPTY_ARRAY;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.projog.core.KnowledgeBase;
import org.projog.core.KnowledgeBaseUtils;
import org.projog.core.Predicate;
import org.projog.core.PredicateFactory;
import org.projog.core.PredicateKey;
import org.projog.core.ProjogException;
import org.projog.core.term.Term;
import org.projog.core.term.TermType;
import org.projog.core.term.Variable;
import org.projog.core.udp.ClauseModel;
import org.projog.core.udp.PredicateUtils;
import org.projog.core.udp.interpreter.ClauseActionFactory.AlwaysMatchedFact;
import org.projog.core.udp.interpreter.ClauseActionFactory.ImmutableConsequentRule;
import org.projog.core.udp.interpreter.ClauseActionFactory.ImmutableFact;
import org.projog.core.udp.interpreter.ClauseActionFactory.MutableFact;
import org.projog.core.udp.interpreter.ClauseActionFactory.MutableRule;
import org.projog.core.udp.interpreter.ClauseActionFactory.VariableAntecedantClauseAction;
import org.projog.core.udp.interpreter.ClauseActionFactory.ZeroArgConsequentRule;

public class ClauseActionFactoryTest {
   private KnowledgeBase kb;
   private PredicateFactory mockPredicateFactory;
   private Predicate mockPredicate1;
   private Predicate mockPredicate2;

   @Before
   public void before() {
      mockPredicate1 = mock(Predicate.class);
      mockPredicate2 = mock(Predicate.class);

      mockPredicateFactory = mock(PredicateFactory.class);
      when(mockPredicateFactory.getPredicate(EMPTY_ARRAY)).thenReturn(mockPredicate1, mockPredicate2);

      kb = KnowledgeBaseUtils.createKnowledgeBase();
      kb.getPredicates().addPredicateFactory(new PredicateKey("test", 0), mockPredicateFactory);
   }

   @After
   public void after() {
      verifyNoInteractions(mockPredicate1, mockPredicate2);
      verifyNoMoreInteractions(mockPredicateFactory);
   }

   @Test
   public void testAlwaysMatchedFact_isRetryable() {
      AlwaysMatchedFact a = create(AlwaysMatchedFact.class, "p.");
      assertFalse(a.isRetryable());
   }

   @Test
   public void testAlwaysMatchedFact_getPredicate_no_arguments() {
      AlwaysMatchedFact a = create(AlwaysMatchedFact.class, "p.");
      assertSame(PredicateUtils.TRUE, a.getPredicate(EMPTY_ARRAY));
   }

   @Test
   public void testAlwaysMatchedFact_getPredicate_distinct_variable_arguments() {
      AlwaysMatchedFact a = create(AlwaysMatchedFact.class, "p(X,Y,Z).");
      assertSame(PredicateUtils.TRUE, a.getPredicate(EMPTY_ARRAY));
   }

   @Test
   public void testImmutableFact_isRetryable() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,c).");
      assertFalse(a.isRetryable());
   }

   @Test
   public void testImmutableFact_getPredicate_query_args_match_clause() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,c).");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), atom("b"), atom("c"))));
   }

   @Test
   public void testImmutableFact_getPredicate_query_args_dont_match_clause() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,c).");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("a"), atom("b"), atom("z"))));
   }

   @Test
   public void testImmutableFact_getPredicate_query_args_all_distinct_variables() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,c).");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      Variable z = new Variable("Z");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(x, y, z)));
      assertEquals(atom("a"), x.getTerm());
      assertEquals(atom("b"), y.getTerm());
      assertEquals(atom("c"), z.getTerm());
   }

   @Test
   public void testImmutableFact_getPredicate_query_args_mixture_of_atom_and_distinct_variables() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,c).");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), x, y)));
      assertEquals(atom("b"), x.getTerm());
      assertEquals(atom("c"), y.getTerm());
   }

   @Test
   public void testImmutableFact_getPredicate_shared_variables_dont_match() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,c).");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(x, y, x)));
   }

   @Test
   public void testImmutableFact_getPredicate_shared_variables_match() {
      ImmutableFact a = create(ImmutableFact.class, "p(a,b,a).");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(x, y, x)));
      assertEquals(atom("a"), x.getTerm());
      assertEquals(atom("b"), y.getTerm());
   }

   @Test
   public void testMutableFact_isRetryable() {
      MutableFact a = create(MutableFact.class, "p(a,X,c).");
      assertFalse(a.isRetryable());
   }

   @Test
   public void testMutableFact_getPredicate_query_args_unify_with_clause() {
      MutableFact a = create(MutableFact.class, "p(a,X,c).");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), atom("b"), atom("c"))));
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), atom("d"), atom("c"))));
   }

   @Test
   public void testMutableFact_getPredicate_query_args_dont_unify_with_clause() {
      MutableFact a = create(MutableFact.class, "p(a,X,c).");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("a"), atom("b"), atom("d"))));
   }

   @Test
   public void testMutableFact_getPredicate_query_args_shared_variable_doesnt_unify_with_clause() {
      MutableFact a = create(MutableFact.class, "p(a,X,c).");
      Variable x = new Variable("X");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(x, atom("b"), x)));
   }

   @Test
   public void testMutableFact_getPredicate_query_args_shared_variable_unify_with_clause() {
      MutableFact a = create(MutableFact.class, "p(a,X,a).");
      Variable x = new Variable("X");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(x, atom("b"), x)));
      assertEquals(atom("a"), x.getTerm());
   }

   @Test
   public void testMutableFact_getPredicate_query_args_dont_unify_with_clause_shared_variable() {
      MutableFact a = create(MutableFact.class, "p(X,b,X).");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("a"), atom("b"), atom("c"))));
   }

   @Test
   public void testMutableFact_getPredicate_query_args_unify_with_clause_shared_variable() {
      MutableFact a = create(MutableFact.class, "p(X,b,X).");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), atom("b"), atom("a"))));
   }

   @Test
   public void testMutableFact_getPredicate_query_args_variable_unifies_with_clause_variable() {
      MutableFact a = create(MutableFact.class, "p(a,X,c).");
      Variable x = new Variable("X");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), x, atom("c"))));
      assertSame(TermType.VARIABLE, x.getTerm().getType());
      // assert query variable has been unified with clause variable
      assertNotSame(x, x.getTerm());
   }

   @Test
   public void testMutableFact_getPredicate_query_args_variable_unifies_with_clause_atom() {
      MutableFact a = create(MutableFact.class, "p(a,X,c).");
      Variable x = new Variable("X");
      assertSame(PredicateUtils.TRUE, a.getPredicate(array(atom("a"), atom("b"), x)));
      assertEquals(atom("c"), x.getTerm());
   }

   @Test
   public void testVariableAntecedant_isRetryable() {
      VariableAntecedantClauseAction a = create(VariableAntecedantClauseAction.class, "p(X) :- X.");
      assertTrue(a.isRetryable());
   }

   @Test()
   public void testVariableAntecedant_getPredicate_unassigned_variable() {
      VariableAntecedantClauseAction a = create(VariableAntecedantClauseAction.class, "p(X) :- X.");
      try {
         a.getPredicate(array(new Variable("Z")));
         fail();
      } catch (ProjogException e) {
         assertEquals("Expected an atom or a predicate but got a VARIABLE with value: X", e.getMessage());
      }
   }

   @Test
   public void testVariableAntecedant_getPredicate_unknown_predicate() {
      VariableAntecedantClauseAction a = create(VariableAntecedantClauseAction.class, "p(X) :- X.");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("an_unknown_predicate"))));
   }

   @Test
   public void testVariableAntecedant_getPredicate_query_args_dont_unify_with_clause() {
      VariableAntecedantClauseAction a = create(VariableAntecedantClauseAction.class, "p(X,a) :- X.");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("test"), atom("b"))));
   }

   @Test
   public void testVariableAntecedant_getPredicate_known_predicate() {
      Term[] queryArgs = array(atom("test"));

      VariableAntecedantClauseAction a = create(VariableAntecedantClauseAction.class, "p(X) :- X.");
      assertSame(mockPredicate1, a.getPredicate(queryArgs));
      assertSame(mockPredicate2, a.getPredicate(queryArgs));

      verify(mockPredicateFactory, times(2)).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testVariableAntecedant_getPredicate_with_different_query_args() {
      PredicateFactory pf1 = mock(PredicateFactory.class);
      Predicate p1 = mock(Predicate.class);
      when(pf1.getPredicate(EMPTY_ARRAY)).thenReturn(p1);
      kb.getPredicates().addPredicateFactory(new PredicateKey("test1", 0), pf1);

      PredicateFactory pf2 = mock(PredicateFactory.class);
      Predicate p2 = mock(Predicate.class);
      when(pf2.getPredicate(EMPTY_ARRAY)).thenReturn(p2);
      kb.getPredicates().addPredicateFactory(new PredicateKey("test2", 0), pf2);

      VariableAntecedantClauseAction a = create(VariableAntecedantClauseAction.class, "p(X) :- X.");
      assertSame(p1, a.getPredicate(array(atom("test1"))));
      assertSame(p2, a.getPredicate(array(atom("test2"))));

      verify(pf1, times(1)).getPredicate(EMPTY_ARRAY);
      verify(pf2, times(1)).getPredicate(EMPTY_ARRAY);
      verifyNoMoreInteractions(pf1, pf2, p1, p2);
   }

   @Test
   public void testZeroArgConsequentRule_isRetryable_unknown_predicate() {
      ZeroArgConsequentRule a = create(ZeroArgConsequentRule.class, "p :- an_unknown_predicate.");
      assertTrue(a.isRetryable());
   }

   @Test
   public void testZeroArgConsequentRule_isRetryable_true() {
      when(mockPredicateFactory.isRetryable()).thenReturn(true);

      ZeroArgConsequentRule a = create(ZeroArgConsequentRule.class, "p :- test.");
      assertTrue(a.isRetryable());

      verify(mockPredicateFactory).isRetryable();
   }

   @Test
   public void testZeroArgConsequentRule_isRetryable_false() {
      when(mockPredicateFactory.isRetryable()).thenReturn(false);

      ZeroArgConsequentRule a = create(ZeroArgConsequentRule.class, "p :- test.");
      assertFalse(a.isRetryable());

      verify(mockPredicateFactory).isRetryable();
   }

   @Test
   public void testZeroArgConsequentRule_getPredicate() {
      ZeroArgConsequentRule a = create(ZeroArgConsequentRule.class, "p :- test.");
      assertSame(mockPredicate1, a.getPredicate(EMPTY_ARRAY));
      assertSame(mockPredicate2, a.getPredicate(EMPTY_ARRAY));

      verify(mockPredicateFactory, times(2)).getPredicate(EMPTY_ARRAY);
   }

   // TODO p :- test(X). p(X) :- test(X). p(a) :- test(X).

   @Test
   public void testZeroArgConsequentRule_getPredicate_antecedent_mutable() {
      PredicateFactory pf = mock(PredicateFactory.class);
      kb.getPredicates().addPredicateFactory(new PredicateKey("test", 5), pf);

      ArgumentCaptor<Term[]> captor = ArgumentCaptor.forClass(Term[].class);
      Predicate p1 = mock(Predicate.class);
      Predicate p2 = mock(Predicate.class);
      when(pf.getPredicate(captor.capture())).thenReturn(p1, p2);

      ZeroArgConsequentRule a = create(ZeroArgConsequentRule.class, "p :- test(X,y,X,p(X),Z).");
      assertSame(p1, a.getPredicate(EMPTY_ARRAY));
      assertSame(p2, a.getPredicate(EMPTY_ARRAY));

      List<Term[]> allValues = captor.getAllValues();
      assertEquals(2, allValues.size());

      Term[] values1 = allValues.get(0);
      assertEquals(atom("y"), values1[1]);
      assertSame(values1[0], values1[2]);
      assertSame(values1[0], values1[3].getArgument(0));
      assertNotSame(values1[0], values1[4]);

      Term[] values2 = allValues.get(1);
      assertNotSame(values1[0], values2[0]);
      assertSame(values1[1], values2[1]);
      assertNotSame(values1[2], values2[2]);
      assertNotSame(values1[3], values2[3]);
      assertNotSame(values1[4], values2[4]);

      verify(pf, times(2)).getPredicate(any(Term[].class));
      verifyNoMoreInteractions(pf, p1, p2);
   }

   @Test
   public void testImmutableConsequentRule_isRetryable_unknown_predicate() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- an_unknown_predicate.");
      assertTrue(a.isRetryable());
   }

   @Test
   public void testImmutableConsequentRule_isRetryable_true() {
      when(mockPredicateFactory.isRetryable()).thenReturn(true);

      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");
      assertTrue(a.isRetryable());

      verify(mockPredicateFactory).isRetryable();
   }

   @Test
   public void testImmutableConsequentRule_isRetryable_false() {
      when(mockPredicateFactory.isRetryable()).thenReturn(false);

      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");
      assertFalse(a.isRetryable());

      verify(mockPredicateFactory).isRetryable();
   }

   @Test
   public void testImmutableConsequentRule_getPredicate_query_args_match_clause() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");
      Term[] queryArgs = array(atom("a"), atom("b"), atom("c"));
      assertSame(mockPredicate1, a.getPredicate(queryArgs));
      assertSame(mockPredicate2, a.getPredicate(queryArgs));

      verify(mockPredicateFactory, times(2)).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testImmutableConsequentRule_getPredicate_query_args_dont_match_clause() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("a"), atom("b"), atom("z"))));
   }

   @Test
   public void testImmutableConsequentRule_getPredicate_query_args_all_distinct_variables() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      Variable z = new Variable("Z");
      assertSame(mockPredicate1, a.getPredicate(array(x, y, z)));
      assertEquals(atom("a"), x.getTerm());
      assertEquals(atom("b"), y.getTerm());
      assertEquals(atom("c"), z.getTerm());

      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testImmutableConsequentRule_getPredicate_query_args_mixture_of_atom_and_distinct_variables() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertSame(mockPredicate1, a.getPredicate(array(atom("a"), x, y)));
      assertEquals(atom("b"), x.getTerm());
      assertEquals(atom("c"), y.getTerm());

      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testImmutableConsequentRule_getPredicate_shared_variables_dont_match() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,c) :- test.");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(x, y, x)));
   }

   @Test
   public void testImmutableConsequentRule_getPredicate_shared_variables_match() {
      ImmutableConsequentRule a = create(ImmutableConsequentRule.class, "p(a,b,a) :- test.");

      Variable x = new Variable("X");
      Variable y = new Variable("Y");
      assertSame(mockPredicate1, a.getPredicate(array(x, y, x)));
      assertEquals(atom("a"), x.getTerm());
      assertEquals(atom("b"), y.getTerm());

      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testMutableRule_isRetryable_unknown_predicate() {
      MutableRule a = create(MutableRule.class, "p(a,X,c) :- an_unknown_predicate.");
      assertTrue(a.isRetryable());
   }

   @Test
   public void testMutableRule_isRetryable_true() {
      when(mockPredicateFactory.isRetryable()).thenReturn(true);

      MutableRule a = create(MutableRule.class, "p(a,X,c) :- test.");
      assertTrue(a.isRetryable());

      verify(mockPredicateFactory).isRetryable();
   }

   @Test
   public void testMutableRule_isRetryable_false() {
      when(mockPredicateFactory.isRetryable()).thenReturn(false);

      MutableRule a = create(MutableRule.class, "p(a,X,c) :- test.");
      assertFalse(a.isRetryable());

      verify(mockPredicateFactory).isRetryable();
   }

   @Test
   public void testMutableRule_getPredicate_distinct_variable_arguments() {
      MutableRule a = create(MutableRule.class, "p(X,Y,Z) :- test.");

      Variable v1 = new Variable("A");
      Variable v2 = new Variable("B");
      Variable v3 = new Variable("C");

      assertSame(mockPredicate1, a.getPredicate(array(v1, v2, v3)));

      // assert query variables have been unified with clause variables
      assertSame(TermType.VARIABLE, v1.getTerm().getType());
      assertNotSame(v1, v1.getTerm());
      assertEquals("X", ((Variable) v1.getTerm()).getId());

      assertSame(TermType.VARIABLE, v2.getTerm().getType());
      assertNotSame(v2, v2.getTerm());
      assertEquals("Y", ((Variable) v2.getTerm()).getId());

      assertSame(TermType.VARIABLE, v3.getTerm().getType());
      assertNotSame(v3, v3.getTerm());
      assertEquals("Z", ((Variable) v3.getTerm()).getId());

      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testMutableRule_getPredicate_query_args_unify_with_clause() {
      MutableRule a = create(MutableRule.class, "p(a,X,c)  :- test.");
      assertSame(mockPredicate1, a.getPredicate(array(atom("a"), atom("b"), atom("c"))));
      assertSame(mockPredicate2, a.getPredicate(array(atom("a"), atom("d"), atom("c"))));
      verify(mockPredicateFactory, times(2)).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testMutableRule_getPredicate_query_args_dont_unify_with_clause() {
      MutableRule a = create(MutableRule.class, "p(a,X,c)  :- test.");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("a"), atom("b"), atom("d"))));
   }

   @Test
   public void testMutableRule_getPredicate_query_args_shared_variable_doesnt_unify_with_clause() {
      MutableRule a = create(MutableRule.class, "p(a,X,c) :- test.");
      Variable x = new Variable("X");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(x, atom("b"), x)));
   }

   @Test
   public void testMutableRule_getPredicate_query_args_shared_variable_unify_with_clause() {
      MutableRule a = create(MutableRule.class, "p(a,X,a) :- test.");
      Variable x = new Variable("X");
      assertSame(mockPredicate1, a.getPredicate(array(x, atom("b"), x)));
      assertEquals(atom("a"), x.getTerm());
      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testMutableRule_getPredicate_query_args_dont_unify_with_clause_shared_variable() {
      MutableRule a = create(MutableRule.class, "p(X,b,X) :- test.");
      assertSame(PredicateUtils.FALSE, a.getPredicate(array(atom("a"), atom("b"), atom("c"))));
   }

   @Test
   public void testMutableRule_getPredicate_query_args_unify_with_clause_shared_variable() {
      MutableRule a = create(MutableRule.class, "p(X,b,X) :- test.");
      assertSame(mockPredicate1, a.getPredicate(array(atom("a"), atom("b"), atom("a"))));
      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testMutableRule_getPredicate_query_args_variable_unifies_with_clause_variable() {
      MutableRule a = create(MutableRule.class, "p(a,X,c) :- test.");
      Variable variable = new Variable("A");
      assertSame(mockPredicate1, a.getPredicate(array(atom("a"), variable, atom("c"))));
      // assert query variable has been unified with clause variable
      assertSame(TermType.VARIABLE, variable.getTerm().getType());
      assertNotSame(variable, variable.getTerm());
      assertEquals("X", ((Variable) variable.getTerm()).getId());
      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @Test
   public void testMutableRule_getPredicate_query_args_variable_unifies_with_clause_atom() {
      MutableRule a = create(MutableRule.class, "p(a,X,c) :- test.");
      Variable x = new Variable("X");
      assertSame(mockPredicate1, a.getPredicate(array(atom("a"), atom("b"), x)));
      assertEquals(atom("c"), x.getTerm());
      verify(mockPredicateFactory).getPredicate(EMPTY_ARRAY);
   }

   @SuppressWarnings("unchecked")
   private <T extends ClauseAction> T create(Class<?> type, String syntax) {
      ClauseModel model = createClauseModel(syntax);
      ClauseAction result = ClauseActionFactory.createClauseAction(kb, model);
      assertClass(type, result);
      assertSame(model, result.getModel());
      return (T) result;
   }
}
