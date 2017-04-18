package templor.walker;

import mino.language_mino.LexerException;
import mino.language_mino.NStm_Block;
import mino.language_mino.NTerm_Block;
import mino.language_mino.ParserException;

import mino.walker.TemplorFinder;
import templor.exception.TemplorException;
import templor.language_templor.*;
import templor.language_templor.Node;
import templor.language_templor.Walker;
import templor.structure.Block;
import templor.structure.Template;

import java.io.*;
import java.util.*;

/**
 * Created by Lam on 24/02/2017.
 */
public class TemplatesFactory
        extends Walker {

    private Map<String, Template> templatesMap = new HashMap<>();

    //TODO instead of string create an object Template
    private String templateDef;
    private Template currentParentTemplate;
    private Map<String, Block> currentBlocks;
    private Map<String, Object> templateAttributes = new HashMap<>();

    public void visit(Node node){
        node.apply(this);
    }

    private String getTemplateDef(
            Node node){

        visit(node);
        String template = this.templateDef;
        this.templateDef = null;
        return template;
    }

    private Map<String, Block> getBlocks(
            Node node){

        this.currentBlocks = new LinkedHashMap<>();
        visit(node);
        Map<String, Block> blocks = this.currentBlocks;
        this.currentBlocks = null;
        return blocks;
    }

    public Map<String, Template> getTemplatesMap(){
        return this.templatesMap;
    }

    public Template createAnonymousTemplate(
            NTemplateDef node){

        String stringTemplateDef = node.getText().replaceAll("<\\{"," ").replaceAll("}>", " ");
        StringReader reader = new StringReader(stringTemplateDef);
        mino.language_mino.Node syntaxTree = null;

        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            TemplorFinder engine = new TemplorFinder(null);
            engine.visit(syntaxTree);
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
            template = new Template(null,null, stringTemplateDef,
                    null, syntaxTree, null);
        }

        return template;

    }


    private Map<String, Object> getAttributes(
            Node node){

        this.templateAttributes = new LinkedHashMap<>();

        visit(node);
        Map<String, Object> attributes = this.templateAttributes;
        this.templateAttributes = null;
        return attributes;
    }

    //Function to retrieve template from file_path
//    private Reader getTemplateReader(
//            String templateDef)
//            throws IOException {
//
//        return getTemplateReader(templateDef, null);
//    }
//
//    private Reader getTemplateReader(
//            String templateDefinition,
//            String parentDef)
//            throws IOException {
//
//        Reader reader;
//        BufferedReader br = null;
//
//        if(!templateDefinition.contains("<{") && !templateDefinition.contains("}>")){
//            reader = new FileReader(templateDefinition.replaceAll("\"", ""));
//            br = new BufferedReader(reader);
//            StringBuilder sb = new StringBuilder();
//            String line = br.readLine();
//
//            while(line != null){
//                sb.append(line);
//                line = br.readLine();
//            }
//            templateDefinition = sb.toString();
//
//            if(parentDef != null){
//                templateDefinition.concat(parentDef.replaceAll("<\\{"," ").replaceAll("}>", " "));
//            }
//        }else{
//            templateDefinition = templateDefinition.replaceAll("<\\{"," ").replaceAll("}>", " ");
//        }
//
//        reader = new StringReader(templateDefinition);
//        return reader;
//    }

    private String formatTemplateDef(
            String templateDef){

        return templateDef.replaceAll("<\\{"," ").replaceAll("}>", " ");
    }

    private mino.language_mino.Node parseTree(
            String templateDef,
            NId location){

        Reader reader = new StringReader(templateDef);

        mino.language_mino.Node syntaxTree = null;
        try {
            syntaxTree = new mino.language_mino.Parser(reader).parse();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + " on definition of template : "
                    + location.getText() + " while initializing template at line "
                    + location.getLine() + " pos " + location.getPos());
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + " on definition of template : "
                    + location.getText() + " while initializing template at line "
                    + location.getLine() + " pos " + location.getPos());
            System.exit(1);
        }

        return syntaxTree;
    }

    @Override
    public void caseStm_Create(
            NStm_Create node) {

        Template parentTemplate = null;

        if(node.get_SpecialOpt() instanceof NSpecialOpt_One){
            String specialTemplateName = ((NSpecialOpt_One)node.get_SpecialOpt()).get_Special().get_TemplateName().getText();
            if(!this.templatesMap.containsKey(specialTemplateName)){
                throw new TemplorException("Template of name : " + specialTemplateName, node.get_TemplateName());
            }
            this.currentParentTemplate = parentTemplate = this.templatesMap.get(specialTemplateName);
        }

        String templateDef = getTemplateDef(node.get_Members());
        Map<String, Object> currentAttributes = getAttributes(node.get_Members());
        Map<String, Block> blocks = getBlocks(node.get_Members());

        if(templateDef == null){
            //no definition
            if(parentTemplate == null){
                throw new TemplorException("Template must be defined", node.get_TemplateName());
            }else{
                //herit from parent
                templateDef = parentTemplate.get_templateDef();
            }
        }
        Reader reader = new StringReader(templateDef);

        mino.language_mino.Node syntaxTree = null;
        try {
            //TODO change name TemplorFinder == attributesVerifier
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            TemplorFinder engine = new TemplorFinder(currentAttributes);
            engine.visit(syntaxTree);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + " on definition of templateDef : "
                    + node.get_TemplateName().getText() + " while initializing template");
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + " on definition of templateDef : "
                    + node.get_TemplateName().getText() + " while initializing template");
            System.exit(1);
        }

        if(syntaxTree != null){
            Template template = new Template(parentTemplate, node.get_TemplateName().getText(), templateDef,
                    currentAttributes, syntaxTree, blocks);

            this.templatesMap.put(node.get_TemplateName().getText(), template);
        }
    }

    //Attributes
    @Override
    public void caseMember_Attributes(
            NMember_Attributes node) {

        this.templateAttributes = getAttributes(node.get_TemplateAttributes());
        if (this.templateAttributes != null
                && this.templateAttributes.size() == 0
                && this.currentParentTemplate != null) {

            //Attributes not used
            this.templateAttributes = this.currentParentTemplate.get_attributes();
        }else if(this.templateAttributes == null){

            this.templateAttributes = new LinkedHashMap<>();
        }
    }

    @Override
    public void caseTemplateAttributes_More(
            NTemplateAttributes_More node) {

        if(this.currentParentTemplate == null){
            throw new TemplorException("Parent cannot be found", node.get_SuperKw());
        }

        Map<String, Object> attributes = this.currentParentTemplate.get_attributes();
        attributes.putAll(getAttributes(node.get_ParametersList()));
        this.templateAttributes = attributes;
    }

    @Override
    public void caseTemplateAttributes_Simple(
            NTemplateAttributes_Simple node) {

        this.templateAttributes = getAttributes(node.get_ParametersList());
    }

    @Override
    public void caseTemplateAttributes_Super(
            NTemplateAttributes_Super node) {

        if(this.currentParentTemplate == null){
            throw new TemplorException("Parent cannot be found", node.get_SuperKw());
        }

        this.templateAttributes = this.currentParentTemplate.get_attributes();
    }

    @Override
    public void caseTemplateAttributes_Zero(
            NTemplateAttributes_Zero node) {

        this.templateAttributes = null;
    }

    //Template definition
    @Override
    public void caseMember_Definition(
            NMember_Definition node) {

        this.templateDef = getTemplateDef(node.get_TemplateDefinition());
    }

    @Override
    public void caseTemplateDefinition_More(
            NTemplateDefinition_More node) {

        if(this.currentParentTemplate == null){
            throw new TemplorException("Parent cannot be found", node.get_SuperKw());
        }

        String templateDefinition = this.currentParentTemplate.get_templateDef();
        this.templateDef = formatTemplateDef(templateDefinition.concat(node.get_TemplateDef().getText()));
    }

    @Override
    public void caseTemplateDefinition_Simple(
            NTemplateDefinition_Simple node) {

        this.templateDef = formatTemplateDef(node.get_TemplateDef().getText());
    }

    @Override
    public void caseTemplateDefinition_Super(
            NTemplateDefinition_Super node) {

        if(this.currentParentTemplate == null){
            throw new TemplorException("Parent cannot be found", node.get_SuperKw());
        }

        this.templateDef = this.currentParentTemplate.get_templateDef();
    }



        }
        }


        }


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

        if(this.templatesMap.containsKey(template_name)){
            this.templateDef = this.templatesMap.get(template_name).get_templateDef();
        }else{
            throw new TemplorException("Template of name " + template_name + " is unknown", node.get_Id());
        }
    }

    @Override
    public void caseParameter_Name(
            NParameter_Name node) {

        this.templateAttributes.put(node.get_Id().getText(),null);
    }

    @Override
    public void caseTemplate_FilePath(
            NTemplate_FilePath node) {

        this.templateDef = node.get_String().getText();
    }
}
