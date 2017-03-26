package templor.walker;

import mino.language_mino.LexerException;
import mino.language_mino.ParserException;

import mino.walker.AttributeFinder;
import templor.exception.InterpreterException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;
import templor.structure.Template;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lam on 24/02/2017.
 */
public class TemplatesFactory
        extends Walker {

    private Map<String, Template> templates = new HashMap<>();
    private Map<String, Object> templateAttributes = new HashMap<>();

    private Node template;

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        Node templateDef = getTemplateDef(node.get_AddTemplate());
        Map<String, Object> currentAttributes = getAttributes(node.get_ParametersListOpt());
        List<String> templateNames = new ArrayList<>();

        if(templateDef == null){
            throw new InterpreterException("Template cannot be null", node.get_TemplateName());
        }
        String stringTemplateDef = templateDef.getText();
        Reader reader = null;

        try {
            reader = getTemplateReader(stringTemplateDef);
        }
        catch (IOException e) {
            throw new InterpreterException(e.getMessage(), node.get_TemplateName());
        }

        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            AttributeFinder engine = new AttributeFinder(currentAttributes);
            engine.visit(syntaxTree);
            templateNames = engine.getDependentTemplates();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + " on definition of template :" + node.get_TemplateName().getText());
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + " on definition of template :" + node.get_TemplateName().getText());
            System.exit(1);
        }



        if(syntaxTree != null){
            List<Template> integratedTemplates = new ArrayList<>();

            for(String templateName : templateNames){
                integratedTemplates.add(getTemplateByName(templateName));
            }

            Template template = new Template(node.get_TemplateName().getText(), stringTemplateDef,
                    currentAttributes, templateDef, integratedTemplates);
            this.templates.put(node.get_TemplateName().getText(), template);
        }
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        this.template = node.get_TemplateDef();
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            this.template = this.templates.get(template_name).get_nodeTemplate();
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", node.get_Id());
        }
    }

    @Override
    public void caseParameter_Name(
            NParameter_Name node) {

        if(this.templateAttributes != null){
            this.templateAttributes.put(node.get_Id().getText(),null);
        }
    }

    @Override
    public void caseTemplate_FilePath(
            NTemplate_FilePath node) {

        this.template = node.get_String();
    }

    private Map<String, Object> getAttributes(
            Node node){

        this.templateAttributes = new HashMap<>();

        visit(node);
        Map<String, Object> attributes = this.templateAttributes;
        this.templateAttributes = null;
        return attributes;
    }

    private Node getTemplateDef(
            Node node){

        visit(node);
        Node template = this.template;
        this.template = null;
        return template;
    }

    public Map<String, Template> getTemplates(){
        return this.templates;
    }

    public Template getTemplateByName(
            String template_name){

        if(this.templates.containsKey(template_name)){
            return this.templates.get(template_name);
        }else{
            throw new InterpreterException("Template of name " + template_name + " is unknown", null);
        }
    }

    public Template createAnonymousTemplate(NTemplateDef node){
        List<String> templateNames = new ArrayList<>();

        String stringTemplateDef = node.getText().replaceAll("<\\{"," ").replaceAll("}>", " ");
        StringReader reader = new StringReader(stringTemplateDef);
        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            AttributeFinder engine = new AttributeFinder(null);
            engine.visit(syntaxTree);
            templateNames = engine.getDependentTemplates();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + ".");
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + ".");
            System.exit(1);
        }

        Template template = null;

        if(syntaxTree != null){
            List<Template> integratedTemplates = new ArrayList<>();

            for(String templateName : templateNames){
                integratedTemplates.add(getTemplateByName(templateName));
            }
            template = new Template(null, stringTemplateDef,
                    null, node, integratedTemplates);
        }

        return template;

    }

    public Reader getTemplateReader(
            String templateDefinition)
            throws IOException {

        Reader reader;
        BufferedReader br = null;

        if(!templateDefinition.contains("<{")){
            reader = new FileReader(templateDefinition.replaceAll("\"", ""));
            br = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while(line != null){
                sb.append(line);
                line = br.readLine();
            }
            templateDefinition = sb.toString();
        }else{
            templateDefinition = templateDefinition.replaceAll("<\\{"," ").replaceAll("}>", " ");
        }

        reader = new StringReader(templateDefinition);
        return reader;
    }
}
