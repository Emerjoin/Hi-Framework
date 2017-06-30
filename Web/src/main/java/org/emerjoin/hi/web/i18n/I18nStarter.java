package org.emerjoin.hi.web.i18n;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.jarex.Jarex;
import org.emerjoin.jarex.query.Queries;
import org.emerjoin.jarex.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mário Júnior
 */
public class I18nStarter {

    private I18nConfiguration configuration;
    private Optional<Collection<URL>> classpath;
    private ServletContext servletContext;
    private I18nCache i18nCache;

    private boolean ready = false;

    private Map<String,LanguageBundle> bundles = new HashMap<>();
    private static Logger LOG = LoggerFactory.getLogger(I18nStarter.class);

    public I18nStarter(I18nConfiguration configuration, Optional<Collection<URL>> classpath, ServletContext servletContext){

        if(configuration==null)
            throw new IllegalArgumentException("I18nConfiguration must not be null");

        if(servletContext==null)
            throw new IllegalArgumentException("ServletContext must not be null");

        this.configuration = configuration;
        this.classpath = classpath;

        this.servletContext = servletContext;

    }


    private String getDictionaryFilePath(String lang, String name){

        return String.format("/i18n/%s/%s.properties", lang, name);

    }

    private void loadDictionariesWithoutConcatenation(){
        for(String lang: configuration.getLanguages()){
            LanguageBundle bundle = new LanguageBundle(lang);

            for(String dict : configuration.getDictionaries()){
                String dictionaryPath = getDictionaryFilePath(lang,dict);
                LOG.debug(String.format("Loading language dictionary file : %s",dictionaryPath));

                try {

                    URL dictionaryResource = servletContext.getResource(dictionaryPath);
                    if (dictionaryResource == null) {
                        LOG.warn(String.format("Language dictionary file missing : %s", dictionaryPath));
                        bundle.addDictionary(dict, new HashMap<>());
                        continue;
                    }

                    DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
                    dictionaryBuilder.put(dictionaryResource.openStream(), configuration);
                    bundle.addDictionary(dict,dictionaryBuilder.build());

                }catch (IOException ex){

                    throw new I18nException(String.format("Failed to load language dictionary file : %s",dictionaryPath),ex);

                }
            }

            bundles.put(lang,bundle);
        }
    }


    private List<InputStream> findDictionariesInClasspath(String path){

        String pathInJar = "META-INF/resources"+path;
        LOG.debug(String.format("Searching for %s in classpath...",pathInJar));
        Query dictionaryFile = Queries.fileEntry(pathInJar);
        return Jarex.createInstance(classpath.get())
                .all(dictionaryFile)
                .withResults().of(dictionaryFile)
                .map((item -> item.getInputStream()))
                .collect(Collectors.toList());

    }

    private void loadDictionariesWithConcatenation(){

        LOG.info("Loading language dictionary files using CONCATENATION");
        if(!classpath.isPresent())
            throw new I18nException("Can't proceed with language dictionaries concatenation because no valid classpath was specified");

        for(String lang: configuration.getLanguages()){

            LanguageBundle bundle = new LanguageBundle(lang);

            for(String dict : configuration.getDictionaries()){
                String dictionaryPath = getDictionaryFilePath(lang,dict);
                LOG.debug(String.format("Loading language dictionary files under %s",dictionaryPath));

                try {

                    URL dictionaryResource = servletContext.getResource(dictionaryPath);
                    DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();

                    if(dictionaryResource!=null)
                        dictionaryBuilder.put(dictionaryResource.openStream(), configuration);

                    List<InputStream> otherDictionaryFiles = findDictionariesInClasspath(dictionaryPath);
                    otherDictionaryFiles.forEach(d -> dictionaryBuilder.put(d,configuration));

                    bundle.addDictionary(dict,dictionaryBuilder.build());

                }catch (IOException ex){

                    throw new I18nException(String.format("Failed to load language dictionary files under %s",dictionaryPath),ex);

                }
            }

            bundles.put(lang,bundle);

        }

    }


    private void setDefaultI18nCache(){

        i18nCache = new DefaultI18nCache();

    }

    public void start(){

        if(!configuration.isConcatenateDictionaries()){
            loadDictionariesWithoutConcatenation();
        }else loadDictionariesWithConcatenation();


        if(configuration.getCacheClassName().isPresent()){

            String cacheType = configuration.getCacheClassName().get();

            try {

                Class clazz = Class.forName(cacheType);
                Object cacheInstance = clazz.newInstance();
                if (!(cacheInstance instanceof I18nCache))
                    throw new I18nException(String.format("Cache classname is not a valid subclass of I18nCache : %s", cacheType));

            }catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex){

                throw new I18nException("Failed to instantiate I18n Cache type",ex);

            }

        }else setDefaultI18nCache();

        this.ready = true;
        I18nRuntime.init(this, AppConfigurations.get());

    }


    private void checkInitialized(){
        if(!ready)
            start();
    }

    public I18nConfiguration getConfiguration(){

        return configuration;

    }

    public Map<String,LanguageBundle> getBundles(){
        checkInitialized();
        return bundles;

    }

    public I18nCache getI18nCache(){
        checkInitialized();
        return i18nCache;

    }


}
