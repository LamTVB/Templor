package templor.walker;

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

    private Map<String, mino.language_mino.Node> parsedTemplates = new HashMap<>();

    private mino.language_mino.Node templateDef;

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        visit(node.get_Template());
        if(this.templateDef != null){
            this.parsedTemplates.put(node.get_TemplateName().getText(), this.templateDef);
        }
    }

    public Map<String, mino.language_mino.Node> getTemplates(){
        return this.parsedTemplates;
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        String template = this.templateDef.getText().substring(2, this.templateDef.getText().length() - 2);
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

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.getText();

        if(this.parsedTemplates.containsKey(node.getText())){
            this.templateDef = this.parsedTemplates.get(node.getText());
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.getLine(), node.getPos());
        }
    }


}
