package mino.walker;

import mino.exception.InterpreterException;
import mino.language_mino.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lam on 14/03/2017.
 */
public class TemplorFinder
        extends Walker{

    private final Map<String, Object> attributes;
    private final List<String> templates = new ArrayList<>();

    public TemplorFinder(
            Map<String, Object> attributes){

        this.attributes = attributes;
    }

    @Override
    public void caseInterpolation(
            NInterpolation node) {

        String name = node.getText().replaceAll("\\{\\{", "")
                .replaceAll("}}", "");

        if(!this.attributes.containsKey(name)){
            //TODO gérer pour cactch l'erreur dans le visiteur TemplatesFactory
            throw new InterpreterException("Attribute " + name + " does not exist in this template ", node);
        }
    }

    @Override
    public void caseStm_TemplateIntegration(
            NStm_TemplateIntegration node) {

        String templateName = node.get_Template().getText().substring(1, node.get_Template().getText().length() - 1);
        templates.add(templateName);
    }

    @Override
    public void caseTerm_Template(
            NTerm_Template node) {

        String templateName = node.get_Template().getText().substring(1, node.get_Template().getText().length() - 1);
        templates.add(templateName);
    }

    public void visit(
            Node node){

        node.apply(this);
    }

    public List<String> getDependentTemplates(){
        return this.templates;
    }
}
