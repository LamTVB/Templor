/* This file is part of Mino.
 *
 * See the NOTICE file distributed with this work for copyright information.
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

package mino.structure;

import java.util.*;

import mino.exception.*;
import mino.language_mino.*;
import mino.walker.*;

public class PrimitiveOperatorMethodInfo
        extends MethodInfo {

    private static enum Operation {
        INTEGER_PLUS,
        STRING_PLUS,
        INTEGER_DIV,
        INTEGER_MULT,
        INTEGER_MINUS,
        INTEGER_MODUL,
        FLOAT_PLUS,
        FLOAT_MINUS,
        FLOAT_MULT,
        FLOAT_DIV;
    }

    private final NMember_PrimitiveOperator definition;

    private final Token operatorToken;

    private final Operation operation;

    PrimitiveOperatorMethodInfo(
            MethodTable methodTable,
            NMember_PrimitiveOperator definition,
            LinkedList<VariableInfo> params,
            ClassInfo returnParam,
            Token operatorToken) {

        super(methodTable, params, returnParam);
        this.definition = definition;
        this.operatorToken = operatorToken;

        if (getName().equals("+")) {
            if (getParamCount() != 1) {
                throw new SemanticException(
                        "method + must have a single parameter", operatorToken);
            }

            String className = methodTable.getClassInfo().getName();
            if (className.equals("Integer")) {
                this.operation = Operation.INTEGER_PLUS;
            }
            else if (className.equals("String")) {
                this.operation = Operation.STRING_PLUS;
            }else if(className.equals("Float")){
                this.operation = Operation.FLOAT_PLUS;
            }
            else {
                throw new SemanticException(
                        "method + is not primitive in class " + className,
                        operatorToken);
            }
        }
        else if (getName().equals("==")) {
            if (getParamCount() != 1) {
                throw new SemanticException(
                        "method == must have a single parameter", operatorToken);
            }

            String className = methodTable.getClassInfo().getName();
            throw new SemanticException(
                    "method == is not primitive in class " + className,
                    operatorToken);
        }else if (getName().equals("/")){
            if(getParamCount() != 1){
                throw new SemanticException(
                        "method / must have a single parameter ", operatorToken);
            }

            String className = methodTable.getClassInfo().getName();
            if(className.equals("Integer")){
                this.operation = Operation.INTEGER_DIV;
            }else if(className.equals("Float")){
                this.operation = Operation.FLOAT_DIV;
            }else{
                throw new SemanticException(
                        "method / is not primitive in class " + className,
                        operatorToken);
            }
        }
        else if (getName().equals("*")){
            if(getParamCount() != 1){
                throw new SemanticException(
                        "method * must have a single parameter ", operatorToken);
            }

            String className = methodTable.getClassInfo().getName();
            if(className.equals("Integer")){
                this.operation = Operation.INTEGER_MULT;
            }else if(className.equals("Float")){
                this.operation = Operation.FLOAT_MULT;
            }else{
                throw new SemanticException(
                        "method * is not primitive in class " + className,
                        operatorToken);
            }
        }
        else if (getName().equals("%")){
            if(getParamCount() != 1){
                throw new SemanticException(
                        "method % must have a single parameter ", operatorToken);
            }

            String className = methodTable.getClassInfo().getName();
            if(className.equals("Integer")){
                this.operation = Operation.INTEGER_MODUL;
            }else{
                throw new SemanticException(
                        "method % is not primitive in class " + className,
                        operatorToken);
            }
        }
        else if (getName().equals("-")){
            if(getParamCount() != 1){
                throw new SemanticException(
                        "method - must have a single parameter ", operatorToken);
            }

            String className = methodTable.getClassInfo().getName();
            if(className.equals("Integer")){
                this.operation = Operation.INTEGER_MINUS;
            }else if(className.equals("Float")){
                this.operation = Operation.FLOAT_MINUS;
            }else{
                throw new SemanticException(
                        "method - is not allowed on class " + className,
                        operatorToken);
            }
        }
        else {
            // if this point is reached, there's a bug
            throw new RuntimeException("Unhandled operator " + getName());
        }
    }

    @Override
    public String getName() {

        return this.operatorToken.getText();
    }

    @Override
    public void execute(
            InterpreterEngine interpreterEngine) {

        switch (this.operation) {
            case INTEGER_PLUS:
                interpreterEngine.integerPlus(this);
                break;
            case STRING_PLUS:
                interpreterEngine.stringPlus(this);
                break;
            case INTEGER_DIV:
                interpreterEngine.integerDiv(this);
                break;
            case INTEGER_MINUS:
                interpreterEngine.integerMinus(this);
                break;
            case INTEGER_MULT:
                interpreterEngine.integerMult(this);
                break;
            case INTEGER_MODUL:
                interpreterEngine.integerModul(this);
                break;
            case FLOAT_PLUS:
                interpreterEngine.floatPlus(this);
                break;
            case FLOAT_DIV:
                interpreterEngine.floatDiv(this);
                break;
            case FLOAT_MULT:
                interpreterEngine.floatMult(this);
                break;
            case FLOAT_MINUS:
                interpreterEngine.floatMinus(this);
                break;
            default:
                throw new RuntimeException("unhandled case");
        }
    }
}
