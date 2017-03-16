package templor.structure;

import templor.exception.InterpreterException;
import templor.language_templor.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lam on 14/03/2017.
 */
public class Template {

    private String _templateName;

    private String _templateDef;

    private Node _nodeTemplate;

    private Map<String, Object> _attributes = new HashMap<>();

    public Template(
            String name,
            String template_def,
            Map<String, Object> attributes,
            Node nodeTemplateDef){

        this._templateName = name;
        this._templateDef = template_def;
        this._attributes = attributes;
        this._nodeTemplate = nodeTemplateDef;
    }

    public Node get_nodeTemplate(){return this._nodeTemplate;}

    public String get_templateDef(){
        return this._templateDef;
    }
    public void set_templateDef(String templateDef){
        this._templateDef = templateDef;
    }

    public void addOrUpdateAttribute(
            String name,
            Object value){

        if(!this._attributes.containsKey(name)){
            throw new InterpreterException("Attribute of name " + name + " does not exist", null);
        }else if(this._attributes != null){
            this._attributes.put(name, value);
        }else{
            //TODO handle interpreterException to give node
            throw new InterpreterException("There is no attribute for the template of name " + _templateName, null);
        }
    }

    public Map<String, Object> get_attributes(){

        if(this._attributes != null){
            return this._attributes;
        }

        return null;
    }
}
