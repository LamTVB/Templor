package mino.structure;

import mino.exception.SemanticException;
import mino.language_mino.NId;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

/**
 * Created by Lam on 30/03/2017.
 */
public class Scope {

    private Scope parentScope;

    private MethodInfo currentMethod;

    private LinkedHashMap<String, VariableInfo> variables = new LinkedHashMap<>();

    public Scope(
            Scope parent) {
        this.parentScope = parent;
    }

    public Scope(
            Scope parent,
            MethodInfo currentMethod){

        this.parentScope = parent;
        this.currentMethod = currentMethod;
    }

    public Scope getParent() {

        return this.parentScope;
    }

    public void addVariable(
            VariableInfo variableInfo) {

        String name = variableInfo.getName();

        if (variableExists(name)) {
            throw new SemanticException(
                    "variable " + name + " is already declared",
                    variableInfo.getLocation());
        }

        this.variables.put(name, variableInfo);
    }

    private boolean variableExists(
            String name) {

        if (this.variables.containsKey(name)) {
            return true;
        }

        if (this.parentScope != null) {
            return this.parentScope.variableExists(name);
        }

        return false;
    }

    public VariableInfo getVariable(
            NId id) {

        String name = id.getText();
        if (!this.variables.containsKey(name)) {
            if (this.parentScope != null) {
                return this.parentScope.getVariable(id);
            }
            else {
                throw new SemanticException("undefined variable " + name, id);
            }
        }

        return this.variables.get(name);
    }

    public MethodInfo getCurrentMethod(){

        if(this.currentMethod == null){
            if(this.parentScope != null){
                return this.parentScope.getCurrentMethod();
            }
        }
        return this.currentMethod;
    }
}
