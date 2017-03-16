package mino.walker;

import mino.exception.InterpreterException;
import mino.language_mino.NInterpolation;
import mino.language_mino.Node;
import mino.language_mino.Walker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 14/03/2017.
 */
public class AttributeFinder
        extends Walker{

    private final Map<String, Object> attributes;

    public AttributeFinder(
            Map<String, Object> attributes){

        this.attributes = attributes;
    }

    @Override
    public void caseInterpolation(
            NInterpolation node) {

        String name = node.getText().replaceAll("\\{\\{", "")
                .replaceAll("}}", "");

        if(!this.attributes.containsKey(name)){
            //TODO gérer pour cactch l'erreur dans le visiteur TemplatesFinder
            throw new InterpreterException("Attribute " + name + " does not exist in this template ", node);
        }
    }

    public void visit(
            Node node){

        node.apply(this);
    }
}
