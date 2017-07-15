package org.emerjoin.hi.web.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Mário Júnior
 */
public class I18nMappingsInfo {

    private Map<String,List<URL>> templates = new HashMap<>();
    private Map<String,List<URL>> views = new HashMap<>();
    private static Logger LOG = LoggerFactory.getLogger(I18nMappingsInfo.class);


    public synchronized void addViewMapping(String path, URL resource){
        if(path==null||path.isEmpty())
            throw new IllegalArgumentException("View path must not be null nor empty");
        if(resource==null)
            throw new IllegalArgumentException("Dictionary resource must not be null");
        List<URL> mappings = new ArrayList<>();
        if(views.containsKey(path))
            mappings = views.get(path);

        mappings.add(resource);
        views.put(path,mappings);
    }


    public synchronized void addTemplateMapping(String name, URL resource){
        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Template name must not be null nor empty");
        if(resource==null)
            throw new IllegalArgumentException("Dictionary resource must not be null");
        List<URL> mappings = new ArrayList<>();
        if(templates.containsKey(name))
            mappings = templates.get(name);

        mappings.add(resource);
        templates.put(name,mappings);

    }



    public Map<String,Map<String,String>> buildDictionaries(I18nConfiguration configuration){

        Map<String,Map<String,String>> allDictionaries = new HashMap<>();
        templates.forEach((k,v) -> allDictionaries.put(k,buildDictionary(v,configuration)));
        views.forEach((k,v) -> allDictionaries.put(k,buildDictionary(v,configuration)));
        return allDictionaries;

    }

    private Map<String,String> buildDictionary(List<URL> files, I18nConfiguration configuration){

        DictionaryBuilder builder = new DictionaryBuilder();
        files.forEach(d -> {

            try {

                builder.put(d.openStream(), configuration);

            }catch (IOException ex){

                LOG.error(String.format("Failed to read language dictionary file resource : %s",d.toString()),ex);

            }

        });

        return builder.build();

    }

}
