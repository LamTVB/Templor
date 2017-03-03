package templor.walker;

import mino.language_mino.Parser;
import mino.language_mino.ParserException;
import mino.language_mino.LexerException;
import mino.walker.InterpreterEngine;
import templor.exception.InterpreterException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 23/02/2017.
 */
public class TemplorEngine
        extends Walker{

    private Map<String, Node> templates = new HashMap<>();
    private mino.language_mino.Node templateDef = null;
    private String stringTemplateDef = null;

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

        String templateText = getTemplateText(node.get_AddTemplate());

        if(templateText != null){
            System.out.println(templateText);
        }
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            Node template = this.templates.get(template_name);
            this.templateDef = parseTree(template);
            this.stringTemplateDef = template.getText();
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.getLine(), node.getPos());
        }
    }

    @Override
    public void caseStm_Render(
            NStm_Render node) {

        visit(node.get_AddTemplate());
        if(this.templateDef != null){
            this.interpreterEngine.visit(this.templateDef);
        }
    }

    @Override
    public void caseAddTemplate_Add(
            NAddTemplate_Add node) {

        String rightTemplate = getTemplateText(node.get_AddTemplate());
        String leftTemplate = getTemplateText(node.get_Template());

        if(rightTemplate == null){
            throw new InterpreterException("Right template should not be null", node.get_Plus().getLine(), node.get_Plus().getPos());
        }else if(leftTemplate == null){
            throw new InterpreterException("Left template should not be null", node.get_Plus().getLine(), node.get_Plus().getPos());
        }else{
            String template = rightTemplate.concat(leftTemplate);
            this.templateDef = parseTree(template);
            this.stringTemplateDef = template;
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

        this.templateDef = parseTree(node.get_TemplateDef());
        this.stringTemplateDef = formatTemplateDef(node.get_TemplateDef().getText());
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
            String templateToParse){

        StringReader reader = new StringReader(templateToParse);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new Parser(reader).parse();
        }
        catch (LexerException | IOException | ParserException e) {
            e.printStackTrace();
        }

        return syntaxTree;
    }

    public mino.language_mino.Node getTemplate(
            Node node){

        visit(node);
        mino.language_mino.Node template = this.templateDef;
        this.templateDef = null;
        return template;
    }

    public String getTemplateText(
            Node node){

        visit(node);
        String template = formatTemplateDef(this.stringTemplateDef);
        this.stringTemplateDef = null;
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
