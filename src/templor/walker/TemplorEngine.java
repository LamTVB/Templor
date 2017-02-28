package templor.walker;

import mino.language_mino.*;
import mino.language_mino.Parser;
import mino.language_mino.ParserException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 23/02/2017.
 */
public class InterpreterEngine
        extends Walker{

    private Map<String, Node> templates = new HashMap<>();
    private Node templateDef = null;

    private TemplatesFinder templatesFinder = new TemplatesFinder();

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
            String template = this.templateDef.getText().substring(2, this.templateDef.getText().length() - 2);
            StringReader reader = new StringReader(template);
            try {
                new Parser(reader).parse();
            }
            catch (mino.language_mino.LexerException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ParserException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {
        getTemplate(node.getText());
    }

    @Override
    public void caseStm_Render(
            NStm_Render node) {
        visit(node.get_Template());
        System.out.println(this.templateDef.getText());
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        this.templateDef = node.get_TemplateDef();
    }

    public void getTemplate(
            String template_name){

        if(this.templates.containsKey(template_name)){
            this.templateDef = this.templates.get(template_name);
        }
    }
}
