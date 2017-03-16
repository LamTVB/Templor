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
import java.util.Map;

/**
 * Created by Lam on 23/02/2017.
 */
public class TemplorEngine
        extends Walker{

    private Map<String, Template> templates = new HashMap<>();

    //contains the syntax tree already parse to be interpreted
    private Template tempTemplate = null;

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
//        node.applyOnChildren(this);
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

//    @Override
//    public void caseCall_Populate(
//            NCall_Populate node) {
//
//        Template template = getTemplate(node.get_Template());
//
//        if(template == null){
//            throw new InterpreterException("Template cannot be null", node.get_Lp());
//        }
//
//        String name = node.get_Id().getText();
//        String value = node.get_Exp().getText();
//
//        if(name == null){
//            throw new InterpreterException("Name of populate should not be null", node.get_Lp());
//        }else if(value == null){
//            throw new InterpreterException("Value of populate should not be null", node.get_Lp());
//        }else{
//            value = value.substring(1, value.length() - 1);
//            template.addOrUpdateAttribute(name, value);
//        }
//
//    }

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

//            this.tempTemplate = new Template(template);
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

        Template template = new Template(null, templateDef, null, node.get_TemplateDef());
        this.tempTemplate = template;
    }

    public mino.language_mino.Node parseTree(
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

    public mino.language_mino.Node parseTree(
            Template templateToParse){

        String templateDef = templateToParse.get_templateDef();
        Map<String, Object> attributes = templateToParse.get_attributes();

        if(attributes != null){
            for(Map.Entry<String, Object> attribute : attributes.entrySet()){
                templateDef = templateDef.replaceAll("\\{\\{"+attribute.getKey()+"}}", attribute.getValue().toString());
            }
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

    public Template getTemplate(
            Node node){

        visit(node);
        Template template = this.tempTemplate;
        this.tempTemplate = null;
        return template;
    }

    public String formatTemplateDef(
            String templateDef){

        if(templateDef != null){
            return templateDef.replaceAll("<\\{"," ").replaceAll("}>", " ");
        }
        return null;
    }
}
