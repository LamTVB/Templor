package templor.walker;

import templor.exception.InterpreterException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;

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
        if(this.templateDef != null){
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
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.getLine(), node.getPos());
        }
    }


}
