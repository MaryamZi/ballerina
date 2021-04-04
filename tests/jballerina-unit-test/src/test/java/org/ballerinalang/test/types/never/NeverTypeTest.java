/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.never;

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for ballerina never type.
 */
public class NeverTypeTest {

    private CompileResult neverTypeTestResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        neverTypeTestResult = BCompileUtil.compile("test-src/types/never/never-type.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/types/never/never-type-negative.bal");
    }


    @Test(description = "Test calling function with 'never' return type")
    public void testNeverReturnTypedFunctionCall() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverReturnTypedFunctionCall");
    }

    @Test(description = "Test inclusive record type with 'never' typed field")
    public void testInclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(neverTypeTestResult, "testInclusiveRecord");
    }

    @Test(description = "Test exclusive record type with 'never' typed field")
    public void testExclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(neverTypeTestResult, "testExclusiveRecord");
    }

    @Test(description = "Test XML with 'never' type constraint")
    public void testXMLWithNeverTypeConstraint() {
        BRunUtil.invoke(neverTypeTestResult, "testXMLWithNeverType");
    }

    @Test(description = "Test union type with 'never' type: 1")
    public void testNeverWithUnionType1() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithUnionType1");
    }

    @Test(description = "Test union type with 'never' type: 2")
    public void testNeverWithUnionType2() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithUnionType1");
    }

    @Test(description = "Test union type with 'never' type: 3")
    public void testNeverWithUnionType3() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithUnionType3");
    }

    @Test(description = "Test table's key constraint with 'never' type")
    public void testNeverWithKeyLessTable() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithKeyLessTable");
    }

    @Test(description = "Test table key constraint with 'never' type")
    public void testNeverInUnionTypedKeyConstraints() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverInUnionTypedKeyConstraints");
    }

    @Test(description = "Test 'never' type as future type param")
    public void testNeverAsFutureTypeParam() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverAsFutureTypeParam");
    }

    @Test(description = "Test 'never' type as mapping type param")
    public void testNeverAsMappingTypeParam() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverAsMappingTypeParam");
    }

    @Test
    public void testNeverTypeNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 45);
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 2, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 12, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '()', found 'never'", 16, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 25, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 31, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 36, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 41, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 58, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 58, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 62, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 66, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 76, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 81, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 91, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 96, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 105, 38);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 112, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 117, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "table key specifier '[name]' does not match with key constraint type '[never]'", 129, 34);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "table key specifier mismatch with key constraint. expected: '1' fields but found '0'", 138, 37);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml:Text', found 'never'", 147, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml<never>', found 'never'", 148, 21);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml<never>', found 'xml:Text'", 150, 26);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(xml|xml:Text)'", 152, 17);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(xml|xml:Text)'", 154, 17);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '(int|float)', found 'xml<never>'", 156, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(string|xml:Text)'", 158, 18);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '(int|string)', found 'xml<never>'", 159, 21);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type " +
                        "'never' or equivalent to type 'never'", 163, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                        "or equivalent to type 'never'", 166, 1);
        BAssertUtil.validateError(negativeCompileResult, i++, "constant cannot be defined with type 'never', " +
                        "expected a simple basic types or a map of a simple basic type", 168, 7);
        BAssertUtil.validateError(negativeCompileResult, i++, "incompatible types: expected 'never', found '()'",
                168, 17);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                        "or equivalent to type 'never'", 171, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                        "or equivalent to type 'never'", 172, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                        "cannot be of type 'never' or equivalent to type 'never'", 180, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                "cannot be of type 'never' or equivalent to type 'never'", 183, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                "cannot be of type 'never' or equivalent to type 'never'", 186, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                "cannot be of type 'never' or equivalent to type 'never'", 189, 25);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot call a remote method with return type 'never'",
                194, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot call a remote method with return type 'never'",
                195, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot call a remote method with return type 'never'",
                196, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                "or equivalent to type 'never'", 214, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter" +
                " cannot be of type 'never' or equivalent to type 'never'", 217, 48);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter" +
                " cannot be of type 'never' or equivalent to type 'never'", 221, 48);
        BAssertUtil.validateError(negativeCompileResult, i, "cannot define an object field of type 'never'" +
                " or equivalent to type 'never'", 221, 82);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Bad Sad!!.*")
    public void testNeverWithCallStmt() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithCallStmt");
    }

    @Test(dataProvider = "dataToTestNeverWithExpressions", description = "Test never type with expressions")
    public void testNeverWithExpressions(String functionName) {
        BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverWithExpressions() {
        return new Object[]{
                "testNeverWithStartAction1",
                "testNeverWithStartAction2",
                "testNeverWithTrapExpr1",
                "testNeverWithTrapExpr2",
                "testValidNeverReturnFuncAssignment"
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Bad Sad!!.*")
    public void testNeverWithMethodCallExpr() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithMethodCallExpr");
    }


    @Test(dataProvider = "dataToTestNeverTypeWithIterator", description = "Test never type with iterator")
    public void testNeverTypeWithIterator(String functionName) {
       BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverTypeWithIterator() {
        return new Object[]{
                "testNeverWithIterator1",
                "testNeverWithIterator2",
                "testNeverWithIterator3",
                "testNeverWithIterator4",
                "testNeverWithIterator5",
                "testNeverWithIterator6"
        };
    }

    @Test(dataProvider = "dataToTestNeverTypeWithForeach", description = "Test never type with foreach")
    public void testNeverTypeWithForeach(String functionName) {
        BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverTypeWithForeach() {
        return new Object[]{
                "testNeverWithForeach1",
                "testNeverWithForeach2",
                "testNeverWithForeach3",
                "testNeverWithForeach4",
                "testNeverWithForeach5",
                "testNeverWithForeach6",
                "testNeverWithForeach7",
                "testNeverWithForeach8"
        };
    }

    @Test(dataProvider = "dataToTestNeverTypeWithFromClauseInQueryExpr", description = "Test never type in from " +
            "clause in query expr")
    public void testNeverTypeWithFromClauseInQueryExpr(String functionName) {
        BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverTypeWithFromClauseInQueryExpr() {
        return new Object[]{
                "testNeverWithFromClauseInQueryExpr1",
                "testNeverWithFromClauseInQueryExpr2",
                "testNeverWithFromClauseInQueryExpr3",
                "testNeverWithFromClauseInQueryExpr4",
                "testNeverWithFromClauseInQueryExpr5"
        };
    }

    @Test(description = "Test never type in rest params and fields")
    public void testNeverWithRestParams() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithRestParamsAndFields");
    }


    @Test(description = "Test never type in remote method return type of service object")
    public void testNeverWithServiceObjFunc() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithServiceObjFunc");
    }

    @AfterClass
    public void tearDown() {
        neverTypeTestResult = null;
        negativeCompileResult = null;
    }
}
