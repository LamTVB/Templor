package templor.walker;

import mino.language_mino.Parser;
import mino.language_mino.ParserException;
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

    private Map<String, mino.language_mino.Node> templates = new HashMap<>();
    private mino.language_mino.Node templateDef = null;

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
    public void caseStm_Print(
            NStm_Print node) {

        visit(node.get_Template());
        if(this.templateDef != null){
            System.out.println(this.templateDef.getText());
        }
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            this.templateDef = this.templates.get(template_name);
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.getLine(), node.getPos());
        }
    }

    @Override
    public void caseStm_Render(
            NStm_Render node) {

        visit(node.get_Template());
        executeTemplate();
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        parseTree(node.get_TemplateDef());
    }

    public void executeTemplate(){

        if(this.templateDef != null){
            this.interpreterEngine.visit(this.templateDef);
        }
    }

    public void parseTree(Node templateToParse){

        String template = templateToParse.getText().substring(2, templateToParse.getText().length() - 2);
        StringReader reader = new StringReader(template);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            this.templateDef = syntaxTree;
        }
        catch (mino.language_mino.LexerException | IOException | mino.language_mino.ParserException e) {
            e.printStackTrace();
        }
    }
}
