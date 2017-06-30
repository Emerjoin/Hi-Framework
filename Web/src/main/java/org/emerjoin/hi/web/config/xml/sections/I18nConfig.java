package org.emerjoin.hi.web.config.xml.sections;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.BadConfigException;
import org.emerjoin.hi.web.config.Configurator;
import org.emerjoin.hi.web.config.xml.ConfigSection;
import org.emerjoin.hi.web.i18n.I18nConfiguration;
import org.emerjoin.hi.web.i18n.I18nException;
import org.emerjoin.xmleasy.XMLEasy;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Mário Júnior
 */
@ConfigSection(tags = "i18n")
public class I18nConfig implements Configurator {

    private static final String ENABLE_CONCATENATION_ELEMENT = "enable-concatenation";
    private static final String CACHE_CLASS_NAME_ELEMENT = "cache";
    private static final String UNICODE_ENCODING_ELEMENT = "unicode-encoding";

    private static I18nConfiguration configuration = null;
    public static Optional<I18nConfiguration> getConfiguration(){

        return Optional.ofNullable(configuration);

    }

    @Override
    public void doConfig(AppConfigurations configs, Map<String, Element> elements, Element document) throws BadConfigException {
        XMLEasy i18nXml = new XMLEasy(document).child("i18n").freeze();
        I18nConfiguration configuration = new I18nConfiguration();

        List<String> languages = new ArrayList<>();
        i18nXml.child("languages").streamChildren()
                .forEach(element -> {
                    XMLEasy easy = XMLEasy.easy(element);
                    String languageName = element.getTextContent();
                    languages.add(languageName);
                    Optional<String> defaultLang = easy.optionalAttribute("default");
                    if(defaultLang.isPresent()){
                        if(defaultLang.get().toLowerCase().equals("true")) {
                            configuration.setDefaultLanguage(languageName);
                            configs.setDefaultLanguage(languageName);
                        }
                    }
                });

        String[] langs = new String[languages.size()];
        languages.toArray(langs);
        configuration.setLanguages(langs);

        List<String> dictionaries = i18nXml.child("dictionaries").streamChildren()
                .map(Element::getTextContent)
                .collect(Collectors.toList());

        String[] dicts = new String[dictionaries.size()];
        dictionaries.toArray(dicts);
        configuration.setDictionaries(dicts);

        if(i18nXml.hasChild(ENABLE_CONCATENATION_ELEMENT))
            configuration.setConcatenateDictionaries(Boolean
                    .parseBoolean(i18nXml.child(ENABLE_CONCATENATION_ELEMENT).getContent()));

        if(i18nXml.hasChild(CACHE_CLASS_NAME_ELEMENT))
            configuration.setCacheClassName(i18nXml.child(CACHE_CLASS_NAME_ELEMENT).getContent());

        if(i18nXml.hasChild(UNICODE_ENCODING_ELEMENT))
            configuration.setEncodingUTF8(Boolean.parseBoolean(i18nXml
                    .child(UNICODE_ENCODING_ELEMENT).getContent()));

        validateConfig(configuration);
        apply(configuration);

    }


    private void validateConfig(I18nConfiguration configuration){

        if(configuration.getDefaultLanguage()==null)
            throw new I18nException("No default Language Set");


    }

    public void apply(I18nConfiguration configuration){

        I18nConfig.configuration = configuration;

    }

}
