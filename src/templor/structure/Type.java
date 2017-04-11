package templor.structure;

import templor.language_templor.NType;
import templor.language_templor.NType_Entity;
import templor.language_templor.Walker;

/**
 * Created by Lam on 11/04/2017.
 */
public enum Type {
    ENTITY;

    public static Type get(
            NType type) {

        class Result {

            Type type;
        }
        final Result result = new Result();

        type.apply(new Walker() {

            @Override
            public void caseType_Entity(NType_Entity node) {
                result.type = ENTITY;
            }
        });

        return result.type;
    }
}
