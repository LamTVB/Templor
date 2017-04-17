package templor.walker;

import mino.language_mino.Parser;
import mino.language_mino.ParserException;
import mino.language_mino.LexerException;
import mino.walker.InterpreterEngine;
import templor.exception.InterpreterException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;
import templor.structure.Template;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Lam on 23/02/2017.
 */
public class TemplorEngine
        extends Walker{

    private Map<String, Template> templatesMap = new HashMap<>();

    private Template tempTemplate = null;
    private Object expVal;

    private TemplatesFactory templatesFactory = new TemplatesFactory();
    private final InterpreterEngine interpreterEngine = new InterpreterEngine();

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseProgram(
            NProgram node) {

        templatesFactory.visit(node);
        this.templatesMap = templatesFactory.getTemplates();
        node.applyOnChildren(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {
        //Override this function in order to avoid parse named templatesMap on creation
    }

    @Override
    public void caseStm_Print(
            NStm_Print node) {

        Template template = getTemplate(node.get_AddTemplate());

        if(template != null && template.get_templateDef() != null){
            System.out.println(formatTemplateDef(template.get_templateDef()));
        }else{
            throw new InterpreterException("Template cannot be null", node.get_Lp());
        }
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templatesMap.containsKey(template_name)){
            this.tempTemplate = this.templatesMap.get(template_name);
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.get_Id());
        }
    }

    @Override
    public void caseStm_Populate(
            NStm_Populate node) {

        Template receiver = this.templatesMap.get(node.get_TemplateName().getText());

        if(receiver == null){
            throw new InterpreterException("Template of name " + node.get_TemplateName().getText() + " does not exist", node.get_TemplateName());
        }

        Object exp = getExpVal(node.get_Exp());
        String attributeName = node.get_AttributeName().getText();

        if(attributeName == null){
            throw new InterpreterException("Attribute name cannot be null", node.get_AttributeName());
        }else if(exp == null){
            throw new InterpreterException("Expression cannot be null", node.get_TemplateName());
        }else{
            receiver.addOrUpdateAttribute(attributeName, exp);
        }
    }

    @Override
    public void caseStm_Render(
            NStm_Render node) {

        Template template = getTemplate(node.get_AddTemplate());

        if(template != null){
            executeMino(template);
        }else{
            throw new InterpreterException("Template to render cannot be null", node.get_Lp());
        }
    }

    @Override
    public void caseAddTemplate_Add(
            NAddTemplate_Add node) {

        Template rightTemplate = getTemplate(node.get_AddTemplate());
        Template leftTemplate = getTemplate(node.get_Template());

        if(rightTemplate == null){
            throw new InterpreterException("Right template should not be null", node.get_Plus());
        }else if(leftTemplate == null){
            throw new InterpreterException("Left template should not be null", node.get_Plus());
        }else{
            String rightTemplateDef = rightTemplate.get_templateDef();
            String leftTemplateDef = leftTemplate.get_templateDef();

            String template = rightTemplateDef.concat(leftTemplateDef);
            Map<String, Object> attributes = new LinkedHashMap<>();
            if(rightTemplate.get_attributes() != null){
                attributes.putAll(rightTemplate.get_attributes());
            }
            if(leftTemplate.get_attributes() != null){
                attributes.putAll(leftTemplate.get_attributes());
            }

            this.tempTemplate = new Template(null, null, template, attributes, null, null);
        }
    }

    @Override
    public void caseAddTemplate_Simple(
            NAddTemplate_Simple node) {

        visit(node.get_Template());
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        Template template = this.templatesFactory.createAnonymousTemplate(node.get_TemplateDef());
        this.tempTemplate = template;
    }

    @Override
    public void caseExp_False(
            NExp_False node) {

        this.expVal = Boolean.FALSE;
    }

    @Override
    public void caseExp_True(
            NExp_True node) {

        this.expVal = Boolean.TRUE;
    }

    @Override
    public void caseExp_Num(
            NExp_Num node) {

        this.expVal = Integer.parseInt(node.get_Number().getText());
    }

    @Override
    public void caseExp_String(
            NExp_String node) {

        this.expVal = node.get_String().getText();
    }

    private mino.language_mino.Node parseTree(
            Template templateToParse){

        String templateDef = formatTemplateDef(templateToParse.get_templateDef());

        if(templateDef == null){
            throw new InterpreterException("Template def is null", null);
        }
        StringReader reader = new StringReader(templateDef);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new Parser(reader).parse();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + " on definition of templateDef : "
                    + templateToParse.get_templateName() + " while initializing all templates");
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + " on definition of templateDef : "
                    + templateToParse.get_templateName() + " while initializing all templates");
            System.exit(1);
        }

        return syntaxTree;
    }

    private Template getTemplate(
            Node node){

        visit(node);
        Template template = this.tempTemplate;
        this.tempTemplate = null;
        return template;
    }

    private String formatTemplateDef(
            String templateDef){

        if(templateDef != null){
            return templateDef.replaceAll("<\\{"," ").replaceAll("}>", " ");
        }
        return null;
    }

    private Object getExpVal(Node node){

        visit(node);
        Object expression = this.expVal;
        this.expVal = null;
        return expression;
    }

    private void executeMino(
            Template template){

        mino.language_mino.Node templateDef = parseTree(template);
        try{
            this.interpreterEngine.visit(templateDef, template, this.templatesMap);
        }catch(mino.exception.InterpreterException e){
            System.err.println("INTERPRETER ERROR: " + e.getMessage() + " on execution of template : "
                    + template.get_templateName());
            System.exit(1);
        }
    }
}
