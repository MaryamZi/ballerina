// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testIsolatedFunctionWithOnlyLocalVars() {
    int x = isolatedFunctionWithOnlyLocalVars();
    assertEquality(4, x);
}

isolated function isolatedFunctionWithOnlyLocalVars() returns int {
    int i = 1;
    int j = i + 1;
    return j + 2;
}

function testIsolatedFunctionWithLocalVarsAndParams() {
    int x = isolatedFunctionWithOnlyLocalVarsAndParams({"two": 2});
    assertEquality(6, x);
}

isolated function isolatedFunctionWithOnlyLocalVarsAndParams(map<int> m) returns int {
    int i = 1;
    int j = i + <int> m["two"];
    return j + 3;
}

final int i = 1;

final readonly & map<string> ms = {
    "first": "hello",
    "second": "world"
};

isolated function testIsolatedFunctionAccessingImmutableGlobalStorage() {
    string concat = <string> ms["first"] + <string> ms["second"];
    assertEquality("helloworld", concat);
}

isolated function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
