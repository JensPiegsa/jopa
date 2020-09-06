/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.owlapi.query;

import cz.cvut.kbss.ontodriver.Statement;
import cz.cvut.kbss.ontodriver.exception.VariableNotBoundException;
import cz.cvut.kbss.ontodriver.model.LangString;
import cz.cvut.kbss.ontodriver.owlapi.exception.BindingValueMismatchException;
import cz.cvut.kbss.ontodriver.owlapi.exception.OwlapiDriverException;
import cz.cvut.kbss.owl2query.model.QueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.semanticweb.owlapi.model.OWLObject;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SelectResultSetTest {

    @Mock
    private Statement statementMock;

    private final QueryResultGenerator generator = new QueryResultGenerator();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void resultSetContainsCorrectMappingOfIndexesAndLabels() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, true))));
        assertEquals(0, resultSet.findColumn("a"));
        assertEquals(1, resultSet.findColumn("b"));
    }

    private SelectResultSet resultSet(QueryResult<OWLObject> queryResult) {
        return new SelectResultSet(queryResult, statementMock);
    }

    @Test
    public void testGetFirstBooleanResultByIndex() throws Exception {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, true))));
        assertTrue(resultSet.hasNext());
        resultSet.next();
        assertFalse(resultSet.hasNext());
        assertFalse(resultSet.getBoolean(0));
        assertTrue(resultSet.getBoolean(1));
    }

    @Test
    public void testGetBooleanResultByColumnName() throws Exception {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false))));
        resultSet.next();
        assertTrue(resultSet.hasNext());
        assertFalse(resultSet.getBoolean("a"));
        assertTrue(resultSet.getBoolean("b"));
        resultSet.next();
        assertTrue(resultSet.getBoolean("a"));
        assertFalse(resultSet.getBoolean("b"));
    }

    @Test
    public void testGetInt() throws Exception {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(100, 117))));
        resultSet.next();
        assertEquals(100, resultSet.getInt(0));
        assertEquals(117, resultSet.getInt("b"));
    }

    @Test
    public void testGetLong() throws Exception {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(100111111L, 117117117117L))));
        resultSet.next();
        assertEquals(100111111L, resultSet.getLong("a"));
        assertEquals(117117117117L, resultSet.getLong(1));
    }

    @Test
    public void testGetDouble() throws Exception {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(3.14D, 4.13D))));
        resultSet.next();
        assertEquals(3.14D, resultSet.getDouble("a"), 0.0);
        assertEquals(4.13D, resultSet.getDouble(1), 0.0);
    }

    @Test
    public void getRowIndexReturnCurrentRow() {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false))));
        assertEquals(-1, resultSet.getRowIndex());
        resultSet.next();
        assertEquals(0, resultSet.getRowIndex());
        assertTrue(resultSet.isFirst());
        resultSet.next();
        assertEquals(1, resultSet.getRowIndex());
    }

    @Test
    public void firstReturnsIteratorToBeginning() {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false))));
        resultSet.next();
        resultSet.next();
        assertFalse(resultSet.isFirst());
        resultSet.first();
        assertTrue(resultSet.isFirst());
    }

    @Test
    public void previousReturnsPreviousRow() {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        resultSet.next();
        resultSet.next();
        assertEquals(2, resultSet.getRowIndex());
        resultSet.previous();
        assertEquals(1, resultSet.getRowIndex());
    }

    @Test
    public void setRowIndexLargerGoesForwardInResultSet() {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        assertEquals(0, resultSet.getRowIndex());
        resultSet.setRowIndex(2);
        assertEquals(2, resultSet.getRowIndex());
        assertFalse(resultSet.hasNext());
    }

    @Test
    public void setRowIndexSmallerGoesBackInResultSet() {
        final SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        resultSet.next();
        resultSet.next();
        assertEquals(2, resultSet.getRowIndex());
        resultSet.setRowIndex(0);
        assertEquals(0, resultSet.getRowIndex());
        assertTrue(resultSet.isFirst());
    }

    @Test
    public void setRowIndexTheSameDoesNothing() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        resultSet.next();
        assertEquals(1, resultSet.getRowIndex());
        resultSet = spy(resultSet);

        resultSet.setRowIndex(1);
        assertEquals(1, resultSet.getRowIndex());
        verify(resultSet, never()).first();
        verify(resultSet, never()).next();
    }

    @Test
    public void setRowIndexNegativeThrowsException() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        final IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> resultSet.setRowIndex(-5));
        assertThat(ex.getMessage(), containsString("Cannot set row index to a number less than 0"));
    }

    @Test
    public void setRowTooLargeGoesToEndOfResultSet() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.setRowIndex(5);
        assertEquals(2, resultSet.getRowIndex());
        assertFalse(resultSet.hasNext());
    }

    @Test
    public void relativeWithPositiveIntGoesForward() throws Exception {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        resultSet.relative(2);
        assertEquals(2, resultSet.getRowIndex());
        assertFalse(resultSet.hasNext());
        assertTrue(resultSet.getBoolean("a"));
    }

    @Test
    public void relativeWithNegativeIntGoesBackward() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        resultSet.next();
        resultSet.next();
        resultSet.relative(-2);
        assertEquals(0, resultSet.getRowIndex());
        assertTrue(resultSet.isFirst());
    }

    @Test
    public void relativeTooLargeEndsAtResultSetEnd() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.relative(5);
        assertEquals(2, resultSet.getRowIndex());
        assertFalse(resultSet.hasNext());
    }

    @Test
    public void lastGoesToEndOfResultSet() throws Exception {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Arrays.asList(
                        Arrays.asList(false, true),
                        Arrays.asList(true, false),
                        Arrays.asList(true, false))));
        resultSet.next();
        assertTrue(resultSet.hasNext());
        resultSet.last();
        assertFalse(resultSet.hasNext());
        assertFalse(resultSet.getBoolean("b"));
    }

    @Test
    public void getColumnCountReturnsNumberOfBoundVariables() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(false, true))));
        assertEquals(2, resultSet.getColumnCount());
    }

    @Test
    public void findUnknownColumnIndexReturnsMinusOne() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(false, true))));
        assertEquals(-1, resultSet.findColumn("unknown"));
    }

    @Test
    public void getValueOnClosedResultSetThrowsException() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(false, true))));
        resultSet.next();
        assertTrue(resultSet.isOpen());
        resultSet.close();
        assertFalse(resultSet.isOpen());
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> resultSet.getBoolean(1));
        assertThat(ex.getMessage(), containsString("closed"));
    }

    @Test
    public void getIncompatibleValueThrowsMismatchException() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(false, true))));
        resultSet.next();
        assertThrows(BindingValueMismatchException.class, () -> resultSet.getByte("a"));
    }

    @Test
    public void testGetString() throws Exception {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList("One", "Two"))));
        resultSet.next();
        assertEquals("One", resultSet.getString(0));
        assertEquals("Two", resultSet.getString("b"));
    }

    @Test
    public void getStringCanHandleBooleanAndNumber() throws Exception {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(false, 3.14D))));
        resultSet.next();
        assertEquals(Boolean.toString(false), resultSet.getString("a"));
        assertEquals(Double.toString(3.14D), resultSet.getString(1));
    }

    @Test
    public void getStringCanHandleOWLIndividuals() throws Exception {
        final URI uri = URI.create("http://krizik.felk.cvut.cz/ontologies#Individual");
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(uri, uri))));
        resultSet.next();
        assertEquals(uri.toString(), resultSet.getString(0));
        assertEquals(uri.toString(), resultSet.getString("b"));
    }

    @Test
    public void getValueBeforeFirstRowThrowsIllegalState() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList("One", "Two"))));
        assertThrows(IllegalStateException.class, () -> resultSet.getString(0));
    }

    @Test
    public void getValueWithUnknownIndexThrowsOntoDriverException() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList("One", "Two"))));
        resultSet.next();
        final OwlapiDriverException ex = assertThrows(OwlapiDriverException.class, () -> resultSet.getString(5));
        assertThat(ex.getMessage(), containsString("No result binding found for index"));
    }

    @Test
    public void getValueWithUnknownColumnLabelThrowsOntoDriverException() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList("One", "Two"))));
        resultSet.next();
        final OwlapiDriverException ex =
                assertThrows(OwlapiDriverException.class, () -> resultSet.getString("unknown"));
        assertThat(ex.getMessage(), containsString("No result binding found for label"));
    }

    @Test
    public void nextAfterEndThrowsNoSuchElement() {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList("One", "Two"))));
        resultSet.next();
        assertThrows(NoSuchElementException.class, resultSet::next);
    }

    @Test
    public void getObjectOnOWLLiteralReturnsLiteralValue() throws Exception {
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList("One", 117))));
        resultSet.next();
        assertThat(resultSet.getObject(0), instanceOf(LangString.class));
        assertEquals("One", ((LangString) resultSet.getObject("a")).getValue());
        assertThat(resultSet.getObject("b"), instanceOf(Integer.class));
        assertEquals(117, resultSet.getObject(1));
    }

    @Test
    public void getObjectOfOWLNamedIndividualReturnsItsUri() throws Exception {
        final URI uri = URI.create("http://krizik.felk.cvut.cz/ontologies#Individual");
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b"), Collections.singletonList(
                        Arrays.asList(uri, uri))));
        resultSet.next();
        assertEquals(uri, resultSet.getObject(0));
        assertEquals(uri, resultSet.getObject("b"));
    }

    @Test
    public void getTypedObjectOnLiteralReturnsCastedLiteral() throws Exception {
        final Date date = new Date();
        SelectResultSet resultSet = resultSet(
                generator.generate(Arrays.asList("a", "b", "c", "d", "e"), Collections.singletonList(
                        Arrays.asList("One", 117, true, 3.14D, date))));
        resultSet.next();
        assertEquals("One", resultSet.getObject(0, String.class));
        assertEquals(117, resultSet.getObject("b", Integer.class).intValue());
        assertTrue(resultSet.getObject(2, Boolean.class));
        assertEquals(date, resultSet.getObject("e", Date.class));
    }

    @Test
    public void getTypedObjectOnNamedIndividualCastToUri() throws Exception {
        final URI uri = URI.create("http://krizik.felk.cvut.cz/ontologies#Individual");
        SelectResultSet resultSet = resultSet(
                generator.generate(Collections.singletonList("a"), Collections.singletonList(
                        Collections.singletonList(uri))));
        resultSet.next();
        assertEquals(uri, resultSet.getObject("a", URI.class));
    }

    @Test
    public void getTypedObjectOnNamedIndividualCreatesInstanceWhenConstructorTakesMatchingParam() throws Exception {
        final URI uri = URI.create("http://krizik.felk.cvut.cz/ontologies#Individual");
        SelectResultSet resultSet = resultSet(
                generator.generate(Collections.singletonList("a"), Collections.singletonList(
                        Collections.singletonList(uri))));
        resultSet.next();
        final TestType tt = resultSet.getObject(0, TestType.class);
        assertNotNull(tt);
        assertEquals(uri, tt.getUri());
    }

    @Test
    public void getTypedObjectWithUnsupportedConversionThrowsDriverException() {
        final URI uri = URI.create("http://krizik.felk.cvut.cz/ontologies#Individual");
        SelectResultSet resultSet = resultSet(
                generator.generate(Collections.singletonList("a"), Collections.singletonList(
                        Collections.singletonList(uri))));
        resultSet.next();
        final OwlapiDriverException ex =
                assertThrows(OwlapiDriverException.class, () -> resultSet.getObject("a", String.class));
        assertThat(ex.getMessage(), containsString("No constructor taking parameter of type"));
    }

    @Test
    public void getNamedIndividualAsLiteralValueThrowsException() {
        final URI uri = URI.create("http://krizik.felk.cvut.cz/ontologies#Individual");
        SelectResultSet resultSet = resultSet(
                generator.generate(Collections.singletonList("a"), Collections.singletonList(
                        Collections.singletonList(uri))));
        resultSet.next();
        assertThrows(BindingValueMismatchException.class, () -> resultSet.getInt(0));
    }

    public static class TestType {
        private final URI uri;

        public TestType(URI uri) {
            this.uri = uri;
        }

        public URI getUri() {
            return uri;
        }
    }

    @Test
    public void isBoundReturnsTrueForBoundVariableIndex() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, true))));
        resultSet.next();
        assertTrue(resultSet.isBound(0));
    }

    @Test
    public void isBoundReturnsFalseForUnboundVariableIndex() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, null))));
        resultSet.next();
        assertFalse(resultSet.isBound(1));
    }

    @Test
    public void isBoundReturnsTrueForBoundVariableName() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, true))));
        resultSet.next();
        assertTrue(resultSet.isBound("b"));
    }

    @Test
    public void isBoundReturnsFalseForUnboundVariableName() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, null))));
        resultSet.next();
        assertFalse(resultSet.isBound("b"));
    }

    @Test
    public void getObjectThrowsVariableNotBoundWhenVariableIsNotBoundInCurrentRow() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(false, null))));
        resultSet.next();
        final VariableNotBoundException ex =
                assertThrows(VariableNotBoundException.class, () -> resultSet.getBoolean(1));
        assertThat(ex.getMessage(), containsString("Variable at index 1 not bound in the current result row"));
    }

    @Test
    public void getLongThrowsVariableNotBoundWhenVariableIsNotBoundInCurrentRow() {
        final SelectResultSet resultSet = resultSet(generator.generate(Arrays.asList("a", "b"),
                Collections.singletonList(
                        Arrays.asList(117L, null))));
        resultSet.next();
        final VariableNotBoundException ex =
                assertThrows(VariableNotBoundException.class, () -> resultSet.getLong("b"));
        assertThat(ex.getMessage(), containsString("Variable \"b\" not bound in the current result row"));
    }
}
