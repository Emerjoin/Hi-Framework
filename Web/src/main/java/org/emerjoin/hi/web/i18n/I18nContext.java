package org.emerjoin.hi.web.i18n;

import javax.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mário Júnior
 */
@RequestScoped
public class I18nContext {

    private Map<String,String> exported = new HashMap<>();

    private void validateTerm(String term){
        if(term==null||term.isEmpty())
            throw new IllegalArgumentException("Dictionary term must not be null nor empty");

    }

    public void export(String term){
        validateTerm(term);
        exported.put(term,I18nRuntime.get().translate(term));
    }

    public void export(String term, String alias){
        validateTerm(term);
        if(alias==null||alias.isEmpty())
            throw new IllegalArgumentException("Term alias must not be null nor empty");

        String value = I18nRuntime.get().translate(term);
        if(value!=null)
            exported.put(alias,value);

    }

    public void exportDictionary(String name){
        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Dictionary name must not be null nor empty");
        Map<String,String> dic = I18nRuntime.get().getDictionary(name);
        exported.putAll(dic);

    }

    public Map<String,String> collect(){

        return exported;

    }

    public boolean isLanguageKnown(String name){

         return I18nRuntime.isReady() && Arrays.asList(I18nRuntime.get()
                 .getConfiguration().getLanguages()).indexOf(name)>-1;

    }

}
