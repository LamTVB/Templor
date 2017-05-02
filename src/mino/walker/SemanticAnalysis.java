package mino.walker;

import mino.exception.InterpreterException;
import mino.exception.SemanticException;
import mino.language_mino.*;
import mino.structure.*;

import java.util.*;

/**
 * Created by Lam on 28/03/2017.
 */
public class SemanticAnalysis
        extends Walker{

    private final ClassTable classTable;

    private ClassInfo currentClassInfo;

    private Token operatorToken;

    private ClassInfo objectClassInfo;

    private BooleanClassInfo booleanClassInfo;

    private IntegerClassInfo integerClassInfo;

    private StringClassInfo stringClassInfo;

    private FloatClassInfo floatClassInfo;

    private Map<NId, NClassName> idList;

    private Scope currentScope;

    private ClassInfo expType;

    private List<NExp> expList;

    private List<NClassName> classNames = new LinkedList<>();

    public void visit(Node node){
        node.apply(this);
    }

    public static void verify(
            Node tree,
            ClassTable classTable){

        new SemanticAnalysis(classTable).visit(tree);
    }

    private SemanticAnalysis(
            ClassTable classTable){

        this.classTable = classTable;
    }

    @Override
    public void caseFile(
            NFile node) {

        // handle compiler-known classes
        this.objectClassInfo = this.classTable.getObjectClassInfoOrNull();
        if (this.objectClassInfo == null) {
            throw new InterpreterException("class Object is not defined", null);
        }

        this.booleanClassInfo = (BooleanClassInfo) this.classTable
                .getBooleanClassInfoOrNull();
        if (this.booleanClassInfo == null) {
            throw new InterpreterException("class Boolean was not defined",
                    null);
        }

        this.integerClassInfo = (IntegerClassInfo) this.classTable
                .getIntegerClassInfoOrNull();
        if (this.integerClassInfo == null) {
            throw new InterpreterException("class Integer was not defined",
                    null);
        }

        this.stringClassInfo = (StringClassInfo) this.classTable
                .getStringClassInfoOrNull();
        if (this.stringClassInfo == null) {
            throw new InterpreterException("class String was not defined", null);
        }

        this.floatClassInfo =(FloatClassInfo) this.classTable
                .getFloatClassInfoOrNull();

        if(this.floatClassInfo == null){
            throw new InterpreterException("class Float was not defined", null);
        }

        visit(node.get_Classdefs());

        this.currentScope = new Scope(null);

        // execute statements
        visit(node.get_Stms());
    }

    private Token getOperatorToken(
            NOperator node) {

        visit(node);
        Token operatorToken = this.operatorToken;
        this.operatorToken = null;
        return operatorToken;
    }

    private ClassInfo getExpType(
            Node node){

        visit(node);
        ClassInfo expType = this.expType;
        this.expType = null;
        return expType;
    }

    private List<NExp> getExpList(
            NExpListOpt node) {

        this.expList = new LinkedList<NExp>();
        visit(node);
        List<NExp> expList = this.expList;
        this.expList = null;
        return expList;
    }

    private void verifyParameters(
            LinkedList<ClassInfo> args,
            LinkedList<VariableInfo> parameters,
            Token location){

        Iterator<ClassInfo> argsIterator = args.iterator();
        int i = 0;
        for (VariableInfo variableInfo : parameters) {
            ++i;
            if (!argsIterator.hasNext()) {
                throw new SemanticException("argument #" + i + " is missing",
                        location);
            }

            ClassInfo next = argsIterator.next();

            if (!next.isa(variableInfo.getExplicitType())) {
                throw new SemanticException("argument #" + i + " is not of "
                        + variableInfo.getExplicitType().getName() + " type", location);
            }
        }

        if (argsIterator.hasNext()) {
            throw new SemanticException(
                    "only " + i + " arguments were expected", location);
        }
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

        this.currentScope = new Scope(null, methodInfo);

        for(VariableInfo varInfo : methodInfo.getParams()){
            this.currentScope.addVariable(varInfo);
        }
        visit(node.get_Stms());
        this.currentScope = null;
    }

    @Override
    public void caseMember_Operator(
            NMember_Operator node) {

        Token operatorToken = getOperatorToken(node.get_Operator());
        MethodInfo methodInfo = this.currentClassInfo.getMethodTable().getMethodInfo(operatorToken);

        this.currentScope = new Scope(null, methodInfo);
        for(VariableInfo varInfo : methodInfo.getParams()){
            this.currentScope.addVariable(varInfo);
        }
        visit(node.get_Stms());
        this.currentScope = null;
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

    @Override
    public void caseTerm_Num(
            NTerm_Num node) {

        this.expType = this.integerClassInfo;
    }

    @Override
    public void caseTerm_True(
            NTerm_True node) {

        this.expType = this.booleanClassInfo;
    }

    @Override
    public void caseTerm_False(
            NTerm_False node) {

        this.expType = this.booleanClassInfo;
    }

    @Override
    public void caseTerm_String(
            NTerm_String node) {

        this.expType = this.stringClassInfo;
    }

    @Override
    public void caseTerm_Float(
            NTerm_Float node) {

        this.expType = this.floatClassInfo;
    }

    @Override
    public void caseTerm_Null(
            NTerm_Null node) {

        this.expType = null;
    }

    @Override
    public void caseTerm_Self(
            NTerm_Self node) {

        this.expType = this.currentClassInfo;
    }

    @Override
    public void caseTerm_Var(
            NTerm_Var node) {

        VariableInfo className = this.currentScope.getVariable(node.get_Id());
        this.expType = className.getExplicitType();
    }

    @Override
    public void caseTerm_Field(
            NTerm_Field node) {

        NClassName className = null;
        for (FieldInfo fieldInfo : this.currentClassInfo.getFieldTable().getFields()) {
            if(node.get_FieldName().getText().equals(fieldInfo.getName())){
                className = fieldInfo.getClassName();
                break;
            }
        }

        if(className == null){
            throw new InterpreterException("Field "+ node.get_FieldName().getText() +" does not have a class name", node.get_FieldName());
        }

        this.expType = this.classTable.get(className);
    }

    @Override
    public void caseTerm_New(
            NTerm_New node) {

        ClassInfo classInfo = this.classTable.get(node.get_ClassName());

        String name = classInfo.getName();
        if (name.equals("Boolean") || name.equals("Integer")
                || name.equals("String")) {
            throw new InterpreterException("invalid use of new operator",
                    node.get_NewKwd());
        }

        this.expType = classInfo;
    }

    @Override
    public void caseExp_Is(
            NExp_Is node) {

        this.expType = this.booleanClassInfo;
    }

    @Override
    public void caseExp_Eq(
            NExp_Eq node) {

        ClassInfo left = getExpType(node.get_Exp());
        ClassInfo right = getExpType(node.get_AddExp());
        Token eq = node.get_Eq();

        if(left.isa(this.stringClassInfo) && !right.isa(this.stringClassInfo)){
            throw new SemanticException("Cannot compare String with something else", eq);
        }else if((left.isa(this.integerClassInfo) || left.isa(this.floatClassInfo))
                && (!right.isa(this.integerClassInfo) || right.isa(this.floatClassInfo))){
            throw new SemanticException("Cannot compare Integer or Float with something else", eq);
        }else{
            this.expType = this.booleanClassInfo;
        }

    }

    @Override
    public void caseExp_NotEq(
            NExp_NotEq node) {

        ClassInfo left = getExpType(node.get_Exp());
        ClassInfo right = getExpType(node.get_AddExp());
        Token nEq = node.get_NotEq();

        if(left.isa(this.stringClassInfo) && !right.isa(this.stringClassInfo)){
            throw new SemanticException("Cannot compare String with something else", nEq);
        }else if((left.isa(this.integerClassInfo) || left.isa(this.floatClassInfo))
                && (!right.isa(this.integerClassInfo) & !right.isa(this.floatClassInfo))){
            throw new SemanticException("Cannot compare Integer or Float with something else", nEq);
        }else{
            this.expType = this.booleanClassInfo;
        }
    }

    @Override
    public void caseExp_LowerThan(
            NExp_LowerThan node) {

        ClassInfo left = getExpType(node.get_Exp());
        ClassInfo right = getExpType(node.get_AddExp());
        Token lt = node.get_Lt();

        booleanComparator(left, right, lt);
    }

    @Override
    public void caseExp_LowerThanEqual(
            NExp_LowerThanEqual node) {

        ClassInfo left = getExpType(node.get_Exp());
        ClassInfo right = getExpType(node.get_AddExp());
        Token lte = node.get_Lte();

        booleanComparator(left, right, lte);
    }

    @Override
    public void caseExp_GreaterThan(
            NExp_GreaterThan node) {

        ClassInfo left = getExpType(node.get_Exp());
        ClassInfo right = getExpType(node.get_AddExp());
        Token gt = node.get_Gt();

        booleanComparator(left, right, gt);
    }

    @Override
    public void caseExp_GreaterThanEqual(NExp_GreaterThanEqual node) {

        ClassInfo left = getExpType(node.get_Exp());
        ClassInfo right = getExpType(node.get_AddExp());
        Token gte = node.get_Gte();

        booleanComparator(left, right, gte);
    }

    private void booleanComparator(
            ClassInfo left,
            ClassInfo right,
            Token node){

        if(left.isa(this.stringClassInfo) && !right.isa(this.stringClassInfo)){
            throw new SemanticException("Cannot compare String with something else", node);
        }else if((left.isa(this.integerClassInfo) || left.isa(this.floatClassInfo))
                && (!right.isa(this.integerClassInfo) && !right.isa(this.floatClassInfo))){
            throw new SemanticException("Cannot compare Integer or Float with something else", node);
        }else if(left.isa(this.booleanClassInfo)){
            throw new SemanticException("Cannot compare boolean", node);
        }else{
            this.expType = this.booleanClassInfo;
        }
    }

    @Override
    public void caseExp_Isa(
            NExp_Isa node) {

        this.expType = this.booleanClassInfo;
    }

    @Override
    public void caseStm_VarAssign(
            NStm_VarAssign node) {

        ClassInfo left;
        ClassInfo expType = null;

        if(node.get_ClassNameOpt() instanceof  NClassNameOpt_One){
            NClassName explicitType = ((NClassNameOpt_One)node.get_ClassNameOpt()).get_ClassName();
            left = this.classTable.get(explicitType);
            this.currentScope.addVariable(new VariableInfo(node.get_Id().getText(), left, node.get_Id()));
        }else{
            VariableInfo leftInfo = this.currentScope.getVariable(node.get_Id());
            left = leftInfo.getExplicitType();
        }

        if(node.get_AssignOpt() instanceof NAssignOpt_One){
            NExp exp = ((NAssignOpt_One)node.get_AssignOpt()).get_Exp();
            expType = getExpType(exp);
        }

        if(expType == null){
            return;
        }

        if(left.isa(this.integerClassInfo) || left.isa(this.floatClassInfo)){
            if(!expType.isa(this.integerClassInfo) && !expType.isa(this.floatClassInfo)){
                throw new SemanticException("Cannot assign a " + expType.getName() + " to " + node.get_Id(), node.get_Id());
            }
        }else if(!expType.isa(left)){
            throw new SemanticException("Cannot assign a " + expType.getName() + " to " + node.get_Id()
                    , node.get_Id());
        }
    }

    @Override
    public void caseStm_FieldAssign(
            NStm_FieldAssign node) {

        ClassInfo explicitType = getExpType(node.get_FieldName());
        ClassInfo expType = getExpType(node.get_Exp());

        if(!explicitType.isa(expType)){
            throw new SemanticException("Cannot assign a " + expType.getName() + " to " + node.get_FieldName()
                    , node.get_FieldName());
        }
    }

    @Override
    public void caseFieldName(
            NFieldName node) {

        NClassName className = null;
        for (FieldInfo fieldInfo : this.currentClassInfo.getFieldTable().getFields()) {
            if(node.getText().equals(fieldInfo.getName())){
                className = fieldInfo.getClassName();
                break;
            }
        }

        if(className == null){
            throw new InterpreterException("Field "+ node.getText() +" does not have a class name", node);
        }

        this.expType = this.classTable.get(className);
    }

    @Override
    public void caseStm_While(
            NStm_While node) {

        ClassInfo exp = getExpType(node.get_Exp());

        if(!exp.isa(this.booleanClassInfo)){
            throw new SemanticException("Condition must be a Boolean", node.get_LPar());
        }

        Scope oldScope = this.currentScope;
        this.currentScope = new Scope(oldScope);
        visit(node.get_Stms());
        this.currentScope = oldScope;
    }

    @Override
    public void caseStm_If(
            NStm_If node) {

        ClassInfo exp = getExpType(node.get_Exp());

        if(!exp.isa(this.booleanClassInfo)){
            throw new SemanticException("Condition must be a Boolean", node.get_LPar());
        }

        Scope oldScope = this.currentScope;
        this.currentScope = new Scope(oldScope);
        visit(node.get_Stms());
        this.currentScope = oldScope;
    }

    @Override
    public void caseStm_Call(
            NStm_Call node) {

        getExpType(node.get_Call());
    }

    @Override
    public void caseCall(
            NCall node) {

        List<NExp> expList = getExpList(node.get_ExpListOpt());

        ClassInfo receiver = getExpType(node.get_RightUnaryExp());
        LinkedList<ClassInfo> argsList = new LinkedList<>();

        for(NExp exp : expList){
            argsList.add(getExpType(exp));
        }

        MethodInfo invokedMethod = receiver.getMethodTable()
                .getMethodInfo(node.get_Id());

        if(invokedMethod == null){
            throw new SemanticException("class " + this.currentClassInfo.getName()
                    + " has no " + node.get_Id().getText() + " method", node.get_Id());
        }

        verifyParameters(argsList, invokedMethod.getParams(), node.get_Id());

        if(invokedMethod.getClassReturnParam() != null){
            this.expType = invokedMethod.getClassReturnParam();
        }else{
            this.expType = receiver;
        }
    }

    @Override
    public void caseStm_SelfCall(
            NStm_SelfCall node) {

        getExpType(node.get_SelfCall());
    }

    @Override
    public void caseSelfCall(
            NSelfCall node) {

        List<NExp> expList = getExpList(node.get_ExpListOpt());
        MethodInfo methodInfo;

        if(this.currentClassInfo == null && this.currentScope.getCurrentMethod() == null){
            methodInfo = this.objectClassInfo.getMethodTable().getMethodInfo(node.get_Id());
        }else if(this.currentClassInfo != null){
            methodInfo = this.currentClassInfo.getMethodTable().getMethodInfo(node.get_Id());
        }else{
            methodInfo = this.currentScope.getCurrentMethod();
        }

        if(methodInfo == null){
            throw new SemanticException("Function " + methodInfo + " was not declared in + " +
                                        this.currentClassInfo.getName(), node.get_Id());
        }
        LinkedList<ClassInfo> argsList = new LinkedList<>();

        for(NExp exp : expList){
            argsList.add(getExpType(exp));
        }

        verifyParameters(argsList, methodInfo.getParams(), node.get_Id());
    }

    @Override
    public void caseAddExp_Add(
            NAddExp_Add node) {

        ClassInfo left = getExpType(node.get_AddExp());
        ClassInfo right = getExpType(node.get_MultExp());

        if(left == null || right == null){
            throw new InterpreterException("left or right cannot be null", node.get_Plus());
        }

        MethodInfo invokedMethod = left.getMethodTable()
                .getMethodInfo(node.get_Plus());

        if(invokedMethod == null){
            throw new SemanticException("Function of operator + was not defined on" + left.getName(), node.get_Plus());
        }
        if(!right.isa(invokedMethod.getParams().get(0).getExplicitType())){
            throw new SemanticException("Right argument must be a " + invokedMethod.getParams().get(0).getExplicitType().getName(), node.get_Plus());
        }

        if(left.isa(this.floatClassInfo)){

            if(right.isa(this.floatClassInfo) || right.isa(this.integerClassInfo)){
                this.expType = this.floatClassInfo;
            }else if(right.isa(this.stringClassInfo)){
                this.expType = this.stringClassInfo;
            }else{
                throw new SemanticException("Cannot add " + left.getName() + " with " + right.getName(), node.get_Plus());
            }
        }else if(left.isa(this.stringClassInfo)
                && (right.isa(this.stringClassInfo)
                || right.isa(this.integerClassInfo)
                || right.isa(this.floatClassInfo)
                || right.isa(this.booleanClassInfo))){

            this.expType = this.stringClassInfo;
        }else if(left.isa(this.integerClassInfo)){

            if(right.isa(this.integerClassInfo)){
                this.expType = this.integerClassInfo;
            }else if(right.isa(this.floatClassInfo)){
                this.expType = this.floatClassInfo;
            }else if(right.isa(this.stringClassInfo)){
                this.expType = this.stringClassInfo;
            }else{
                throw new SemanticException("Cannot add " + left.getName() + " with " + right.getName(), node.get_Plus());
            }
        }

        if(invokedMethod.getClassReturnParam() != null && !this.expType.isa(invokedMethod.getClassReturnParam())){
            throw new SemanticException("Return must be a " + invokedMethod.getClassReturnParam().getName() + " type." , node.get_Plus());
        }
    }

    @Override
    public void caseMultExp_Mult(
            NMultExp_Mult node) {

        ClassInfo left = getExpType(node.get_MultExp());
        ClassInfo right = getExpType(node.get_LeftUnaryExp());

        if(left == null || right == null){
            throw new InterpreterException("left or right cannot be null", node.get_Mult());
        }

        //To keep the primitives functions
        MethodInfo invokedMethod = left.getMethodTable()
                .getMethodInfo(node.get_Mult());

        if(invokedMethod == null){
            throw new SemanticException("Function of operator * was not defined on" + left.getName(), node.get_Mult());
        }
        if(!right.isa(invokedMethod.getParams().get(0).getExplicitType())){
            throw new SemanticException("Right argument must be a " + invokedMethod.getParams().get(0).getExplicitType().getName(), node.get_Mult());
        }

        if(left.isa(this.floatClassInfo)
                && (right.isa(this.floatClassInfo)
                || right.isa(this.integerClassInfo))){

            this.expType = this.floatClassInfo;
        }else if(left.isa(this.integerClassInfo)){

            if(right.isa(this.integerClassInfo)){
                this.expType = this.integerClassInfo;
            }else if(right.isa(this.floatClassInfo)){
                this.expType = this.floatClassInfo;
            }
        }else{
            throw new SemanticException("Cannot mult " + left.getName() + " with " + right.getName(), node.get_Mult());
        }

        if(invokedMethod.getClassReturnParam() != null && !this.expType.isa(invokedMethod.getClassReturnParam())){
            throw new SemanticException("Return must be a " + invokedMethod.getClassReturnParam().getName() + " type." , node.get_Mult());
        }

    }

    @Override
    public void caseMultExp_Modul(
            NMultExp_Modul node) {

        ClassInfo left = getExpType(node.get_MultExp());
        ClassInfo right = getExpType(node.get_LeftUnaryExp());

        if(left == null || right == null){
            throw new InterpreterException("left or right cannot be null", node.get_Modul());
        }

        //To keep the primitives functions
        MethodInfo invokedMethod = left.getMethodTable()
                .getMethodInfo(node.get_Modul());

        if(invokedMethod == null){
            throw new SemanticException("Function of operator % was not defined on" + left.getName(), node.get_Modul());
        }
        if(!right.isa(invokedMethod.getParams().get(0).getExplicitType())){
            throw new SemanticException("Right argument must be a " + invokedMethod.getParams().get(0).getExplicitType().getName(), node.get_Modul());
        }

        if(left.isa(this.floatClassInfo)
                && (right.isa(this.floatClassInfo)
                || right.isa(this.integerClassInfo))){

            this.expType = this.floatClassInfo;
        }else if(left.isa(this.integerClassInfo)){

            if(right.isa(this.integerClassInfo)){
                this.expType = this.integerClassInfo;
            }else if(right.isa(this.floatClassInfo)){
                this.expType = this.floatClassInfo;
            }
        }else{
            throw new SemanticException("Cannot modulo " + left.getName() + " with " + right.getName(), node.get_Modul());
        }

        if(invokedMethod.getClassReturnParam() != null && !this.expType.isa(invokedMethod.getClassReturnParam())){
            throw new SemanticException("Return must be a " + invokedMethod.getClassReturnParam().getName() + " type." , node.get_Modul());
        }
    }

    @Override
    public void caseMultExp_Div(
            NMultExp_Div node) {

        ClassInfo left = getExpType(node.get_MultExp());
        ClassInfo right = getExpType(node.get_LeftUnaryExp());

        if(left == null || right == null){
            throw new InterpreterException("left or right cannot be null", node.get_Div());
        }

        //To keep the primitives functions
        MethodInfo invokedMethod = left.getMethodTable()
                .getMethodInfo(node.get_Div());

        if(invokedMethod == null){
            throw new SemanticException("Function of operator + was not defined on" + left.getName(), node.get_Div());
        }
        if(!right.isa(invokedMethod.getParams().get(0).getExplicitType())){
            throw new SemanticException("Right argument must be a " + invokedMethod.getParams().get(0).getExplicitType().getName(), node.get_Div());
        }

        if(left.isa(this.floatClassInfo)
                && (right.isa(this.floatClassInfo)
                || right.isa(this.integerClassInfo))){

            this.expType = this.floatClassInfo;
        }else if(left.isa(this.integerClassInfo)){

            if(right.isa(this.integerClassInfo)){
                this.expType = this.integerClassInfo;
            }else if(right.isa(this.floatClassInfo)){
                this.expType = this.floatClassInfo;
            }
        }else{
            throw new SemanticException("Cannot div " + left.getName() + " with " + right.getName(), node.get_Div());
        }
    }

    @Override
    public void caseAddExp_Min(
            NAddExp_Min node) {

        ClassInfo left = getExpType(node.get_AddExp());
        ClassInfo right = getExpType(node.get_MultExp());

        if(left == null || right == null){
            throw new InterpreterException("left or right cannot be null", node.get_Min());
        }

        MethodInfo invokedMethod = left.getMethodTable()
                .getMethodInfo(node.get_Min());

        if(invokedMethod == null){
            throw new SemanticException("Function of operator + was not defined on" + left.getName(), node.get_Min());
        }
        if(!right.isa(invokedMethod.getParams().get(0).getExplicitType())){
            throw new SemanticException("Right argument must be a " + invokedMethod.getParams().get(0).getExplicitType().getName(),
                                node.get_Min());
        }

        if(left.isa(this.floatClassInfo)
                && (right.isa(this.floatClassInfo)
                || right.isa(this.integerClassInfo))){

            this.expType = this.floatClassInfo;
        }else if(left.isa(this.integerClassInfo)){

            if(right.isa(this.integerClassInfo)){
                this.expType = this.integerClassInfo;
            }else if(right.isa(this.floatClassInfo)){
                this.expType = this.floatClassInfo;
            }
        }else{
            throw new SemanticException("Cannot min " + left.getName() + " with " + right.getName(), node.get_Min());
        }

        if(invokedMethod.getClassReturnParam() != null && !this.expType.isa(invokedMethod.getClassReturnParam())){
            throw new SemanticException("Return must be a " + invokedMethod.getClassReturnParam().getName() + " type." , node.get_Min());
        }
    }

    @Override
    public void caseExpList(
            NExpList node) {

        this.expList.add(node.get_Exp());
        visit(node.get_AdditionalExps());
    }

    @Override
    public void caseAdditionalExp(
            NAdditionalExp node) {

        this.expList.add(node.get_Exp());
    }

    @Override
    public void caseStm_Return(
            NStm_Return node) {

        if(this.currentClassInfo == null){
            throw new SemanticException("Cannot return in the main program", node.get_ReturnKwd());
        }

        if(node.get_ExpOpt() instanceof NExpOpt_One){
            NExp exp = ((NExpOpt_One) node.get_ExpOpt()).get_Exp();

            MethodInfo methodInfo = this.currentScope.getCurrentMethod();

            if(methodInfo.getClassReturnParam() == null){
                throw new SemanticException("Return not necessary", node.get_ReturnKwd());
            }
            ClassInfo methodReturnClassInfo = methodInfo.getClassReturnParam();
            ClassInfo expType = getExpType(exp);

            if(!methodReturnClassInfo.isa(expType)){
                throw new SemanticException("Required return type : " + methodReturnClassInfo.getName()
                        + ". Given : " + expType.getName(), node.get_ReturnKwd());
            }
            this.expType = expType;
        }
    }
}
