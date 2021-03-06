package templor.structure;

import mino.language_mino.Node;
import templor.exception.TemplorException;
import templor.language_templor.NBlockdef;

import java.util.*;

/**
 * Created by Lam on 14/03/2017.
 */
public class Template {

    private Template _parent;

    private String _templateName;

    private String _templateDef;

    private Node _parsedTemplate;

    private Map<String, Object> _attributes = new LinkedHashMap<>();

    private Map<String, Template> extendedTemplates = new HashMap<>();

    private Map<String, Block> _blocks = new HashMap<>();

    public Template(
            Template parent,
            String name,
            String template_def,
            Map<String, Object> attributes,
            Node nodeTemplateDef,
            Map<String, Block> blocks){

        this._parent = parent;
        this._templateName = name;
        this._templateDef = template_def;
        this._attributes = attributes;
        this._parsedTemplate = nodeTemplateDef;
        this._blocks = blocks;
    }

    public Template(
            Template parent,
            String name){

        this._parent = parent;
        this._templateName = name;
    }

    public Node get_parsedTemplate(){return this._parsedTemplate;}

    public void set_parsedTemplate(
            Node parsedNode){

        this._parsedTemplate = parsedNode;
    }

    public String get_templateDef(){
        return this._templateDef;
    }

    public void setDefinition(
            String templateDef){

        this._templateDef = templateDef;
    }

    public Map<String, Object> get_attributes(){

        if(this._attributes != null){
            return this._attributes;
        }

        return null;
    }

    public void set_attributes(
            Map<String, Object> attributes){

        this._attributes = attributes;
    }

    public void addOrUpdateAttribute(
            String name,
            Object value){

        if(!this._attributes.containsKey(name)){
            throw new TemplorException("Attribute of name " + name + " does not exist in " + get_templateName(), null);
        }else if(this._attributes != null){
            this._attributes.put(name, value);
        }else{
            //TODO handle interpreterException to give node
            throw new TemplorException("There is no attribute for the template of name " + get_templateName(), null);
        }
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

    public String get_templateName(){
        return this._templateName;
    }

    private void addExtendedTemplate(
            Template template){

        if(this.extendedTemplates.containsKey(template.get_templateName())){
            throw new TemplorException("Cannot add another subTemplate of name + " + template.get_templateName(), null);
        }

        this.extendedTemplates.put(template.get_templateName(), template);
    }

    public Block getBlock(
            String blockName){

        if(!this._blocks.containsKey(blockName)){
            if(this._parent == null){
                throw new TemplorException("Cannot find block " + blockName + " in " + get_templateName(), null);
            }

            return this._parent.getBlock(blockName);
        }

        return this._blocks.get(blockName);
    }

    public void addBlock(
            Block newBlock,
            templor.language_templor.NId blockName){

        if(this._blocks.containsKey(blockName.getText())){
            throw new TemplorException("Block " + newBlock.get_blockName() + " has already been defined", blockName);
        }

        this._blocks.put(blockName.getText(), newBlock);
    }

    public Map<String, Block> get_blocks(){
       return this._blocks;
    }

    public void set_blocks(
            Map<String, Block> blocks){

        this._blocks = blocks;
    }

    public boolean hasBlock(
            String blockName){

        if(this._blocks.containsKey(blockName)){
            return true;
        }else{
            if(this._parent != null){
                return this._parent.hasBlock(blockName);
            }
        }

        return false;
    }

    public Template get_parent(){
        return this._parent;
    }
}
