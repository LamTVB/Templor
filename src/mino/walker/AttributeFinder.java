package mino.walker;

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

    private Map<String, Object> attributes = new HashMap<>();

    @Override
    public void caseInterpolation(
            NInterpolation node) {

        String name = node.getText().replaceAll("\\{\\{", "")
                .replaceAll("}}", "");

        if(!this.attributes.containsKey(name)){
            this.attributes.put(name, null);
        }
    }

    public void visit(
            Node node){

        node.apply(this);
    }

    public Map<String, Object> getAttributes(){
        return attributes;
    }
}
