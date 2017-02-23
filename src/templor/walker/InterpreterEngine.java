package templor.walker;

import templor.language_templor.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 23/02/2017.
 */
public class InterpreterEngine
        extends Walker{

    private Map<String, Node> templates = new HashMap<>();
    private Node templateDef = null;

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseStm_Call(
            NStm_Call node) {

        System.out.println(node.getText());
    }

    @Override
    public void caseInterpolation(
            NInterpolation node) {

        System.out.println("interpolation : " + node.getText());
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        System.out.println("create : " + node.get_TemplateName().getText());
        visit(node.get_Template());
        templates.put(node.get_TemplateName().getText(), this.templateDef);
    }

    @Override
    public void caseStm_Print(
            NStm_Print node) {
        visit(node.get_Template());
        System.out.println(this.templateDef.getText());
    }

    @Override
    public void caseStm_Render(
            NStm_Render node) {
        visit(node.get_Template());
        System.out.println(this.templateDef.getText());
    }

    @Override
    public void caseTemplate_Interpolation(
            NTemplate_Interpolation node) {

        this.templateDef = node.get_Interpolation();
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        this.templateDef = node.get_TemplateDef();
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        if(this.templates.containsKey(node.get_Id().getText())){
            this.templateDef = this.templates.get(node.get_Id().getText());
        }
    }
}
