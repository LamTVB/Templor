package templor.structure;

import templor.language_templor.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 14/03/2017.
 */
public class Template {

    private String _templateDef;

    private Map<String, Object> _attributes = new HashMap<>();

    public Template(
            String template_def){

        this._templateDef = template_def;
    }

    public void set_attributes(
            Map<String, Object> attributes){

        this._attributes = attributes;
    }

    public String get_templateDef(){
        return this._templateDef;
    }
    public void set_templateDef(String templateDef){
        this._templateDef = templateDef;
    }
}
