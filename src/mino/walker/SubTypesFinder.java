package mino.walker;

import mino.language_mino.*;
import mino.structure.ClassInfo;
import mino.structure.ClassTable;

/**
 * Created by Lam on 31/03/2017.
 */
public class SubTypesFinder extends Walker{

    private final ClassTable classTable;

    private ClassInfo currentClassInfo;

    private Token operatorToken;

    public static void print(
            Node tree,
            ClassTable classTable){

        new SubTypesFinder(classTable).visit(tree);
    }

    public void visit(
            Node node){

        node.apply(this);
    }

    private SubTypesFinder(
            ClassTable classTable){

        this.classTable = classTable;
    }

    @Override
    public void caseFile(
            NFile node) {

        visit(node.get_Classdefs());

        this.classTable.printSubTypesTables();
    }

    @Override
    public void caseClassdef(
            NClassdef node) {

        ClassInfo childClassInfo =  this.classTable.get(node.get_ClassName());
        childClassInfo.addSubTypes(childClassInfo);
    }
}
