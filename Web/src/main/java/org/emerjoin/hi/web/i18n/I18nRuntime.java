package org.emerjoin.hi.web.i18n;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mário Júnior
 */
public class I18nRuntime {

    private static I18nRuntime instance = null;
    private Map<String,LanguageBundle> bundles = null;
    private ThreadLocal<String> currentLanguage = new ThreadLocal<>();
    private ThreadLocal<LanguageBundle> currentBundle = new ThreadLocal<>();
    private I18nCache cache = null;
    private DOMTranslator DOMTranslator = null;
    private boolean underDevelopment = false;

    private I18nRuntime(){

        this.DOMTranslator = new DOMTranslator();
        this.bundles = new HashMap<>();

    }


    public static boolean isReady(){

        return instance!=null;

    }

    public static I18nRuntime get(){
        if(instance==null)
            throw new I18nException("I18n is not available. Please activate it in Hi.xml");

        return instance;

    }

    protected static void init(I18nStarter i18NStarter, AppConfigurations configurations){
        if(I18nRuntime.instance!=null)
            throw new I18nException("I18nRuntime already initialized");

        I18nRuntime.instance = new I18nRuntime();
        instance.bundles = i18NStarter.getBundles();
        instance.cache = i18NStarter.getI18nCache();
        instance.underDevelopment  = configurations.underDevelopment();


    }


    public void setLanguage(String lang){
        if(lang==null||lang.isEmpty())
            throw new IllegalArgumentException("Language name must not be null nor empty");

        if(!bundles.containsKey(lang))
            throw new IllegalArgumentException(String.format("Language bundle not found : %s",lang));

        this.currentLanguage.set(lang);
        this.currentBundle.set(bundles.get(lang));

    }


    public void unsetLanguage(){

        this.currentLanguage.remove();
        this.currentBundle.remove();

    }


    private void checkLanguageSet(){

        if(currentBundle.get()==null||currentLanguage.get()==null)
            throw new IllegalStateException("No current language set");

    }


    public String translateTemplate(String name, String html){
        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Template's name must not be null nor empty");

        if(html==null||html.isEmpty())
            throw new IllegalArgumentException("Template's html must not be null nor empty");

        if(underDevelopment)
            return translateHTMLFragment(Jsoup.parse(html));

        String cachingKey = String.format("template-%s-%s",name,currentLanguage.get());
        if(cache.contains(cachingKey))
            return cache.pull(cachingKey);

        String translated = translateHTMLFragment(Jsoup.parse(html));
        cache.push(cachingKey,translated);
        return translated;

    }


    public String translateView(String name, String html){
        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("View's name must not be null nor empty");
        if(html==null||html.isEmpty())
            throw new IllegalArgumentException("View's html must not be null nor empty");

        if(underDevelopment)
            return translateHTMLFragment(Jsoup.parseBodyFragment(html));

        String cachingKey = String.format("view-%s-%s",name,html);
        if(cache.contains(cachingKey))
            return cache.pull(cachingKey);

        String translated = translateHTMLFragment(Jsoup.parseBodyFragment(html));
        cache.push(cachingKey,translated);
        return translated;

    }

    public String translateHTMLFragment(Document html){
        if(html==null)
            throw new IllegalArgumentException("HTML fragment must  not be null");

        checkLanguageSet();
        DOMTranslator.translateFragment(html,currentBundle.get());
        return html.outerHtml();

    }

    public Map<String,String> getDictionary(String name){
        if(name==null||name.isEmpty())
            throw new IllegalArgumentException("Dictionary's name must not be null nor empty");
        checkLanguageSet();

        LanguageBundle bundle = currentBundle.get();
        if(!bundle.hasDictionary(name))
            throw new UnrecognizedLanguageException(name);

        return  bundle.getDictionary(name);

    }

    public String translate(String string){

        if(string==null||string.isEmpty())
            throw new IllegalArgumentException("string to be translated must not be null nor empty");

        checkLanguageSet();
        String translated = currentBundle.get().translate(string);
        if(translated==null)
            return string;

        return translated;

    }

    public LanguageBundle getBundle(){

        checkLanguageSet();
        return currentBundle.get();

    }

}


