package templor.walker;

import mino.language_mino.LexerException;
import mino.language_mino.ParserException;

import mino.walker.TemplorFinder;
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

    private String templateDef;

    public void visit(Node node){
        node.apply(this);
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        String templateDef = getTemplateDef(node.get_AddTemplate());
        Map<String, Object> currentAttributes = getAttributes(node.get_ParametersListOpt());
        List<String> templateNames = new ArrayList<>();
        Template superTemplate = null;

        if(node.get_SpecialOpt() instanceof NSpecialOpt_One){
            String specialTemplateName = ((NSpecialOpt_One)node.get_SpecialOpt()).get_Special().get_TemplateName().getText();
            if(!this.templates.containsKey(specialTemplateName)){
                throw new InterpreterException("Template of name : " + specialTemplateName, node.get_TemplateName());
            }
            superTemplate = this.templates.get(specialTemplateName);
        }

        if(templateDef == null){
            throw new InterpreterException("Template cannot be null", node.get_TemplateName());
        }
        Reader reader = null;

        try {
            if(superTemplate != null){
                reader = getTemplateReader(templateDef, superTemplate.get_templateDef());
            }else{
                reader = getTemplateReader(templateDef);
            }
        }
        catch (IOException e) {
            throw new InterpreterException(e.getMessage(), node.get_TemplateName());
        }

        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            TemplorFinder engine = new TemplorFinder(currentAttributes,superTemplate);
            engine.visit(syntaxTree);
            templateNames = engine.getDependentTemplates();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + " on definition of templateDef : "
                    + node.get_TemplateName().getText() + " while initializing all templates");
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + " on definition of templateDef : "
                    + node.get_TemplateName().getText() + " while initializing all templates");
            System.exit(1);
        }



        if(syntaxTree != null){
            List<Template> integratedTemplates = new ArrayList<>();

            for(String templateName : templateNames){
                integratedTemplates.add(getTemplateByName(templateName));
            }

            Template template = new Template(superTemplate, node.get_TemplateName().getText(), templateDef,
                    currentAttributes, syntaxTree, integratedTemplates);

            this.templates.put(node.get_TemplateName().getText(), template);
        }
    }

    @Override
    public void caseTemplate_TemplateDef(
            NTemplate_TemplateDef node) {

        this.templateDef = node.get_TemplateDef().getText();
    }

    @Override
    public void caseTemplate_TemplateName(
            NTemplate_TemplateName node) {

        String template_name = node.get_Id().getText();

        if(this.templates.containsKey(template_name)){
            this.templateDef = this.templates.get(template_name).get_templateDef();
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

        this.templateDef = node.get_String().getText();
    }

    private Map<String, Object> getAttributes(
            Node node){

        this.templateAttributes = new HashMap<>();

        visit(node);
        Map<String, Object> attributes = this.templateAttributes;
        this.templateAttributes = null;
        return attributes;
    }

    private String getTemplateDef(
            Node node){

        visit(node);
        String template = this.templateDef;
        this.templateDef = null;
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
            TemplorFinder engine = new TemplorFinder(null, null);
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
            template = new Template(null,null, stringTemplateDef,
                    null, syntaxTree, integratedTemplates);
        }

        return template;

    }

    private Reader getTemplateReader(
            String templateDef)
            throws IOException {

        return getTemplateReader(templateDef, null);
    }

    private Reader getTemplateReader(
            String templateDefinition,
            String parentDef)
            throws IOException {

        Reader reader;
        BufferedReader br = null;

        if(!templateDefinition.contains("<{") && !templateDefinition.contains("}>")){
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

        if(parentDef != null){
            templateDefinition.concat(parentDef.replaceAll("<\\{"," ").replaceAll("}>", " "));
        }

        reader = new StringReader(templateDefinition);
        return reader;
    }
}
