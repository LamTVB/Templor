package mino.structure;

import mino.language_mino.NClassdef;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lam on 24/03/2017.
 */
public class ArrayClassInfo
        extends ClassInfo {

    public ArrayClassInfo(
            ClassTable classTable,
            NClassdef definition) {

        super(classTable, definition);
    }

    @Override
    public Instance newInstance() {
        throw new RuntimeException("invalid instance creation");
    }

    public Instance newArray(
            List<Instance> values){

        Instance instance = new ArrayInstance(this, values);
        return instance;
    }
}
