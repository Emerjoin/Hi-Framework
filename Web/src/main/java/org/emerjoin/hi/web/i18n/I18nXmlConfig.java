package org.emerjoin.hi.web.i18n;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.BadConfigException;
import org.emerjoin.hi.web.config.Configurator;
import org.emerjoin.hi.web.config.xml.ConfigSection;
import org.emerjoin.xmleasy.XMLEasy;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Mário Júnior
 */
@ConfigSection(tags = "i18n")
public class I18nXmlConfig implements Configurator {

    private static final String ENABLE_CONCATENATION_ELEMENT = "enable-concatenation";
    private static final String CACHE_CLASS_NAME_ELEMENT = "cache";
    private static final String UNICODE_ENCODING_ELEMENT = "unicode-encoding";
    private static final String ENABLE_MAPPINGS_ELEMENT = "enable-mappings";
    private static final String DISABLE_BUNDLE_EXPORTATION_ELEMENT = "disable-full-bundle-exportation";

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

        final  List<String> dictionaries = new ArrayList<>();
        i18nXml.ifChild("dictionaries").then((element -> {
            element.streamChildren().map(Element::getTextContent)
                    .forEach(dictionaries::add);
        })).eval();


        String[] dicts = new String[dictionaries.size()];
        dictionaries.toArray(dicts);
        configuration.setDictionaries(dicts);

        configureConcatenation(i18nXml,configuration);
        configureCaching(i18nXml,configuration);
        configureUnicode(i18nXml,configuration);
        configureMappings(i18nXml,configuration);

        validateConfig(configuration);
        apply(configuration);

    }



    private void configureConcatenation(XMLEasy i18nXml, I18nConfiguration configuration){

        if(i18nXml.hasChild(ENABLE_CONCATENATION_ELEMENT))
            configuration.setConcatenateDictionaries(Boolean
                    .parseBoolean(i18nXml.child(ENABLE_CONCATENATION_ELEMENT).getContent()));


    }


    private void configureCaching(XMLEasy i18nXml, I18nConfiguration configuration){

        if(i18nXml.hasChild(CACHE_CLASS_NAME_ELEMENT))
            configuration.setCacheClassName(i18nXml.child(CACHE_CLASS_NAME_ELEMENT).getContent());

    }


    private void configureUnicode(XMLEasy i18nXml, I18nConfiguration configuration){

        if(i18nXml.hasChild(UNICODE_ENCODING_ELEMENT))
            configuration.setEncodingUTF8(Boolean.parseBoolean(i18nXml
                    .child(UNICODE_ENCODING_ELEMENT).getContent()));

    }


    private void configureMappings(XMLEasy i18nXml, I18nConfiguration configuration){

        i18nXml.ifChild(DISABLE_BUNDLE_EXPORTATION_ELEMENT)
                .then(element -> configuration.setExportLanguageBundle(
                            !Boolean.parseBoolean(element.getContent()))).eval();

        i18nXml.ifChild(ENABLE_MAPPINGS_ELEMENT)
                .then(element -> {
                    boolean enable = Boolean.parseBoolean(element.getContent());
                    if(enable)
                        configuration.enableMappings();
                }).eval();
    }

    private void validateConfig(I18nConfiguration configuration){

        if(configuration.getDefaultLanguage()==null)
            throw new I18nException("No default Language Set");


    }

    public void apply(I18nConfiguration configuration){

        I18nXmlConfig.configuration = configuration;

    }

}
