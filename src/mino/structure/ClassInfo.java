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

import mino.exception.*;
import mino.language_mino.*;
import templor.structure.Template;

public class ClassInfo {

    private final ClassTable classTable;

    private final NClassdef definition;

    private final ClassInfo superClass;

    private final MethodTable methodTable = new MethodTable(this);

    private final FieldTable fieldTable = new FieldTable(this);

    private Template template;

    ClassInfo(
            ClassTable classTable,
            NClassdef definition,
            Template template) {

        this.classTable = classTable;
        this.definition = definition;
        this.template = template;

        if (getName().equals("Object")) {
            // Object
            if (definition.get_SpecialOpt() instanceof NSpecialOpt_One) {
                throw new InterpreterException(
                        "class Object may not have a super class",
                        definition.get_ClassName());
            }

            if(definition.get_TemplateOpt() instanceof NTemplateOpt_One){
                throw new InterpreterException(
                        "class may not have a template",
                        definition.get_ClassName());
            }
            this.superClass = null;
        }
        else if (definition.get_SpecialOpt() instanceof NSpecialOpt_Zero) {
            // implicit Object super class
            ClassInfo objectClass = classTable.getObjectClassInfoOrNull();
            if (objectClass == null) {
                throw new InterpreterException(
                        "class Object has not yet been defined",
                        definition.get_ClassName());
            }
            this.superClass = objectClass;
        }
        else {
            // explicit super class
            NSpecial aSpecial = ((NSpecialOpt_One) definition.get_SpecialOpt())
                    .get_Special();

            String superClassName = aSpecial.get_ClassName().getText();
            if (superClassName.equals("Boolean")
                    || superClassName.equals("Integer")
                    || superClassName.equals("String")) {
                throw new InterpreterException("class " + superClassName
                        + " cannot be specialized", aSpecial.get_ClassName());
            }

            this.superClass = classTable.get(aSpecial.get_ClassName());
        }
    }

    public String getName() {

        return this.definition.get_ClassName().getText();
    }

    public MethodTable getMethodTable() {

        return this.methodTable;
    }

    public FieldTable getFieldTable() {

        return this.fieldTable;
    }

    public ClassInfo getSuperClassInfoOrNull() {

        return this.superClass;
    }

    public Instance newInstance() {

        return new Instance(this);
    }

    public boolean isa(
            ClassInfo classInfo) {

        if (this == classInfo) {
            return true;
        }

        if (this.superClass != null) {
            return this.superClass.isa(classInfo);
        }

        return false;
    }

    public Template getTemplate(){

        return this.template;
    }

    public boolean hasTemplate(){

        return this.template != null;
    }
}
