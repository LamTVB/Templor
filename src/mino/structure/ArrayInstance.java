package mino.structure;

import java.util.List;

/**
 * Created by Lam on 24/03/2017.
 */
public class ArrayInstance
        extends Instance{

    private final List<Instance> value;

    public ArrayInstance(
            ClassInfo classInfo,
            List<Instance> value) {

        super(classInfo);
        this.value = value;
    }

    public List<Instance> getValue() {

        return this.value;
    }
}
