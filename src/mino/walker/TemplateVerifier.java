package mino.walker;

import mino.exception.InterpreterException;
import mino.language_mino.*;
import templor.structure.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lam on 14/03/2017.
 */
public class TemplateVerifier
        extends Walker{

    private final Template currentTemplate;

    public TemplateVerifier(
            Template template){

        this.currentTemplate = template;
    }

    public void visit(
            Node node){

        node.apply(this);
    }

    @Override
    public void caseInterpolation(
            NInterpolation node) {

        String name = node.getText().replaceAll("\\{\\{", "")
                .replaceAll("}}", "");

        if(!this.currentTemplate.isVariableExist(name)){
            //TODO gérer pour cactch l'erreur dans le visiteur TemplatesFactory
            throw new InterpreterException(
                    "Undefined attribute " + name + " in "
                    + currentTemplate.get_templateName(), node);
        }
    }

    @Override
    public void caseTerm_Block(
            NTerm_Block node) {

        String name = node.get_BlockName().getText();

        if(!this.currentTemplate.hasBlock(name)){
            throw new InterpreterException(
                    "Undefined block "  + name + " in "
                    + currentTemplate.get_templateName(), node.get_BlockName());
        }
    }

    @Override
    public void caseStm_Block(
            NStm_Block node) {

        String name = node.get_BlockName().getText();

        if(!this.currentTemplate.hasBlock(name)){
            throw new InterpreterException(
                    "Undefined block "  + name + " in "
                    + currentTemplate.get_templateName(), node.get_BlockName());
        }
    }
}
