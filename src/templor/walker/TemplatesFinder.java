package templor.walker;

import mino.language_mino.LexerException;
import mino.language_mino.ParserException;

import templor.exception.InterpreterException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 24/02/2017.
 */
public class TemplatesFinder
        extends Walker {

    private Map<String, Node> templates = new HashMap<>();

    private Node templateDef;

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        visit(node.get_AddTemplate());

        if(this.templateDef == null){
            throw new InterpreterException("Template cannot be null", node.get_Eq());
        }

        String template = this.templateDef.getText().replaceAll("<\\{"," ").replaceAll("}>", " ");
        StringReader reader = new StringReader(template);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + ".");
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + ".");
            System.exit(1);
        }

        if(syntaxTree != null){
            this.templates.put(node.get_TemplateName().getText(), this.templateDef);
        }
    }

    public Map<String, Node> getTemplates(){
        return this.templates;
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        this.templateDef = node.get_TemplateDef();
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            this.templateDef = this.templates.get(template_name);
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.get_Id());
        }
    }


}
