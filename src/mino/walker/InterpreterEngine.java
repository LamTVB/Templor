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

package mino.walker;

import java.math.*;
import java.util.*;

import mino.exception.*;
import mino.language_mino.*;
import mino.structure.*;
import templor.structure.Block;
import templor.structure.Template;

public class InterpreterEngine
        extends Walker {

    private ClassTable classTable;

    private ClassInfo currentClassInfo;

    private Token operatorToken;

    private List<NExp> expList;

    private Instance expEval;

    private Template expTemplate;

    private Frame currentFrame;

    private ClassInfo objectClassInfo;

    private BooleanClassInfo booleanClassInfo;

    private IntegerClassInfo integerClassInfo;

    private StringClassInfo stringClassInfo;

    private FloatClassInfo floatClassInfo;

    private ArrayClassInfo arrayClassInfo;

    private Template _currentTemplate;

    private Map<String, Template> nameTemplateMap;

    public void visit(
            Node node,
            ClassTable classTable) {

        this.classTable = classTable;
        node.apply(this);
    }

    public void visit(
            Node tree){

        tree.apply(this);
    }


    public void visit(
            Node node,
            Template templateExecute,
            Map<String, Template> templates){

        this._currentTemplate = templateExecute;
        this.nameTemplateMap = templates;
        this.classTable.setNameToTemplateMap(this.nameTemplateMap);
        node.apply(this);
    }

    public void printStackTrace() {

        Frame frame = this.currentFrame;
        while (frame != null) {
            Token locationToken = frame.getCurrentLocation();
            String location = "";
            if (locationToken != null) {
                location = " at line " + locationToken.getLine() + " position "
                        + locationToken.getPos();
            }
            MethodInfo invokedMethod = frame.getInvokedMethod();
            if (invokedMethod != null) {
                System.err.println(" in "
                        + invokedMethod.getClassInfo().getName() + "."
                        + invokedMethod.getName() + "()" + location);
            }
            else {
                System.err.println(" in main program" + location);
            }

            frame = frame.getPreviousFrame();
        }
    }

    private Instance getExpEval(
            Node node) {

        visit(node);
        Instance expEval = this.expEval;
        this.expEval = null;
        return expEval;
    }

    private List<NExp> getExpList(
            NExpListOpt node) {

        this.expList = new LinkedList<NExp>();
        visit(node);
        List<NExp> expList = this.expList;
        this.expList = null;
        return expList;
    }

    private Template getTemplate(
            NIncludeTemplate node){

        visit(node);
        Template expTemplate = this.expTemplate;
        this.expTemplate = null;
        return expTemplate;
    }

    private Instance execute(
            MethodInfo invokedMethod,
            Frame frame,
            Token location) {

        this.currentFrame.setCurrentLocation(location);
        this.currentFrame = frame;
        try {
            invokedMethod.execute(this);
        }
        catch (ReturnException e) {
        }

        this.currentFrame = frame.getPreviousFrame();
        this.currentFrame.setCurrentLocation(null);
        return frame.getReturnValue();
    }

    @Override
    public void caseFile(
            NFile node) {

        // handle compiler-known classes
        this.objectClassInfo = this.classTable.getObjectClassInfoOrNull();
        this.booleanClassInfo = (BooleanClassInfo) this.classTable
                .getBooleanClassInfoOrNull();

        this.integerClassInfo = (IntegerClassInfo) this.classTable
                .getIntegerClassInfoOrNull();
        this.stringClassInfo = (StringClassInfo) this.classTable
                .getStringClassInfoOrNull();
        this.floatClassInfo =(FloatClassInfo) this.classTable
                .getFloatClassInfoOrNull();

        // create initial Object instance
        Instance instance = this.objectClassInfo.newInstance();

            // create initial frame
            this.currentFrame = new Frame(null, instance, null);

        // execute statements
        this.visit(node.get_Stms());
    }

    @Override
    public void caseStm_VarAssign(
            NStm_VarAssign node) {

        NExp exp = null;

        if(node.get_AssignOpt() instanceof NAssignOpt_One){
            exp = ((NAssignOpt_One)node.get_AssignOpt()).get_Exp();
            Instance value = getExpEval(exp);
            this.currentFrame.setVar(node.get_Id(), value);
        }else{
            this.currentFrame.setVar(node.get_Id(), null);
        }
    }

    @Override
    public void caseStm_FieldAssign(
            NStm_FieldAssign node) {

        Instance value = getExpEval(node.get_Exp());
        Instance self = this.currentFrame.getReceiver();
        self.setField(node.get_FieldName(), value);
    }

    @Override
    public void caseStm_While(
            NStm_While node) {

        while (true) {
            Instance value = getExpEval(node.get_Exp());
            if (value == null) {
                throw new InterpreterException("expression is null",
                        node.get_LPar());
            }

            if (value == this.booleanClassInfo.getFalse()) {
                break;
            }

            // execute statements
            visit(node.get_Stms());
        }
    }

    @Override
    public void caseStm_If(
            NStm_If node) {

        Instance value = getExpEval(node.get_Exp());

        if (value == this.booleanClassInfo.getTrue()) {
            // execute then statements
            visit(node.get_Stms());
        }
        else {
            visit(node.get_ElseOpt());
        }
    }

    @Override
    public void caseStm_Return(
            NStm_Return node) {

        if (node.get_ExpOpt() instanceof NExpOpt_One) {
            NExp exp = ((NExpOpt_One) node.get_ExpOpt()).get_Exp();
            Instance value = getExpEval(exp);
            this.currentFrame.setReturnValue(value);
        }

        throw new ReturnException();
    }

    @Override
    public void caseStm_Call(
            NStm_Call node) {

        getExpEval(node.get_Call());
    }

    @Override
    public void caseStm_SelfCall(
            NStm_SelfCall node) {

        getExpEval(node.get_SelfCall());
    }

    @Override
    public void caseStm_Foreach(
            NStm_Foreach node) {

        Instance right = this.currentFrame.getVar(node.get_ArrayName());

        if(!right.isa(this.arrayClassInfo)){
            throw new InterpreterException("Right parameter must be an array", node.get_ArrayName());
        }

        List<Instance> values = ((ArrayInstance)right).getValue();
        for(Instance b_instance : values){

            this.currentFrame.setVar(node.get_Id(), b_instance);
            visit(node.get_Stms());
        }
    }

    @Override
    public void caseStm_Populate(
            NStm_Populate node) {

        Template template = getTemplate(node.get_IncludeTemplate());
        String attributeName = node.get_Id().getText();
        Instance exp = getExpEval(node.get_Exp());

        if(template != null){
            template.addOrUpdateAttribute(attributeName, exp);
        }
    }

    @Override
    public void caseStm_TemplateIntegration(
            NStm_TemplateIntegration node) {

        Template oldTemplate = this._currentTemplate;
        this._currentTemplate = getTemplate(node.get_IncludeTemplate());
        visit(this._currentTemplate.get_parsedTemplate());
        this._currentTemplate = oldTemplate;
    }

    @Override
    public void caseStm_Expression(
            NStm_Expression node) {

        this.expEval = getExpEval(node.get_Exp());
    }

    @Override
    public void caseStm_Block(
            NStm_Block node) {

        Block block = this._currentTemplate.getBlock(node.get_BlockName().getText());
        visit(block.get_parsedBlock());
    }

    @Override
    public void caseExp_Is(
            NExp_Is node) {

        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());
        if (left == right) {
            this.expEval = this.booleanClassInfo.getTrue();
        }
        else {
            this.expEval = this.booleanClassInfo.getFalse();
        }
    }

    @Override
    public void caseExp_Eq(
            NExp_Eq node) {

        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());
        if (left == null || right == null) {
            if (left == right) {
                this.expEval = this.booleanClassInfo.getTrue();
            }
            else {
                this.expEval = this.booleanClassInfo.getFalse();
            }
        }
        else {
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(node.get_Eq());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_Eq());
        }
    }

    @Override
    public void caseExp_NotEq(
            NExp_NotEq node) {
        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());

        if (left == null || right == null) {
            if (left != right) {
                this.expEval = this.booleanClassInfo.getTrue();
            }
            else {
                this.expEval = this.booleanClassInfo.getFalse();
            }
        }
        else {
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(node.get_NotEq());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_NotEq());
        }
    }

    @Override
    public void caseExp_LowerThan(
            NExp_LowerThan node) {

        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());
        Token lt = node.get_Lt();

        if(left == null){
            throw new InterpreterException("left member cannot be null", lt);
        }else if(right == null){
            throw new InterpreterException("right member cannot be null", lt);
        }else if(left.isa(this.floatClassInfo) || left.isa(this.integerClassInfo)){

            Float leftValue;
            Float rightValue;

            if(left.isa(this.integerClassInfo)){
                leftValue = ((IntegerInstance)left).getValue().floatValue();
            }else{
                leftValue = ((FloatInstance)left).getValue();
            }

            if(right.isa(this.integerClassInfo)){
                rightValue = ((IntegerInstance)right).getValue().floatValue();
            }
            else if(right.isa(this.floatClassInfo)){
                rightValue = ((FloatInstance)right).getValue();
            }
            else{
                throw new InterpreterException("right member must be Integer or Float", lt);
            }
            if(leftValue.compareTo(rightValue) == -1){
                this.expEval = this.booleanClassInfo.getTrue();
            }else{
                this.expEval = this.booleanClassInfo.getFalse();
            }
        }else{
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(lt);
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, lt);
        }
    }

    @Override
    public void caseExp_LowerThanEqual(
            NExp_LowerThanEqual node) {

        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());
        Token lte = node.get_Lte();

        if(left == null){
            throw new InterpreterException("left member cannot be null", lte);
        }else if(right == null){
            throw new InterpreterException("right member cannot be null", lte);
        }else if(left.isa(this.floatClassInfo) || left.isa(this.integerClassInfo)){

            Float leftValue;
            Float rightValue;

            if(left.isa(this.integerClassInfo)){
                leftValue = ((IntegerInstance)left).getValue().floatValue();
            }else{
                leftValue = ((FloatInstance)left).getValue();
            }

            if(right.isa(this.integerClassInfo)){
                rightValue = ((IntegerInstance)right).getValue().floatValue();
            }
            else if(right.isa(this.floatClassInfo)){
                rightValue = ((FloatInstance)right).getValue();
            }
            else{
                throw new InterpreterException("right member must be Integer or Float", lte);
            }
            if(leftValue.compareTo(rightValue) != 1){
                this.expEval = this.booleanClassInfo.getTrue();
            }else{
                this.expEval = this.booleanClassInfo.getFalse();
            }
        }else{

            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(lte);
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, lte);
        }
    }

    @Override
    public void caseExp_GreaterThan(
            NExp_GreaterThan node) {

        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());
        Token gt = node.get_Gt();

        if(left == null){
            throw new InterpreterException("left member cannot be null", gt);
        }else if(right == null){
            throw new InterpreterException("right member cannot be null", gt);
        }else if(left.isa(this.floatClassInfo) || left.isa(this.integerClassInfo)){

            Float leftValue;
            Float rightValue;

            if(left.isa(this.integerClassInfo)){
                leftValue = ((IntegerInstance)left).getValue().floatValue();
            }else{
                leftValue = ((FloatInstance)left).getValue();
            }

            if(right.isa(this.integerClassInfo)){
                rightValue = ((IntegerInstance)right).getValue().floatValue();
            }
            else if(right.isa(this.floatClassInfo)){
                rightValue = ((FloatInstance)right).getValue();
            }
            else{
                throw new InterpreterException("right member must be Integer or Float", gt);
            }

            if(leftValue.compareTo(rightValue) == 1){
                this.expEval = this.booleanClassInfo.getTrue();
            }else{
                this.expEval = this.booleanClassInfo.getFalse();
            }
        }else{

            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(gt);
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, gt);
        }
    }

    @Override
    public void caseExp_GreaterThanEqual(NExp_GreaterThanEqual node) {

        Instance left = getExpEval(node.get_Exp());
        Instance right = getExpEval(node.get_AddExp());
        Token gte = node.get_Gte();

        if(left == null){
            throw new InterpreterException("left member cannot be null", gte);
        }else if(right == null){
            throw new InterpreterException("right member cannot be null", gte);
        }else if(left.isa(this.floatClassInfo) || left.isa(this.integerClassInfo)){

            Float leftValue;
            Float rightValue;

            if(left.isa(this.integerClassInfo)){
                leftValue = ((IntegerInstance)left).getValue().floatValue();
            }else{
                leftValue = ((FloatInstance)left).getValue();
            }

            if(right.isa(this.integerClassInfo)){
                rightValue = ((IntegerInstance)right).getValue().floatValue();
            }else if(right.isa(this.floatClassInfo)){
                rightValue = ((FloatInstance)right).getValue();
            }
            else{
                throw new InterpreterException("right member must be Integer or Float", gte);
            }

            if(leftValue.compareTo(rightValue) != -1){
                this.expEval = this.booleanClassInfo.getTrue();
            }else{
                this.expEval = this.booleanClassInfo.getFalse();
            }
        }else{

            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(gte);
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, gte);
        }
    }

    @Override
    public void caseExp_Isa(
            NExp_Isa node) {

        Instance left = getExpEval(node.get_Exp());
        ClassInfo right = this.classTable.get(node.get_ClassName());

        if (left == null) {
            this.expEval = this.booleanClassInfo.getTrue();
        }
        else if (left.isa(right)) {
            this.expEval = this.booleanClassInfo.getTrue();
        }
        else {
            this.expEval = this.booleanClassInfo.getFalse();
        }
    }

    @Override
    public void caseAddExp_Add(
            NAddExp_Add node) {

        Instance left = getExpEval(node.get_AddExp());
        Instance right = getExpEval(node.get_MultExp());
        if (left == null) {
            throw new InterpreterException("left argument of + method is null",
                    node.get_Plus());
        }
        else if (right == null) {
            throw new InterpreterException(
                    "right argument of + method is null", node.get_Plus());
        }
        else {
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable()
                    .getMethodInfo(node.get_Plus());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_Plus());
        }
    }

    @Override
    public void caseAddExp_Min(
            NAddExp_Min node) {

        Instance left = getExpEval(node.get_AddExp());
        Instance right = getExpEval(node.get_MultExp());
        if(left == null){
            throw new InterpreterException("left argument of - is null", node.get_Min());
        }
        else if(right == null){
            throw new InterpreterException("right argument of - is null", node.get_Min());
        }else{
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable().getMethodInfo(node.get_Min());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_Min());
        }
    }

    @Override
    public void caseMultExp_Div(
            NMultExp_Div node) {

        Instance left = getExpEval(node.get_MultExp());
        Instance right = getExpEval(node.get_LeftUnaryExp());

        if(left == null){
            throw new InterpreterException("left argument of / is null", node.get_Div());
        }
        else if(right == null){
            throw new InterpreterException("right argument of / is null", node.get_Div());
        }else{
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable().getMethodInfo(node.get_Div());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_Div());
        }
    }

    @Override
    public void caseMultExp_Modul(
            NMultExp_Modul node) {

        Instance left = getExpEval(node.get_MultExp());
        Instance right = getExpEval(node.get_LeftUnaryExp());

        if(left == null){
            throw new InterpreterException("left argument of % is null", node.get_Modul());
        }
        else if(right == null){
            throw new InterpreterException("right argument of % is null", node.get_Modul());
        }else{
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable().getMethodInfo(node.get_Modul());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_Modul());
        }
    }

    @Override
    public void caseMultExp_Mult(NMultExp_Mult node) {

        Instance left = getExpEval(node.get_MultExp());
        Instance right = getExpEval(node.get_LeftUnaryExp());

        if(left == null){
            throw new InterpreterException("left argument of * is null", node.get_Mult());
        }
        else if(right == null){
            throw new InterpreterException("right argument of * is null", node.get_Mult());
        }else{
            MethodInfo invokedMethod = left.getClassInfo().getMethodTable().getMethodInfo(node.get_Mult());
            Frame frame = new Frame(this.currentFrame, left, invokedMethod);
            frame.setParam(right);
            this.expEval = execute(invokedMethod, frame, node.get_Mult());
        }
    }

    @Override
    public void caseLeftUnaryExp_Not(
            NLeftUnaryExp_Not node) {

        Instance value = getExpEval(node.get_LeftUnaryExp());
        if (value == null) {
            throw new InterpreterException("expression is null", node.get_Not());
        }

        if (!value.isa(this.booleanClassInfo)) {
            throw new InterpreterException("expression is not boolean",
                    node.get_Not());
        }

        if (value == this.booleanClassInfo.getTrue()) {
            this.expEval = this.booleanClassInfo.getFalse();
        }
        else {
            this.expEval = this.booleanClassInfo.getTrue();
        }
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

        this.expEval = classInfo.newInstance();
    }

    @Override
    public void caseTerm_Field(
            NTerm_Field node) {

        Instance self = this.currentFrame.getReceiver();
        this.expEval = self.getField(node.get_FieldName());
    }

    @Override
    public void caseTerm_Var(
            NTerm_Var node) {

        this.expEval = this.currentFrame.getVar(node.get_Id());
    }

    @Override
    public void caseTerm_Num(
            NTerm_Num node) {

        this.expEval = this.integerClassInfo.newInteger(new BigInteger(node
                .get_Number().getText()));
    }

    @Override
    public void caseTerm_Null(
            NTerm_Null node) {

        this.expEval = null;
    }

    @Override
    public void caseTerm_Self(
            NTerm_Self node) {

        this.expEval = this.currentFrame.getReceiver();
    }

    @Override
    public void caseTerm_True(
            NTerm_True node) {

        this.expEval = this.booleanClassInfo.getTrue();
    }

    @Override
    public void caseTerm_False(
            NTerm_False node) {

        this.expEval = this.booleanClassInfo.getFalse();
    }

    @Override
    public void caseTerm_String(
            NTerm_String node) {

        String string = node.get_String().getText();
        this.expEval = this.stringClassInfo.newString(string.substring(1,
                string.length() - 1));
    }

    @Override
    public void caseTerm_Float(
            NTerm_Float node) {
        Float floatValue = Float.parseFloat(node.get_Float().getText());
        this.expEval = this.floatClassInfo.newFloat(floatValue);
    }

    @Override
    public void caseTerm_Array(
            NTerm_Array node) {

        List<NExp> expList = getExpList(node.get_ExpListOpt());
        List<Instance> valueList = new ArrayList<>();

        for (NExp exp : expList) {
            valueList.add(getExpEval(exp));
        }

        this.expEval = this.arrayClassInfo.newArray(valueList);
    }

    @Override
    public void caseTerm_Template(
            NTerm_Template node) {

        Template oldTemplate = this._currentTemplate;
        this._currentTemplate = getTemplate(node.get_IncludeTemplate());
        this.expEval = getExpEval(this._currentTemplate.get_parsedTemplate());
        this._currentTemplate = oldTemplate;
    }

    @Override
    public void caseTerm_Block(
            NTerm_Block node) {

        String blockName = node.get_BlockName().getText();
        Block block = this._currentTemplate.getBlock(blockName);
        this.expEval = getExpEval(block.get_parsedBlock());
    }

    @Override
    public void caseTerm_Interpolation(
            NTerm_Interpolation node) {

        String attributeName = node.get_Interpolation().getText().replaceAll("\\{\\{", "").replaceAll("}}","");
        Object value = this._currentTemplate.getValue(attributeName);

        if(value instanceof String){
            this.expEval = this.stringClassInfo.newString((String)value);
        }else if(value instanceof Integer){
            this.expEval = this.integerClassInfo.newInteger(BigInteger.valueOf((Integer)value));
        }else if(value instanceof Float){
            this.expEval = this.floatClassInfo.newFloat((Float)value);
        }else if(value instanceof Instance){
            this.expEval = (Instance)value;
        }else if(value instanceof Boolean){
            Boolean booleanValue = (Boolean)value;
            this.expEval = booleanValue ? this.booleanClassInfo.getTrue() : this.booleanClassInfo.getFalse();
        }else if(value == null){
            throw new InterpreterException("Attribute "
                    + attributeName + " is not defined in template "
                    + this._currentTemplate.get_templateName(), node.get_Interpolation());
        }else{
            throw new InterpreterException("Error", node.get_Interpolation());
        }
    }

    @Override
    public void caseIncludeTemplate_Self(
            NIncludeTemplate_Self node) {

        if(!this.currentFrame.getReceiver().getClassInfo().hasTemplate()) {
            throw new InterpreterException("Using self on undefined template on class "
                    + this.currentFrame.getReceiver().getClassInfo().getName(), node.get_TemplateKw());
        }

        this.expTemplate = this.currentFrame.getReceiver().getClassInfo().getTemplate();
    }

    @Override
    public void caseIncludeTemplate_Simple(
            NIncludeTemplate_Simple node) {

        String templateName = node.get_TemplateName().getText();
        if(!this.nameTemplateMap.containsKey(templateName)){
            throw new InterpreterException("Template "
                    + templateName
                    + " is not defined", node.get_TemplateName());
        }

        this.expTemplate = this.nameTemplateMap.get(templateName);
    }

    @Override
    public void caseCall(
            NCall node) {

        List<NExp> expList = getExpList(node.get_ExpListOpt());

        Instance receiver = getExpEval(node.get_RightUnaryExp());

        MethodInfo invokedMethod = receiver.getClassInfo().getMethodTable()
                .getMethodInfo(node.get_Id());

        Frame frame = new Frame(this.currentFrame, receiver, invokedMethod);

        for (NExp exp : expList) {
            frame.setParam(getExpEval(exp));
        }

        this.expEval = execute(invokedMethod, frame, node.get_Id());
    }

    @Override
    public void caseSelfCall(
            NSelfCall node) {

        List<NExp> expList = getExpList(node.get_ExpListOpt());

        Instance receiver = this.currentFrame.getReceiver();

        MethodInfo invokedMethod = receiver.getClassInfo().getMethodTable()
                .getMethodInfo(node.get_Id());

        Frame frame = new Frame(this.currentFrame, receiver, invokedMethod);

        for (NExp exp : expList) {
            frame.setParam(getExpEval(exp));
        }

        this.expEval = execute(invokedMethod, frame, node.get_Id());
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

    public void integerPlus(
            MethodInfo methodInfo) {

        IntegerInstance self = (IntegerInstance) this.currentFrame
                .getReceiver();

        String argInfo = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argInfo);

        //If not an integer nor a string
        if (!arg.isa(this.stringClassInfo) && !arg.isa(this.integerClassInfo) && !arg.isa(this.floatClassInfo)) {
            throw new InterpreterException("right argument is not String neither Integer",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        if(arg.isa(this.stringClassInfo)){
            String left = self.getValue().toString();
            String right = ((StringInstance) arg).getValue();
            this.currentFrame.setReturnValue(this.stringClassInfo.newString(left
                    .concat(right)));
        }else{
            BigInteger left = self.getValue();
            BigInteger right = ((IntegerInstance) arg).getValue();
            this.currentFrame.setReturnValue(this.integerClassInfo.newInteger(left
                    .add(right)));
        }

    }

    public void stringPlus(
            MethodInfo methodInfo) {

        StringInstance self = (StringInstance) this.currentFrame.getReceiver();

        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        String left = self.getValue();
        String right;

        if(arg.isa(this.integerClassInfo)){
            right = ((IntegerInstance) arg).getValue().toString();
        }else if(arg.isa(this.booleanClassInfo)){
            right = ((BooleanInstance)arg).getValue().toString();
        }else if(arg.isa(this.floatClassInfo)){
            right = ((FloatInstance) arg).getValue().toString();
        }
        else{
            right = arg.toString();
        }
        this.currentFrame.setReturnValue(this.stringClassInfo.newString(left
                .concat(right)));
    }

    public void objectAbort(
            MethodInfo methodInfo) {

        VariableInfo argInfo = methodInfo.getParamInfo(0);

        Instance arg = this.currentFrame.getParameterValueWithoutId(argInfo.getName());
        String message = "ABORT: " + ((StringInstance) arg).getValue();
        throw new InterpreterException(message, this.currentFrame
                .getPreviousFrame().getCurrentLocation());
    }

    public void integerToS(
            PrimitiveNormalMethodInfo primitiveNormalMethodInfo) {

        IntegerInstance self = (IntegerInstance) this.currentFrame
                .getReceiver();
        this.currentFrame.setReturnValue(this.stringClassInfo.newString(self
                .getValue().toString()));
    }

    public void stringToSystemOut(
            PrimitiveNormalMethodInfo primitiveNormalMethodInfo) {

        StringInstance self = (StringInstance) this.currentFrame.getReceiver();
        System.out.println(self.getValue());
    }

    public void integerDiv(
            MethodInfo methodInfo){

        IntegerInstance self = (IntegerInstance) this.currentFrame.getReceiver();
        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);
        if(!arg.isa(this.integerClassInfo)){
            throw new InterpreterException("right argument is not Integer",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        BigInteger left = self.getValue();
        BigInteger right = ((IntegerInstance)arg).getValue();

        if(right.equals(BigInteger.ZERO)){
            throw new RuntimeException("Cannot divide by 0");
        }

        this.currentFrame.setReturnValue(this.integerClassInfo.newInteger(left.divide(right)));

    }

    public void integerModul(
            MethodInfo methodInfo){

        IntegerInstance self = (IntegerInstance) this.currentFrame.getReceiver();
        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        if(!arg.isa(this.integerClassInfo)){
            throw new InterpreterException("right argument is not Integer",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        BigInteger left = self.getValue();
        BigInteger right = ((IntegerInstance)arg).getValue();

        this.currentFrame.setReturnValue(this.integerClassInfo.newInteger(left.mod(right)));

    }

    public void integerMinus(
            MethodInfo methodInfo){

        IntegerInstance self = (IntegerInstance) this.currentFrame.getReceiver();
        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        if(!arg.isa(this.integerClassInfo)){
            throw new InterpreterException("right argument is not Integer",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        BigInteger left = self.getValue();
        BigInteger right = ((IntegerInstance)arg).getValue();

        this.currentFrame.setReturnValue(this.integerClassInfo.newInteger(left.min(right)));

    }

    public void integerMult(
            MethodInfo methodInfo){

        IntegerInstance self = (IntegerInstance) this.currentFrame.getReceiver();
        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        if(!arg.isa(this.integerClassInfo)){
            throw new InterpreterException("right argument is not Integer",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        BigInteger left = self.getValue();
        BigInteger right = ((IntegerInstance)arg).getValue();

        this.currentFrame.setReturnValue(this.integerClassInfo.newInteger(left.multiply(right)));

    }

    public void floatPlus(
            MethodInfo methodInfo){
        FloatInstance self = (FloatInstance) this.currentFrame
                .getReceiver();

        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        //If not an integer nor a string
        if (!arg.isa(this.floatClassInfo) && !arg.isa(this.stringClassInfo) && !arg.isa(this.integerClassInfo)) {
            throw new InterpreterException("right argument is not Integer nor String",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        if(arg.isa(this.stringClassInfo)){
            String left = self.getValue().toString();
            String right = ((StringInstance) arg).getValue();
            this.currentFrame.setReturnValue(this.stringClassInfo.newString(left
                    .concat(right)));
        }else{
            Float left = self.getValue();
            Float right;
            if(arg.isa(this.integerClassInfo)){
                right = ((IntegerInstance) arg).getValue().floatValue();
            }else{
                right = ((FloatInstance) arg).getValue();
            }
            this.currentFrame.setReturnValue(this.floatClassInfo.newFloat(left+right));
        }
    }

    public void floatMult(
            MethodInfo methodInfo){
        FloatInstance self = (FloatInstance) this.currentFrame
                .getReceiver();

        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        //If not an integer nor a string
        if (!arg.isa(this.floatClassInfo) && !arg.isa(this.integerClassInfo)) {
            throw new InterpreterException("right argument is not Integer nor Float",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        Float left = self.getValue();
        Float right;

        if(arg.isa(this.integerClassInfo)){
            right = ((IntegerInstance) arg).getValue().floatValue();
        }else{
            right = ((FloatInstance) arg).getValue();
        }
        this.currentFrame.setReturnValue(this.floatClassInfo.newFloat(left*right));
    }

    public void floatMinus(
            MethodInfo methodInfo){
        FloatInstance self = (FloatInstance) this.currentFrame
                .getReceiver();

        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        //If not an integer nor a string
        if (!arg.isa(this.floatClassInfo) && !arg.isa(this.integerClassInfo)) {
            throw new InterpreterException("right argument is not Integer nor Float",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        Float left = self.getValue();
        Float right;
        if(arg.isa(this.integerClassInfo)){
            right = ((IntegerInstance) arg).getValue().floatValue();
        }else{
            right = ((FloatInstance) arg).getValue();
        }
        this.currentFrame.setReturnValue(this.floatClassInfo.newFloat(left-right));
    }

    public void floatDiv(
            MethodInfo methodInfo){
        FloatInstance self = (FloatInstance) this.currentFrame
                .getReceiver();

        String argName = methodInfo.getParamName(0);
        Instance arg = this.currentFrame.getParameterValueWithoutId(argName);

        //If not an integer nor a string
        if (!arg.isa(this.floatClassInfo) && !arg.isa(this.stringClassInfo) && !arg.isa(this.integerClassInfo)) {
            throw new InterpreterException("right argument is not Integer nor String",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }

        Float left = self.getValue();
        Float right;
        if(arg.isa(this.integerClassInfo)){
            right = ((IntegerInstance) arg).getValue().floatValue();
        }else{
            right = ((FloatInstance) arg).getValue();
        }
        if(right.equals(0.00f)){
            throw new InterpreterException("right argument cannot be 0",
                    this.currentFrame.getPreviousFrame().getCurrentLocation());
        }
        this.currentFrame.setReturnValue(this.floatClassInfo.newFloat(left+right));
    }

    public void floatToS(
            PrimitiveNormalMethodInfo methodInfo){

        FloatInstance self = (FloatInstance) this.currentFrame
                .getReceiver();
        this.currentFrame.setReturnValue(this.stringClassInfo.newString(self
                .getValue().toString()));
    }
}
