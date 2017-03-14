package templor.walker;

import mino.language_mino.LexerException;
import mino.language_mino.ParserException;

import mino.walker.AttributeFinder;
import templor.exception.InterpreterException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;
import templor.structure.Template;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 24/02/2017.
 */
public class TemplatesFinder
        extends Walker {

    private Map<String, Template> templates = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();

    private Template template;

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        visit(node.get_AddTemplate());

        if(this.template == null){
            throw new InterpreterException("Template cannot be null", node.get_Eq());
        }

        String stringTemplateDef = this.template.get_templateDef().replaceAll("<\\{"," ").replaceAll("}>", " ");
        StringReader reader = new StringReader(stringTemplateDef);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            AttributeFinder engine = new AttributeFinder();
            engine.visit(syntaxTree);
            attributes.putAll(engine.getAttributes());
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
            Template template = new Template(stringTemplateDef);
            this.templates.put(node.get_TemplateName().getText(), template);
        }
    }

    public Map<String, Template> getTemplates(){
        return this.templates;
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        String templateDef = node.get_TemplateDef().getText().replaceAll("<\\{"," ").replaceAll("}>", " ");
        this.template = new Template(templateDef);
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            this.template = this.templates.get(template_name);
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.get_Id());
        }
    }

    public Map<String, Object> getAttributes(){
        return attributes;
    }
}
