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

public abstract class MethodInfo {

    private final MethodTable methodTable;

    private final LinkedList<VariableInfo> params = new LinkedList<>();

    private final LinkedList<String> paramNames = new LinkedList<String>();

    private final ClassInfo returnParam;

    MethodInfo(
            MethodTable methodTable,
            LinkedList<VariableInfo> params,
            ClassInfo returnParam) {

        this.methodTable = methodTable;
        this.returnParam = returnParam;

        Set<String> paramNameSet = new LinkedHashSet<String>();

        for (VariableInfo variableInfo : params) {
            String name = variableInfo.getName();
            if (paramNameSet.contains(name)) {
                throw new InterpreterException("duplicate parameter " + name,
                        variableInfo.getLocation());
            }
            paramNameSet.add(name);
            this.paramNames.add(name);
            this.params.add(variableInfo);
        }
    }

    public abstract String getName();

    public String getParamName(
            int i) {

        return this.paramNames.get(i);
    }

    public VariableInfo getParamInfo(
            int i){

        return this.params.get(i);
    }

    public int getParamCount() {

        return this.paramNames.size();
    }

    public abstract void execute(
            InterpreterEngine interpreterEngine);

    public ClassInfo getClassInfo() {

        return this.methodTable.getClassInfo();
    }

    public LinkedList<VariableInfo> getParams(){

        return this.params;
    }

    public ClassInfo getClassReturnParam(){

        return this.returnParam;
    }
}
