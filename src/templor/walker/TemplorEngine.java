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
import java.util.List;
import java.util.Map;

/**
 * Created by Lam on 23/02/2017.
 */
public class TemplorEngine
        extends Walker{

    private Map<String, Template> templates = new HashMap<>();

    //contains the syntax tree already parse to be interpreted
    private Template tempTemplate = null;
    private Object expVal;

    private TemplatesFinder templatesFinder = new TemplatesFinder();
    private final InterpreterEngine interpreterEngine = new InterpreterEngine();

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseProgram(
            NProgram node) {

        templatesFinder.visit(node);
        this.templates = templatesFinder.getTemplates();
        node.applyOnChildren(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {
        //Override this function in order to avoid parse named templates on creation
    }

    @Override
    public void caseStm_Print(
            NStm_Print node) {

        Template template = getTemplate(node.get_AddTemplate());

        if(template != null && template.get_templateDef() != null){
            System.out.println(template.get_templateDef());
        }else{
            throw new InterpreterException("Template cannot be null", node.get_Lp());
        }
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            this.tempTemplate = this.templates.get(template_name);
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.get_Id());
        }
    }

    @Override
    public void caseStm_Populate(
            NStm_Populate node) {

        Template receiver = this.templates.get(node.get_TemplateName().getText());

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
            mino.language_mino.Node templateDef = parseTree(template);
            this.interpreterEngine.visit(templateDef);
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
            Map<String, Object> attributes = new HashMap<>();
            if(rightTemplate.get_attributes() != null){
                attributes.putAll(rightTemplate.get_attributes());
            }
            if(leftTemplate.get_attributes() != null){
                attributes.putAll(leftTemplate.get_attributes());
            }

            this.tempTemplate = new Template(null, template, attributes, null, null);
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

        String templateDef = formatTemplateDef(node.get_TemplateDef().getText());
        Template template = this.templatesFinder.createAnonymousTemplate(node.get_TemplateDef());

        if(template != null){
            this.tempTemplate = template;
        }
    }

    @Override
    public void caseExp_False(
            NExp_False node) {

        this.expVal = false;
    }

    @Override
    public void caseExp_True(
            NExp_True node) {

        this.expVal = true;
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
            Node nodeToParse){


        String template = formatTemplateDef(nodeToParse.getText());
        StringReader reader = new StringReader(template);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new Parser(reader).parse();
        }
        catch (LexerException | IOException | ParserException e) {
            e.printStackTrace();
        }

        return syntaxTree;
    }

    private mino.language_mino.Node parseTree(
            Template templateToParse){

        String templateDef = replaceAttributes(templateToParse);
        templateDef = this.replaceTemplates(templateToParse, templateDef);

        if(templateDef == null){
            throw new InterpreterException("Template def is null", null);
        }
        StringReader reader = new StringReader(templateDef);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new Parser(reader).parse();
        }
        catch (LexerException | IOException | ParserException e) {
            e.printStackTrace();
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

    private Template getTemplateByName(
            String name){

        if(this.templates.containsKey(name)){
            return this.templates.get(name);
        }else{
            throw new InterpreterException("Template of name " + name + " does not exit", null);
        }
    }

    private String replaceAttributes(
            Template templateToReplace){

        String templateDef = templateToReplace.get_templateDef();
        Map<String, Object> attributes = templateToReplace.get_attributes();

        if(attributes != null && templateDef != null){
            for(Map.Entry<String, Object> attribute : attributes.entrySet()){
                if(attribute.getValue() != null){
                    templateDef = templateDef.replaceAll("\\{\\{"+attribute.getKey()+"}}", attribute.getValue().toString());
                }
            }
        }

        return templateDef;
    }

    private String replaceTemplates(
            Template templateToReplace,
            String... template){

        String templateDef;

        if(template != null && template.length > 0){
            templateDef = template[0];
        }else{
            templateDef = templateToReplace.get_templateDef();
        }

        List<Template> integratedTemplates = templateToReplace.get_integratedTemplates();

        if(integratedTemplates != null){
            for(Template b_template : integratedTemplates){
                if(b_template.get_templateDef() != null){
                    String subTemplateDef = replaceAttributes(b_template);
                    templateDef = templateDef.replaceAll("\\{"+ b_template.get_templateName() + "}", subTemplateDef);
                }
            }
        }

        return templateDef;
    }
}
