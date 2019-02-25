package org.emerjoin.hi.web.frontier.model;

/**
 * Created by Mario Junior.
 */
public class MethodParam {

    private String name;
    private Class type;
    private boolean nullable;

    public MethodParam(String name, Class type, boolean nullable){
        this.name = name;
        this.type = type;
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}
