package mino.walker;

import mino.language_mino.*;
import mino.structure.*;

/**
 * Created by Lam on 31/03/2017.
 */
public class ClassFinder
        extends Walker{

    public final ClassTable classTable;

    private ClassInfo currentClassInfo;

    public static void find(
            Node node,
            ClassTable classTable){

        new ClassFinder(classTable).visit(node);
    }

    private ClassFinder(
            ClassTable classTable){

        this.classTable = classTable;
    }

    public void visit(
            Node tree){

        tree.apply(this);
    }

    @Override
    public void caseFile(
            NFile node) {

        visit(node.get_Classdefs());
    }

    @Override
    public void inClassdef(
            NClassdef node) {

        this.currentClassInfo = this.classTable.add(node);
    }

    @Override
    public void outClassdef(
            NClassdef node) {

        this.currentClassInfo = null;
    }
}
