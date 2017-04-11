package templor.structure;

import mino.language_mino.Node;
import templor.exception.InterpreterException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lam on 14/03/2017.
 */
public class Template {

    private Template _parent;

    private String _templateName;

    private String _templateDef;

    private Node _parsedTemplate;

    private Map<String, Object> _attributes = new HashMap<>();

    private List<Template> _integratedTemplates;

    private Map<String, Template> subTypes = new HashMap<>();

    public Template(
            Template parent,
            String name,
            String template_def,
            Map<String, Object> attributes,
            Node nodeTemplateDef,
            List<Template> templates){

        this._parent = parent;
        this._templateName = name;
        this._templateDef = template_def;
        this._attributes = attributes;
        this._parsedTemplate = nodeTemplateDef;
        this._integratedTemplates = templates;
        heritParent();
    }

    private void heritParent(){
        if(this._parent != null){
            this._templateDef = this._parent.get_templateDef().concat(this._templateDef);
            this._parent.addSubTemplate(this);
        }
    }

    public Node get_parsedTemplate(){return this._parsedTemplate;}

    public String get_templateDef(){
        return this._templateDef;
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

    public List<Template> get_integratedTemplates(){
        return this._integratedTemplates;
    }

    public String get_templateName(){
        return this._templateName;
    }

    public Template getTemplate(
            String name){

        if(this._templateName != null && this._templateName.equals(name)){
            return this;
        }else{
            for(Template b_template : this._integratedTemplates){
                Template found = b_template.getTemplate(name);
                if(found != null){
                    return found;
                }
            }
        }

        return null;
    }

    public Object getValue(
            String name){

        if(_attributes != null && _attributes.containsKey(name)){
            return _attributes.get(name);
        }else{
            if(this._parent != null){
                return this._parent.getValue(name);
            }
        }

        return null;
    }

    public Boolean isVariableExist(
            String varName){

        if(_attributes != null && _attributes.containsKey(varName)){
            return true;
        }else{
            if(this._parent != null){
                return this._parent.isVariableExist(varName);
            }
        }

        return null;
    }

    private void addSubTemplate(
            Template template){

        if(this.subTypes.containsKey(template.get_templateName())){
            throw new InterpreterException("Cannot add another subTemplate of name + " + template.get_templateName(), null);
        }

        this.subTypes.put(template.get_templateName(), template);
    }
}
