package org.emerjoin.hi.web.events;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mário Júnior
 */
public class TemplateLoadEvent extends HiEvent {

    private Map<String,Object> values = new HashMap<>();

    public void set(String key, Object value){
        if(key==null||key.isEmpty())
            throw new IllegalArgumentException("Key instance must not be null nor empty");
        if(value==null)
            throw new IllegalArgumentException("Value instance must not be null");
        values.put(key,value);

    }


    public void set(Map<String,Object> map){
        if(map==null)
            throw new IllegalArgumentException("Map instance must not be null");
        values.putAll(map);

    }


    public  Map<String,Object> getValues(){

        return this.values;

    }


}
