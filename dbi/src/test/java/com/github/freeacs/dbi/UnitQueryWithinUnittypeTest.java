package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import org.junit.Test;

public class UnitQueryWithinUnittypeTest {

  @Test
  public void testFixedOpIsNotNullAndVarOpIsNull() {
    // Given:
    final String fixedOp = "1";
    final String varOp = null;
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testFixedOpIsNullAndVarOpIsNull() {
    // Given:
    final String fixedOp = null;
    final String varOp = null;
    final Parameter.Operator op = Parameter.Operator.EQ;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testFixedOpIsNullAndVarOpIsSomething() {
    // Given:
    final String fixedOp = null;
    final String varOp = "1";
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testNoMatchInvalidNumber() {
    // Given:
    final String fixedOp = "A";
    final String varOp = "1";
    final Parameter.Operator op = Parameter.Operator.GT;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertFalse(match);
  }

  @Test
  public void testGreaterThanMatchNumber() {
    // Given:
    final String fixedOp = "2";
    final String varOp = "1";
    final Parameter.Operator op = Parameter.Operator.GT;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testLesserThanMatchNumber() {
    // Given:
    final String fixedOp = "1";
    final String varOp = "2";
    final Parameter.Operator op = Parameter.Operator.LT;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testNoMatchNumber() {
    // Given:
    final String fixedOp = "1";
    final String varOp = "2";
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testMatchNumber() {
    // Given:
    final String fixedOp = "1";
    final String varOp = "1";
    final Parameter.Operator op = Parameter.Operator.EQ;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.NUMBER;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testGreaterThanOrEqualMatch() {
    // Given:
    final String fixedOp = "1";
    final String varOp = "1";
    final Parameter.Operator op = Parameter.Operator.GE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testGreaterThanMatch() {
    // Given:
    final String fixedOp = "2";
    final String varOp = "1";
    final Parameter.Operator op = Parameter.Operator.GT;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testLesserThanMatch() {
    // Given:
    final String fixedOp = "1";
    final String varOp = "2";
    final Parameter.Operator op = Parameter.Operator.LT;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testSimpleMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "addald";
    final Parameter.Operator op = Parameter.Operator.EQ;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testSimpleNoMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "addald";
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertFalse(match);
  }

  @Test
  public void testWildCardAndSingleCharacterMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "%ddal_";
    final Parameter.Operator op = Parameter.Operator.EQ;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testWildCardMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "%ddald";
    final Parameter.Operator op = Parameter.Operator.EQ;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testSingleCharacterMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "addal_";
    final Parameter.Operator op = Parameter.Operator.EQ;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertTrue(match);
  }

  @Test
  public void testWildCardAndSingleCharacterNoMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "%ddal_";
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertFalse(match);
  }

  @Test
  public void testWildCardNoMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "%ddald";
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertFalse(match);
  }

  @Test
  public void testSingleCharacterNoMatch() {
    // Given:
    final String fixedOp = "addald";
    final String varOp = "addal_";
    final Parameter.Operator op = Parameter.Operator.NE;
    final Parameter.ParameterDataType dataType = Parameter.ParameterDataType.TEXT;

    // When:
    boolean match = UnitQueryWithinUnittype.match(fixedOp, varOp, op, dataType);

    // Then:
    assertFalse(match);
  }
}
