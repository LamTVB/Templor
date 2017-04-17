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
public class TemplorFinder
        extends Walker{

    private final Map<String, Object> attributes;
    private final Template _parent;

    public TemplorFinder(
            Map<String, Object> attributes,
            Template parent){

        this.attributes = attributes;
        this._parent = parent;
    }

    @Override
    public void caseInterpolation(
            NInterpolation node) {

        String name = node.getText().replaceAll("\\{\\{", "")
                .replaceAll("}}", "");

        if(this.attributes != null && !this.attributes.containsKey(name) && this._parent == null){
            //TODO gérer pour cactch l'erreur dans le visiteur TemplatesFactory
            throw new InterpreterException("Attribute " + name + " does not exist in this template ", node);
        }else{
            if(this._parent != null && !this._parent.isVariableExist(name)) {
                throw new InterpreterException("Attribute " + name + " does not exist in this template ", node);
            }
        }
    }

    public void visit(
            Node node){

        node.apply(this);
    }
}
