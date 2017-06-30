package org.emerjoin.hi.web.i18n;

import java.util.Optional;

/**
 * @author Mário Júnior
 */
public class I18nConfiguration {

    private boolean concatenateDictionaries;
    private String[] dictionaries;
    private String[] languages;
    private String defaultLanguage;
    private boolean encodingUTF8 = false;
    private String cacheClassName;

    public boolean isConcatenateDictionaries() {
        return concatenateDictionaries;
    }

    public void setConcatenateDictionaries(boolean concatenateDictionaries) {
        this.concatenateDictionaries = concatenateDictionaries;
    }

    public String[] getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(String[] dictionaries) {
        this.dictionaries = dictionaries;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }


    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public boolean isEncodingUTF8() {
        return encodingUTF8;
    }

    public void setEncodingUTF8(boolean encodingUTF8) {
        this.encodingUTF8 = encodingUTF8;
    }

    public Optional<String> getCacheClassName() {
        return Optional.ofNullable(cacheClassName);
    }

    public void setCacheClassName(String cacheClassName) {
        this.cacheClassName = cacheClassName;
    }
}