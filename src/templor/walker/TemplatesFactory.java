package templor.walker;

import mino.language_mino.LexerException;
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
    private Map<String, Object> templateAttributes = new HashMap<>();
    private Template currentTemplate;

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
            Token location){

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

    private Template buildTemplate(
            NStm_Definition node){

        Template parent = getParent(node.get_SpecialOpt());
        return new Template(parent, node.get_TemplateName().getText());
    }

    private Template getParent(
            NSpecialOpt node){

        visit(node);
        Template parent = this.currentTemplate;
        this.currentTemplate = null;
        return parent;
    }

    @Override
    public void inStm_Definition(
            NStm_Definition node) {

        this.currentTemplate = buildTemplate(node);
    }

    @Override
    public void outStm_Definition(
            NStm_Definition node) {

        this.currentTemplate = null;
    }

    @Override
    public void caseStm_Definition(
            NStm_Definition node) {

        //collecting members
        visit(node.get_Members());

        Reader reader = new StringReader(this.currentTemplate.get_templateDef());

        mino.language_mino.Node syntaxTree = null;
        try {
            //TODO change name TemplorFinder == attributesVerifier
            syntaxTree = new mino.language_mino.Parser(reader).parse();
            TemplorFinder engine = new TemplorFinder(this.currentTemplate.get_attributes());
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

            this.templatesMap.put(node.get_TemplateName().getText(), this.currentTemplate);
        }
    }

    @Override
    public void caseSpecialOpt_One(
            NSpecialOpt_One node) {

        String specialTemplateName = node.get_Special().get_TemplateName().getText();
        if(!this.templatesMap.containsKey(specialTemplateName)){
            throw new TemplorException("Template of name : " + specialTemplateName + " is not defined.", node.get_Special().get_TemplateName());
        }

        this.currentTemplate = this.templatesMap.get(specialTemplateName);
    }

    //Attributes
    @Override
    public void caseMember_Attributes(
            NMember_Attributes node) {

        visit(node.get_TemplateAttributes());
    }

    @Override
    public void caseTemplateAttributes_More(
            NTemplateAttributes_More node) {

        Template parent = this.currentTemplate.get_parent();
        if(parent == null){
            throw new TemplorException("Undefined parent : inappropriate use of 'super' for template "
                    + this.currentTemplate.get_templateName(), node.get_SuperKw());
        }

        Map<String, Object> attributes = parent.get_attributes();
        attributes.putAll(getAttributes(node.get_ParametersList()));
        this.currentTemplate.set_attributes(attributes);
    }

    @Override
    public void caseTemplateAttributes_Simple(
            NTemplateAttributes_Simple node) {

        Map<String, Object> attributes = getAttributes(node.get_ParametersList());
        this.currentTemplate.set_attributes(attributes);
    }

    @Override
    public void caseTemplateAttributes_Super(
            NTemplateAttributes_Super node) {

        Template parent = this.currentTemplate.get_parent();
        if(parent == null){
            throw new TemplorException("Parent is not defined for template " + this.currentTemplate.get_templateName(), node.get_SuperKw());
        }

        this.currentTemplate.set_attributes(parent.get_attributes());
    }

    @Override
    public void caseTemplateAttributes_Zero(
            NTemplateAttributes_Zero node) {

        this.currentTemplate.set_attributes(new HashMap<>());
    }

    //Template definition
    @Override
    public void caseMember_Definition(
            NMember_Definition node) {

        String templateDef = getTemplateDef(node.get_TemplateDefinition());
        Template parent = this.currentTemplate.get_parent();
        if(templateDef == null){
            //no definition
            if(parent == null){
                throw new TemplorException("Template must be defined", node.get_Eq());
            }else{
                //herit from parent
                templateDef = parent.get_templateDef();
            }
        }

        this.currentTemplate.setDefinition(templateDef);
        this.currentTemplate.set_parsedTemplate(parseTree(templateDef, node.get_Eq()));
    }

    @Override
    public void caseTemplateDefinition_More(
            NTemplateDefinition_More node) {

        Template parent = this.currentTemplate.get_parent();

        if(parent == null){
            throw new TemplorException("Parent is undefined for template "
                    + this.currentTemplate.get_templateName(), node.get_SuperKw());
        }

        String parentDefinition = parent.get_templateDef();
        String templateDefinition = formatTemplateDef(node.get_TemplateDef().getText());
        this.templateDef = formatTemplateDef(parentDefinition.concat(templateDefinition));
    }

    @Override
    public void caseTemplateDefinition_Simple(
            NTemplateDefinition_Simple node) {

        this.templateDef = formatTemplateDef(node.get_TemplateDef().getText());
    }

    @Override
    public void caseTemplateDefinition_Super(
            NTemplateDefinition_Super node) {

        Template parent = this.currentTemplate.get_parent();
        if(parent == null){
            throw new TemplorException("Undefined parent : inapropriate use of 'super' for template " + this.currentTemplate.get_templateName(), node.get_SuperKw());
        }

        this.templateDef = parent.get_templateDef();
    }

    //Blocks
    @Override
    public void caseMember_Blocks(
            NMember_Blocks node) {

        visit(node.get_BlocksList());
    }

    @Override
    public void caseBlockdef_Append(
            NBlockdef_Append node) {

        Template parentTemplate = this.currentTemplate.get_parent();
        if(parentTemplate == null){
            throw new TemplorException("Undefined parent for template "
                    + this.currentTemplate.get_templateName(), node.get_Eq());
        }

        Block parentBlock = parentTemplate.getBlock(node.get_BlockName().getText());

        String templateDef = formatTemplateDef(node.get_TemplateDef().getText());
        String parentBlockDefinition = parentBlock.get_definition();
        String blockDefinition;

        if(node.get_PosOpt() instanceof NPosOpt_One){

            //Append text at a specific position
            String nameTemplateExtend = ((NPosOpt_One)node.get_PosOpt()).get_Id().getText();
            parentBlockDefinition = parentBlockDefinition.replace("[" + nameTemplateExtend + "]", templateDef);
            blockDefinition = formatTemplateDef(parentBlockDefinition);
        }else{
            //no position then concat parent and son
            blockDefinition = formatTemplateDef(parentBlockDefinition.concat(templateDef));
        }

        mino.language_mino.Node parsedBlock = parseTree(blockDefinition, node.get_BlockName());

        Block newBlock = new Block(parentBlock.get_blockName(), blockDefinition, parentBlock, parsedBlock);
        this.currentTemplate.addBlock(newBlock, node.get_BlockName());
    }

    @Override
    public void caseBlockdef_Prepend(
            NBlockdef_Prepend node) {

        Template parentTemplate = this.currentTemplate.get_parent();
        if(parentTemplate == null){
            //get currentTemplateName would be better
            throw new TemplorException("Undefined parent for template "
                    + this.currentTemplate.get_templateName(), node.get_Eq());
        }

        Block parentBlock = parentTemplate.getBlock(node.get_BlockName().getText());

        String templateDef = node.get_TemplateDef().getText();
        String blockDefinition = formatTemplateDef(templateDef.concat(parentBlock.get_definition()));
        mino.language_mino.Node parsedBlock = parseTree(blockDefinition, node.get_BlockName());

        Block newBlock = new Block(parentBlock.get_blockName(), blockDefinition, parentBlock, parsedBlock);
        this.currentTemplate.addBlock(newBlock, node.get_BlockName());
    }

    @Override
    public void caseBlockdef_Definition(
            NBlockdef_Definition node) {

        String blockDefinition = formatTemplateDef(node.get_TemplateDef().getText());
        mino.language_mino.Node parsedBlock = parseTree(blockDefinition, node.get_BlockName());

        Block newBlock = new Block(node.get_BlockName().getText(), blockDefinition, null, parsedBlock);
        this.currentTemplate.addBlock(newBlock,node.get_BlockName());
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
