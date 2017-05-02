package mino.walker;

import mino.language_mino.*;
import mino.structure.ClassInfo;
import mino.structure.ClassTable;
import mino.structure.VariableInfo;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Lam on 31/03/2017.
 */
public class ClassDefinitionFinder
        extends Walker{

    private LinkedList<VariableInfo> idList;

    private final ClassTable classTable;

    private Token operatorToken;

    private ClassInfo currentClassInfo;

    public static void find(
            Node tree,
            ClassTable classTable){

        new ClassDefinitionFinder(classTable).visit(tree);
    }

    public void visit(
            Node node){

        node.apply(this);
    }

    private ClassDefinitionFinder(
            ClassTable classTable){

        this.classTable = classTable;
    }

    private LinkedList<VariableInfo> getParams(
            NIdListOpt node) {

        this.idList = new LinkedList<>();
        visit(node);
        LinkedList<VariableInfo> idList = this.idList;
        this.idList = null;
        return idList;
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
    public void caseMember_Field(
            NMember_Field node) {

        this.currentClassInfo.getFieldTable().add(node);
    }

    @Override
    public void caseMember_Method(
            NMember_Method node) {

        LinkedList<VariableInfo> params = getParams(node.get_IdListOpt());

        ClassInfo returnParam = null;

        if(node.get_ReturnOpt() instanceof NReturnOpt_One){
            NClassName returnClassName = ((NReturnOpt_One)node.get_ReturnOpt()).get_ClassName();
            returnParam = this.classTable.get(returnClassName);
        }
        this.currentClassInfo.getMethodTable().add(node, params, returnParam);
    }

    @Override
    public void caseMember_Operator(
            NMember_Operator node) {

        LinkedList<VariableInfo> params = getParams(node.get_IdListOpt());
        Token operatorToken = getOperatorToken(node.get_Operator());
        ClassInfo returnParam = null;

        if(node.get_ReturnOpt() instanceof NReturnOpt_One){
            NClassName returnClassName = ((NReturnOpt_One)node.get_ReturnOpt()).get_ClassName();
            returnParam = this.classTable.get(returnClassName);
        }
        this.currentClassInfo.getMethodTable().add(node, params, returnParam, operatorToken);
    }

    @Override
    public void caseMember_PrimitiveMethod(
            NMember_PrimitiveMethod node) {

        LinkedList<VariableInfo> params = getParams(node.get_IdListOpt());
        ClassInfo returnParam = null;

        if(node.get_ReturnOpt() instanceof NReturnOpt_One){
            NClassName returnClassName = ((NReturnOpt_One)node.get_ReturnOpt()).get_ClassName();
            returnParam = this.classTable.get(returnClassName);
        }
        this.currentClassInfo.getMethodTable().add(node, params, returnParam);
    }

    @Override
    public void caseMember_PrimitiveOperator(
            NMember_PrimitiveOperator node) {

        LinkedList<VariableInfo> params = getParams(node.get_IdListOpt());
        Token operatorToken = getOperatorToken(node.get_Operator());
        ClassInfo returnParam = null;

        if(node.get_ReturnOpt() instanceof NReturnOpt_One){
            NClassName returnClassName = ((NReturnOpt_One)node.get_ReturnOpt()).get_ClassName();
            returnParam = this.classTable.get(returnClassName);
        }
        this.currentClassInfo.getMethodTable().add(node, params, returnParam, operatorToken);
    }

    @Override
    public void inIdList(
            NIdList node) {

        VariableInfo variableInfo = new VariableInfo(node.get_Id().getText(),
                                                     this.classTable.get(node.get_ClassName()),
                                                     node.get_Id());
        this.idList.add(variableInfo);
    }

    @Override
    public void caseAdditionalId(
            NAdditionalId node) {

        VariableInfo variableInfo = new VariableInfo(node.get_Id().getText(),
                                                     this.classTable.get(node.get_ClassName()),
                                                     node.get_Id());
        this.idList.add(variableInfo);
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
