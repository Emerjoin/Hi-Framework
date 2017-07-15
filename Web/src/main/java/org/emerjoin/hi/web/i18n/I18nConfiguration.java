package org.emerjoin.hi.web.i18n;

import java.net.URL;
import java.util.*;

/**
 * @author Mário Júnior
 */
public class I18nConfiguration {

    private boolean concatenateDictionaries;
    private boolean  exportLanguageBundle = true;
    private String[] dictionaries;
    private String[] languages;
    private String defaultLanguage;
    private boolean encodingUTF8 = false;
    private boolean mappingsEnabled = false;
    private String cacheClassName;
    private Map<String,List<URL>> mappings = new HashMap<>();

    public boolean isConcatenateDictionaries() {
        return concatenateDictionaries;
    }

    public void setConcatenateDictionaries(boolean concatenateDictionaries) {
        this.concatenateDictionaries = concatenateDictionaries;
    }

    public String[] getDictionaries() {
        return dictionaries;
    }

    protected void setDictionaries(String[] dictionaries) {
        this.dictionaries = dictionaries;
    }

    public String[] getLanguages() {
        return languages;
    }

    protected void setLanguages(String[] languages) {
        this.languages = languages;
    }


    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    protected void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public boolean isEncodingUTF8() {
        return encodingUTF8;
    }

    protected void setEncodingUTF8(boolean encodingUTF8) {
        this.encodingUTF8 = encodingUTF8;
    }

    public Optional<String> getCacheClassName() {
        return Optional.ofNullable(cacheClassName);
    }

    protected void setCacheClassName(String cacheClassName) {
        this.cacheClassName = cacheClassName;
    }


    private void validateMappingPath(String path){

        if(path==null||path.isEmpty())
            throw new IllegalArgumentException("Path must not be null nor empty");

    }

    protected void addMapping(String path,URL dictionaryFile){
        validateMappingPath(path);
        if(dictionaryFile==null)
            throw new IllegalArgumentException("Language dictionary file must not be null");

        List<URL> mappingsList = new ArrayList<>();
        if(mappings.containsKey(path))
            mappingsList = mappings.get(path);

        mappingsList.add(dictionaryFile);
        mappings.put(path,mappingsList);

    }

    protected void setMappings(String path, List<URL> mappingsList){
        validateMappingPath(path);
        if(mappingsList==null||mappingsList.isEmpty())
            throw new IllegalArgumentException("Mappings list must not be null nor empty");

        this.mappings.put(path,mappingsList);

    }

    public List<URL> getMappings(String path){
        validateMappingPath(path);

        if(!mappings.containsKey(path))
            return Collections.emptyList();

        return mappings.get(path);

    }

    protected void enableMappings(){

        this.mappingsEnabled = true;
        this.exportLanguageBundle = false;

    }


    public boolean isMappingsEnabled(){

        return mappingsEnabled;

    }

    public boolean isExportLanguageBundle() {
        return exportLanguageBundle;
    }

    public void setExportLanguageBundle(boolean exportLanguageBundle) {
        this.exportLanguageBundle = exportLanguageBundle;
    }
}