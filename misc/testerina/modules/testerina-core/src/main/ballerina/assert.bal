// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

const string assertFailureErrorCategory = "assert-failure";
const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";
const int maxArgLength = 80;
const int mapValueDiffLimit = 5;

public type Comparable anydata|error|Comparable[]|map<Comparable>;

# The error struct for assertion errors.
#
# + message - The assertion error message
# + cause - The error which caused the assertion error
# + category - The assert error category
type AssertError record {
    string message = "";
    error? cause = ();
    string category = "";
};

# Creates an AssertError with custom message and category.
#
# + errorMessage - Custom message for the ballerina error
# + category - error category
#
# + return - an AssertError with custom message and category
public isolated function createBallerinaError(string errorMessage, string category) returns error {
    error e = error(errorMessage);
    return e;
}

# Asserts whether the given condition is true. If it is not, a AssertError is thrown with the given errorMessage.
#
# + condition - Boolean condition to evaluate
# + msg - Assertion error message
public isolated function assertTrue(boolean condition, string msg = "Assertion Failed!") {
    if (!condition) {
        panic createBallerinaError(msg, assertFailureErrorCategory);
    }
}

# Asserts whether the given condition is false. If it is not, a AssertError is thrown with the given errorMessage.
#
# + condition - Boolean condition to evaluate
# + msg - Assertion error message
public isolated function assertFalse(boolean condition, string msg = "Assertion Failed!") {
    if (condition) {
        panic createBallerinaError(msg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are equal. If it is not, an AssertError is thrown with the given errorMessage.
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertEquals(Comparable actual, Comparable expected, string msg = "Assertion Failed!") {
    if (!isEqual(actual, expected)) {
        string errorMsg = getInequalityErrorMsg(actual, expected, msg);
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are not equal. If it is equal, an AssertError is thrown with the given errorMessage.
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertNotEquals(Comparable actual, Comparable expected, string msg = "Assertion Failed!") {
    if (isEqual(actual, expected)) {
        string expectedStr = sprintf("%s", expected);
        string actualStr = sprintf("%s", actual);
        string errorMsg = string `${msg}: expected the actual value not to be '${expectedStr}'`;
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

isolated function isEqual(Comparable actual, Comparable expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else if (actual is error && expected is error) {
        return actual.message() == expected.message() &&
            isEqual(actual.cause(), expected.cause()) &&
            isEqual(actual.detail(), expected.detail());
    } else if (actual is map<Comparable> && expected is map<Comparable>) {
        return isEqual(actual.keys(), expected.keys()) && isEqual(actual.toArray(), actual.toArray());
    } else if (actual is Comparable[] && expected is Comparable[]) {
        var ai = actual.iterator();
        var ei = expected.iterator();
        var nextA = ai.next();
        while (nextA !== ()) {
            var nextE = ei.next();
            if (nextE is ()) {
                return false;
            } else {
                if (!(nextA is ()) && isEqual(nextA.value, nextE.value)) {
                    continue;
                }
            }
        }

        if (ei.next() is ()) {
            return true;
        }
        return false;
    } else {
        return (actual === expected);
    }
}

# Asserts whether the given values are exactly equal. If it is not, an AssertError is thrown with the given errorMessage.
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertExactEquals(any|error actual, any|error expected, string msg = "Assertion Failed!") {
    boolean isEqual = (actual === expected);
    if (!isEqual) {
        string errorMsg = getInequalityErrorMsg(actual, expected, msg);
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are not exactly equal. If it is equal, an AssertError is thrown with the given errorMessage.
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertNotExactEquals(any|error actual, any|error expected, string msg = "Assertion Failed!") {
    boolean isEqual = (actual === expected);
    if (isEqual) {
        string expectedStr = sprintf("%s", expected);
        string actualStr = sprintf("%s", actual);
        string errorMsg = string `${msg}: expected the actual value not to be '${expectedStr}'`;
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Assert failure is triggered based on user discretion. AssertError is thrown with the given errorMessage.
#
# + msg - Assertion error message
public isolated function assertFail(string msg = "Test Failed!") {
    panic createBallerinaError(msg, assertFailureErrorCategory);
}

# Get the error message to be shown when there is an inequaklity while asserting two values.
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
#
# + return - Error message constructed based on the compared values
isolated function getInequalityErrorMsg(any|error actual, any|error expected, string msg = "\nAssertion Failed!") returns @tainted string {
        string expectedType = getBallerinaType(expected);
        string actualType = getBallerinaType(actual);
        string errorMsg = "";
        string expectedStr = sprintf("%s", expected);
        string actualStr = sprintf("%s", actual);
        if (expectedStr.length() > maxArgLength) {
            expectedStr = expectedStr.substring(0, maxArgLength) + "...";
        }
        if (actualStr.length() > maxArgLength) {
            actualStr = actualStr.substring(0, maxArgLength) + "...";
        }
        if (expectedType != actualType) {
            errorMsg = string `${msg}` + "\n \nexpected: " + string `<${expectedType}> '${expectedStr}'` + "\nactual\t: "
                + string `<${actualType}> '${actualStr}'`;
        } else if (actual is string && expected is string) {
            string diff = getStringDiff(<string>actual, <string>expected);
            errorMsg = string `${msg}` + "\n \nexpected: " + string `'${expectedStr}'` + "\nactual\t: "
                                     + string `'${actualStr}'` + "\n \nDiff\t:\n" + string `${diff}`;
        } else if (actual is map<anydata> && expected is map<anydata>) {
            string diff = getMapValueDiff(<map<anydata>>actual, <map<anydata>>expected);
            errorMsg = string `${msg}` + "\n \nexpected: " + string `'${expectedStr}'` + "\nactual\t: " +
                            string `'${actualStr}'` + "\n \nDiff\t:\n" + string `${diff}`;
        } else {
            errorMsg = string `${msg}` + "\n \nexpected: " + string `'${expectedStr}'` + "\nactual\t: "
                                                 + string `'${actualStr}'`;
        }
        return errorMsg;
}

isolated function getKeyArray(map<anydata> valueMap) returns @tainted string[] {
    string[] keyArray = valueMap.keys();
    foreach string keyVal in keyArray {
        var value = valueMap.get(keyVal);
        if (value is map<anydata>) {
            string[] childKeys = getKeyArray(<map<anydata>>value);
            foreach string childKey in childKeys {
                keyArray.push(keyVal + "." + childKey);
            }
        }
    }
    return keyArray;
}

isolated function getMapValueDiff(map<anydata> actualMap, map<anydata> expectedMap) returns @tainted string {
    string diffValue = "";
    string[] actualKeyArray = getKeyArray(actualMap);
    string[] expectedKeyArray = getKeyArray(expectedMap);
    string keyDiff = getKeysDiff(actualKeyArray, expectedKeyArray);
    string valueDiff = compareMapValues(actualMap, expectedMap);
    if (keyDiff != "") {
        diffValue = diffValue.concat(keyDiff, "\n", valueDiff);
    } else {
        diffValue = diffValue.concat(valueDiff);
    }
    return diffValue;
}

isolated function getValueComparison(anydata actual, anydata expected, string keyVal, int count) returns @tainted ([string, int])  {
    int diffCount = count;
    string diff = "";
    string expectedType = getBallerinaType(expected);
    string actualType = getBallerinaType(actual);
    if (expectedType != actualType) {
        diff = diff.concat("\n", "key: ", keyVal, "\n \nexpected value\t: <", expectedType, "> ", expected.toString(),
        "\nactual value\t: <", actualType, "> ", actual.toString());
        diffCount = diffCount + 1;
    } else {
        if (actual is map<anydata> && expected is map<anydata>) {
            string[] expectedkeyArray = (<map<anydata>>expected).keys();
            string[] actualKeyArray = (<map<anydata>>actual).keys();
            int orderCount = diffCount;
            foreach string childKey in actualKeyArray {
                if (expectedkeyArray.indexOf(childKey) != ()){
                    anydata expectedChildVal = expected.get(childKey);
                    anydata actualChildVal = actual.get(childKey);
                    string childDiff;
                    if (expectedChildVal != actualChildVal) {
                        [childDiff, diffCount] = getValueComparison(actualChildVal, expectedChildVal, keyVal + "." + childKey, diffCount);
                        if (diffCount != (orderCount + 1)) {
                            diff = diff.concat("\n");
                        }
                        diff = diff.concat(childDiff);
                    }
                }

            }
        } else {
            diff = diff.concat("\n", "key: ", keyVal, "\n \nexpected value\t: ", expected.toString(),
            "\nactual value\t: ", actual.toString());
            diffCount = diffCount + 1;
        }
    }
    return [diff, diffCount];
}

isolated function compareMapValues(map<anydata> actualMap, map<anydata> expectedMap) returns @tainted string {
    string diff = "";
    map<string> comparisonMap = {};
    string[] actualKeyArray = actualMap.keys();
    string[] expectedKeyArray = expectedMap.keys();
    int count = 0;
    foreach string keyVal in actualKeyArray {
        if (expectedMap.hasKey(keyVal)) {
            anydata expected = expectedMap.get(keyVal);
            anydata actual = actualMap.get(keyVal);
            if (expected != actual) {
                string diffVal;
                [diffVal, count] = getValueComparison(actual, expected, keyVal, count);
                if (count != 1) {
                    diff = diff.concat("\n");
                }
                diff = diff.concat(diffVal);
            }
        }
    }
    if (count > mapValueDiffLimit) {
        diff = diff.concat("\n \nTotal value mismatches: " + count.toString() + "\n");
    }
    return diff;
}

isolated function sprintf(string format, (any|error)... args) returns string = @java:Method {
    name : "sprintf",
    'class : "org.ballerinalang.testerina.natives.io.Sprintf"
} external;

isolated function getBallerinaType((any|error) value) returns string = @java:Method {
    name : "getBallerinaType",
    'class : "org.ballerinalang.testerina.core.BallerinaTypeCheck"
} external;

isolated function getStringDiff(string actual, string expected) returns string = @java:Method {
     name : "getStringDiff",
     'class : "org.ballerinalang.testerina.core.AssertionDiffEvaluator"
 } external;


isolated function getKeysDiff(string[] actualKeys, string[] expectedKeys) returns string = @java:Method {
    name: "getKeysDiff",
    'class: "org.ballerinalang.testerina.core.AssertionDiffEvaluator"
} external;
