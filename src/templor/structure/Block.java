package templor.structure;

import mino.language_mino.Node;

/**
 * Created by Lam on 17/04/2017.
 */
public class Block {

    private final String _blockName;

    private final String _definition;

    private final Block _parent;

    private final Node _parsedBlock;

    public Block(
            String name,
            String definition,
            Block parent,
            Node parsedBlock){

        this._blockName = name;
        this._definition = definition;
        this._parent = parent;
        this._parsedBlock = parsedBlock;
    }

    public String get_blockName() {

        return _blockName;
    }

    public String get_definition() {

        return _definition;
    }

    public Block get_parent() {

        return _parent;
    }

    public Node get_parsedBlock() {

        return _parsedBlock;
    }
}
