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

public class MethodTable {

    private final ClassInfo classInfo;

    private final Map<String, MethodInfo> nameToMethodInfoMap = new LinkedHashMap<String, MethodInfo>();

    private final LinkedHashMap<String, MethodInfo> virtualTable = new LinkedHashMap<>();

    MethodTable(
            ClassInfo classInfo) {

        this.classInfo = classInfo;
    }

    public void add(
            NMember_Method definition,
            LinkedList<VariableInfo> params,
            ClassInfo returnParam) {

        Token nameToken = definition.get_Id();
        String name = nameToken.getText();

        if (this.nameToMethodInfoMap.containsKey(name)) {
            throw new InterpreterException("duplicate definition of method "
                    + name, nameToken);
        }

        this.nameToMethodInfoMap.put(name, new NormalMethodInfo(this,
                definition, params, returnParam));
    }

    public void add(
            NMember_Operator definition,
            LinkedList<VariableInfo> params,
            ClassInfo returnParam,
            Token operatorToken) {

        String name = operatorToken.getText();

        if (this.nameToMethodInfoMap.containsKey(name)) {
            throw new InterpreterException("duplicate definition of operator "
                    + name, operatorToken);
        }

        this.nameToMethodInfoMap.put(name, new OperatorMethodInfo(this,
                definition, params, returnParam, operatorToken));
    }

    public void add(
            NMember_PrimitiveMethod definition,
            LinkedList<VariableInfo> params,
            ClassInfo returnParam) {

        Token nameToken = definition.get_Id();
        String name = nameToken.getText();

        if (this.nameToMethodInfoMap.containsKey(name)) {
            throw new InterpreterException("duplicate definition of method "
                    + name, nameToken);
        }

        this.nameToMethodInfoMap.put(name, new PrimitiveNormalMethodInfo(this,
                definition, params, returnParam));
    }

    public void add(
            NMember_PrimitiveOperator definition,
            LinkedList<VariableInfo> params,
            ClassInfo returnParam,
            Token operatorToken) {

        String name = operatorToken.getText();

        if (this.nameToMethodInfoMap.containsKey(name)) {
            throw new InterpreterException("duplicate definition of operator "
                    + name, operatorToken);
        }

        this.nameToMethodInfoMap.put(name, new PrimitiveOperatorMethodInfo(
                this, definition, params, returnParam, operatorToken));
    }

    private MethodInfo getMethodInfoOrNull(
            String name) {

        MethodInfo methodInfo = this.nameToMethodInfoMap.get(name);
        ClassInfo superClassInfo = this.classInfo.getSuperClassInfoOrNull();

        if (methodInfo == null && superClassInfo != null) {
            methodInfo = superClassInfo.getMethodTable().getMethodInfoOrNull(
                    name);
        }

        return methodInfo;
    }

    public MethodInfo getMethodInfo(
            Token nameToken) {

        String name = nameToken.getText();
        MethodInfo methodInfo = getMethodInfoOrNull(name);

        if (methodInfo == null) {
            throw new InterpreterException("class " + this.classInfo.getName()
                    + " has no " + name + " method", nameToken);
        }

        return methodInfo;
    }

    public ClassInfo getClassInfo() {

        return this.classInfo;
    }

    public void addVirtual(
            MethodInfo methodInfo){

        if(this.virtualTable.size() == 0 && this.classInfo.getSuperClassInfoOrNull() != null){
            this.setVirtualTable();
        }
        this.virtualTable.put(methodInfo.getName(), methodInfo);
    }

    public LinkedHashMap<String, MethodInfo> getVirtualTable(){

        return this.virtualTable;
    }

    private void setVirtualTable(){

        this.virtualTable.putAll(this.classInfo.getSuperClassInfoOrNull().getMethodTable().getVirtualTable());
    }
}
