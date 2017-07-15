package org.emerjoin.hi.web.i18n;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.jarex.Jarex;
import org.emerjoin.jarex.ResultsWrapper;
import org.emerjoin.jarex.query.Queries;
import org.emerjoin.jarex.query.Query;
import org.emerjoin.xmleasy.XMLEasy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mário Júnior
 */
public class I18nStarter {

    private I18nConfiguration configuration;
    private Optional<Collection<URL>> classpath;
    private ServletContext servletContext;
    private I18nCache i18nCache;

    private URL MAPPINGS_XSD = null;

    private boolean ready = false;

    private Map<String,LanguageBundle> bundles = new HashMap<>();
    private static Logger LOG = LoggerFactory.getLogger(I18nStarter.class);

    private static final String I18N_MAPPINGS_FILE = "/WEB-INF/i18n-mappings.xml";
    private static final String TEMPLATES_MAPPINGS_TAG  = "templates-mappings";
    private static final String VIEWS_MAPPINGS_TAG  = "views-mappings";

    public I18nStarter(I18nConfiguration configuration, Collection<URL> classpath, ServletContext servletContext){

        if(configuration==null)
            throw new IllegalArgumentException("I18nConfiguration must not be null");

        if(servletContext==null)
            throw new IllegalArgumentException("ServletContext must not be null");

        this.configuration = configuration;
        this.classpath = Optional.ofNullable(classpath);
        this.servletContext = servletContext;

        try {

            this.MAPPINGS_XSD = servletContext.getResource("/i18n-mappings.xsd");

        }catch (IOException ex){

            throw new I18nException("Failed to load the i18n-mappings.xsd resource",ex);

        }

    }


    private String getDictionaryFilePath(String lang, String name){

        return String.format("/i18n/%s/%s.properties", lang, name);

    }


    private List<URL> getMappedDictionaries(Stream<Element> xmlElements,String lang){

        List<URL> dics =  xmlElements.parallel().map(element -> {
            String URI = element.getTextContent();
            String path = String.format("/i18n/%s/%s", lang, URI);
            LOG.debug(String.format("Dictionary File : [%s]",path));

            URL resource = null;
            try {

                resource = servletContext.getResource(path);
                if (resource == null)
                    LOG.warn(String.format("Dictionary file resource not found : %s", path));

            }catch (MalformedURLException ex){

                LOG.warn(String.format("Invalid dictionary file path supplied : %s",URI));

            }

            if(resource!=null)
                LOG.debug(String.format("Dictionary File Resource : [%s]",resource.toString()));
            return resource;

        }).filter(url -> url!=null).collect(Collectors.toList());

        LOG.debug(String.format("%d dictionary file(s) mapped",dics.size()));
        return dics;

    }

    private I18nMappingsInfo loadMappings(URL mappingsXML, String lang){

        I18nMappingsInfo i18nMappingsInfo = new I18nMappingsInfo();

        XMLEasy xmlEasy = new XMLEasy(mappingsXML).validate(MAPPINGS_XSD).freeze();
        xmlEasy.ifChild(TEMPLATES_MAPPINGS_TAG).then(templates -> {
            LOG.info("Loading templates i18n mappings...");
            //Load templates mappings
            templates.streamChildren().forEach((template -> {
                String templateName = template.getAttribute("for");
                LOG.info(String.format("Loading I18n mappings for [%s]",templateName));
                XMLEasy xml = XMLEasy.easy(template);
                getMappedDictionaries(xml.streamChildren(),lang)
                        .forEach(url -> i18nMappingsInfo.addTemplateMapping(templateName,url));

            }));
        }).otherwise(()->LOG.info("No templates i18n mappings found!")).eval();

        xmlEasy.ifChild(VIEWS_MAPPINGS_TAG).then(views -> {
            LOG.info("Loading views i18n mappings...");
            //Load views mappings
            views.streamChildren().forEach(view -> {
                String path = view.getAttribute("for");
                LOG.info(String.format("Loading I18n mappings for [%s]",path));
                XMLEasy xml = XMLEasy.easy(view);
                getMappedDictionaries(xml.streamChildren(),lang)
                        .forEach(url -> i18nMappingsInfo.addViewMapping(path,url));

            });
        }).otherwise(()->LOG.info("No views i18n mappings found!")).eval();

        return i18nMappingsInfo;

    }

    private void loadDictionariesWithoutConcatenation(){

        URL mappingsXML = null;
        if(configuration.isMappingsEnabled()){

            try {

                mappingsXML = servletContext.getResource(I18N_MAPPINGS_FILE);
                if(mappingsXML==null)
                    LOG.warn(String.format("I18n Mappings config not found : %s",I18N_MAPPINGS_FILE));

            }catch (MalformedURLException ex){

                LOG.error(String.format("Invalid I18n Mappings configuration file Path : %s",I18N_MAPPINGS_FILE));

            }

        }

        for(String lang: configuration.getLanguages()){
            LanguageBundle bundle = new LanguageBundle(lang);

            if(configuration.isMappingsEnabled()&&mappingsXML!=null) {
                LOG.info("Loading I18n Mapped dictionary files : %s",lang);
                I18nMappingsInfo mappings = loadMappings(mappingsXML, lang);
                bundle.addDictionaries(mappings,configuration);
            }


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


    private List<URL> getI18nMappingConfigs(){

        List<URL> mappings = new ArrayList<>();

        if(configuration.isMappingsEnabled()) {
            try {

                URL rootI18nMapping = servletContext.getResource(I18N_MAPPINGS_FILE);
                if (rootI18nMapping != null){

                    //It's not inside a jar file
                    if(rootI18nMapping.toExternalForm().indexOf('!')==-1)
                        mappings.add(rootI18nMapping);
                }

            } catch (MalformedURLException ex) {

            }


            Query i18nMappingFileQuery = Queries.fileEntry("META-INF/resources"+I18N_MAPPINGS_FILE);
            Jarex.createInstance(classpath.get()).all(i18nMappingFileQuery)
                    .withResults().of(i18nMappingFileQuery)
                    .map(ResultsWrapper.Item::getURL)
                    .forEach(mappings::add);

            if(mappings.size()==0)
                LOG.warn("No I18n Mapping config found! Make sure you have placed it correctly.");
            else LOG.info(String.format("%d I18n mapping configuration files found",mappings.size()));
        }

        return mappings;

    }

    private void loadDictionariesWithConcatenation(){
        LOG.info("Loading language dictionary files using CONCATENATION");
        if(!classpath.isPresent())
            throw new I18nException("Can't proceed with language dictionaries concatenation because no valid classpath was specified");

        List<URL> mappingConfigs = getI18nMappingConfigs();

        for(String lang: configuration.getLanguages()){
            LanguageBundle bundle = new LanguageBundle(lang);
            if(configuration.isMappingsEnabled()){
                mappingConfigs.forEach(mappingsXml -> {
                    LOG.info(String.format("Loading [%s] I18n Mappings from : [%s]",lang,
                            mappingsXml.toExternalForm()));
                    I18nMappingsInfo mappings = loadMappings(mappingsXml, lang);
                    bundle.addDictionaries(mappings,configuration);
                });
            }

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

        if(!configuration.isMappingsEnabled()){
            if(configuration.getDictionaries().length==0)
                throw new I18nException(" You must specify at least one dictionary when I18n Mappings are not enabled");
        }

        if(!configuration.isConcatenateDictionaries()){
            loadDictionariesWithoutConcatenation();
        }else loadDictionariesWithConcatenation();


        if(configuration.getCacheClassName().isPresent()){

            String cacheType = configuration.getCacheClassName().get();

            try {

                Class clazz = Class.forName(cacheType);
                Object cacheInstance = clazz.newInstance();
                if (!(cacheInstance instanceof I18nCache))
                    throw new I18nException(String.format("Cache type is not a valid subclass of I18nCache : %s", cacheType));

            }catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex){

                throw new I18nException(String.format("Failed to instantiate I18n Cache type : %s",cacheType),ex);

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
