/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test.symbolbynode;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS;
import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given a class.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByClassTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_class_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(ClassDefinitionNode classDefinitionNode) {
                assertSymbol(classDefinitionNode, model, CLASS, "Person");
                assertSymbol(classDefinitionNode.className(), model, CLASS, "Person");

                for (Node member : classDefinitionNode.members()) {
                    member.accept(this);
                }
            }

            @Override
            public void visit(ObjectFieldNode objectFieldNode) {
                assertSymbol(objectFieldNode, model, CLASS_FIELD, objectFieldNode.fieldName().text());
                assertSymbol(objectFieldNode.fieldName(), model, CLASS_FIELD, objectFieldNode.fieldName().text());
            }

            @Override
            public void visit(FunctionDefinitionNode functionDefinitionNode) {
                if (functionDefinitionNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                    assertSymbol(functionDefinitionNode, model, METHOD, functionDefinitionNode.functionName().text());
                    return;
                }

                functionDefinitionNode.functionBody().accept(this);
            }

            @Override
            public void visit(MethodCallExpressionNode methodCallExpressionNode) {
                assertSymbol(methodCallExpressionNode, model, METHOD, "getName");
                assertSymbol(methodCallExpressionNode.methodName(), model, METHOD, "getName");
            }

            @Override
            public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
                assertSymbol(remoteMethodCallActionNode, model, METHOD, "getAge");
                assertSymbol(remoteMethodCallActionNode.methodName(), model, METHOD, "getAge");
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 13);
    }

    private void assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), kind);
        assertEquals(symbol.get().getName().get(), name);
        incrementAssertCount();
    }
}
