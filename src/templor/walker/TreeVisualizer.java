
package templor.walker;

import templor.language_templor.*;
import java.util.*;

public class TreeVisualizer
        extends Walker {

    private HashMap<Node, Integer> ids = new HashMap<Node, Integer>();

    private int nextID = 1;
    private Node parent;
    
    public static void printTree(
            Node tree) {

        System.out.println("graph G {");
        tree.apply(new TreeVisualizer());
        System.out.println("}");
    }

    @Override
    public void defaultCase(
            Node node) {

        Node oldParent = this.parent;
        this.parent = node;
        super.defaultCase(node);
        this.parent = oldParent;
    }
    
    @Override
    public void defaultIn(
            Node node) {

        if (node instanceof Token) {
            Token token = (Token) node;

            int id = nextID++;
            this.ids.put(token, id);
            System.out.println(" " + id + " [shape=rect,label=\""
                    + token.getType().name() + ": "
                    + token.getText().replaceAll("\"", "\\\\\"") + "\"];");

            System.out.println(" " + this.ids.get(parent) + " -- " + id + ";");
        }
        else {
            int id = nextID++;
            this.ids.put(node, id);
            System.out.println(" " + id + " [label=\""
                    + node.getType().name() + "\"];");

            if (parent != null) {
                System.out.println(
                        " " + this.ids.get(parent) + " -- " + id + ";");
            }
        }
    }
}
