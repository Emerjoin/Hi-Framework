package org.emerjoin.hi.web.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario Junior.
 */
public class JSCommandSchedule {

    private String name;
    private Map<String,Object> parameters = new HashMap<>();

    public JSCommandSchedule(String name){
        this(name, Collections.emptyMap());
    }

    public JSCommandSchedule(String name, Map<String,Object> parameters){
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
