package org.emerjoin.hi.web.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mário Júnior
 */
public class LanguageBundle {

    private Map<String,Map<String,String>> dictionaries = new HashMap<>();
    private String lang=null;

    protected LanguageBundle(String lang){

        if(lang==null||lang.isEmpty())
            throw new IllegalArgumentException("Bundle lang must not be null nor empty");

        this.lang = lang;

    }


    protected void addDictionary(String name, Map<String,String> dictionary){

        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Dictionary's name must not be null nor empty");

        if(dictionary==null)
            throw new IllegalArgumentException("Dictionary must not be null");

        this.dictionaries.put(name,dictionary);

    }

    protected void addDictionaries(I18nMappingsInfo info,I18nConfiguration configuration){

        this.dictionaries.putAll(info.buildDictionaries(configuration));

    }

    public Map<String,String> getDictionary(String name){

        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Dictionary's name must not be null nor empty");

        return dictionaries.get(name);

    }

    public boolean hasDictionary(String name){
        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Dictionary's name must not be null empty");
        return dictionaries.containsKey(name);

    }


    public String translate(String string){
        if(string==null||string.isEmpty())
            throw new IllegalArgumentException("string to be translated must not be null nor empty");

        for(Map<String,String> d : dictionaries.values()){

            String translation = d.get(string);
            if(translation!=null)
                return translation;

        }

        return null;

    }


    public String getLang(){

        return lang;

    }

    public Map<String,String> export(){

        Map<String,String> dictionary = new HashMap<>();
        dictionaries.forEach((k,v) -> dictionary.putAll(v));
        return dictionary;

    }

    public String toString(){

        return dictionaries.toString();

    }


}
