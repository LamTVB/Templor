package mino.walker;

import mino.language_mino.*;
import mino.structure.ClassInfo;
import mino.structure.ClassTable;
import mino.structure.MethodInfo;

/**
 * Created by Lam on 31/03/2017.
 */
public class VirtualTablePrinter
        extends Walker{

    private final ClassTable classTable;

    private ClassInfo currentClassInfo;

    private Token operatorToken;

    public static void print(
            Node tree,
            ClassTable classTable){

        new VirtualTablePrinter(classTable).visit(tree);
    }

    public void visit(
            Node node){

        node.apply(this);
    }

    private VirtualTablePrinter(
            ClassTable classTable){

        this.classTable = classTable;
    }

    private Token getOperatorToken(
            NOperator node) {

        visit(node);
        Token operatorToken = this.operatorToken;
        this.operatorToken = null;
        return operatorToken;
    }

    @Override
    public void caseFile(
            NFile node) {

        visit(node.get_Classdefs());

        this.classTable.printVirtualTables();
    }

    @Override
    public void inClassdef(
            NClassdef node) {

        this.currentClassInfo = this.classTable.get(node.get_ClassName());
    }

    @Override
    public void outClassdef(
            NClassdef node) {

        this.currentClassInfo = null;
    }

    @Override
    public void caseMember_Method(
            NMember_Method node) {

        MethodInfo methodInfo = this.currentClassInfo.getMethodTable().getMethodInfo(node.get_Id());
        this.currentClassInfo.getMethodTable().addVirtual(methodInfo);
    }

    @Override
    public void caseMember_Operator(
            NMember_Operator node) {

        Token operatorToken = getOperatorToken(node.get_Operator());
        MethodInfo methodInfo = this.currentClassInfo.getMethodTable().getMethodInfo(operatorToken);
        this.currentClassInfo.getMethodTable().addVirtual(methodInfo);
    }

    @Override
    public void caseMember_PrimitiveMethod(
            NMember_PrimitiveMethod node) {

        MethodInfo methodInfo = this.currentClassInfo.getMethodTable().getMethodInfo(node.get_Id());
        this.currentClassInfo.getMethodTable().addVirtual(methodInfo);
    }

    @Override
    public void caseMember_PrimitiveOperator(
            NMember_PrimitiveOperator node) {

        Token operatorToken = getOperatorToken(node.get_Operator());
        MethodInfo methodInfo = this.currentClassInfo.getMethodTable().getMethodInfo(operatorToken);
        this.currentClassInfo.getMethodTable().addVirtual(methodInfo);
    }

    @Override
    public void caseOperator_Plus(
            NOperator_Plus node) {

        this.operatorToken = node.get_Plus();
    }

    @Override
    public void caseOperator_Min(
            NOperator_Min node) {
        this.operatorToken = node.get_Min();
    }

    @Override
    public void caseOperator_Div(
            NOperator_Div node) {
        this.operatorToken = node.get_Div();
    }

    @Override
    public void caseOperator_Modul(
            NOperator_Modul node) {
        this.operatorToken = node.get_Modul();
    }

    @Override
    public void caseOperator_Mult(
            NOperator_Mult node) {
        this.operatorToken = node.get_Mult();
    }

    @Override
    public void caseOperator_Eq(
            NOperator_Eq node) {

        this.operatorToken = node.get_Eq();
    }

    @Override
    public void caseOperator_NotEq(
            NOperator_NotEq node) {

        this.operatorToken = node.get_NotEq();
    }

    @Override
    public void caseOperator_GreaterThanEqual(
            NOperator_GreaterThanEqual node) {

        this.operatorToken = node.get_Gte();
    }

    @Override
    public void caseOperator_GreaterThan(
            NOperator_GreaterThan node) {

        this.operatorToken = node.get_Gt();
    }

    @Override
    public void caseOperator_LowerThan(
            NOperator_LowerThan node) {

        this.operatorToken = node.get_Lt();
    }

    @Override
    public void caseOperator_LowerThanEqual(
            NOperator_LowerThanEqual node) {

        this.operatorToken = node.get_Lte();
    }
}
