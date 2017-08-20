package org.emerjoin.hi.web.i18n;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mário Júnior
 */
public class DOMTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOMTranslator.class);

    public void translateFragment(Document fragment, LanguageBundle bundle){
        if(fragment==null)
            throw new IllegalArgumentException("Html fragment must not be null");
        if(bundle==null)
            throw new IllegalArgumentException("LanguageBundle instance must not be null");

        Elements elements = fragment.select("[^translate]");
        for(Element element : elements)
            translateElement(element,bundle);

    }


    private void translateElement(Element node, LanguageBundle bundle){
        Attributes attributes = node.attributes();
        List<Attribute> attributesList = attributes.asList()
                .parallelStream()
                .filter((attribute -> attribute.getKey().startsWith("translate")))
                .collect(Collectors.toList());

        for(Attribute attribute: attributesList) {
            translateAttribute(attribute, node, bundle);
            node.removeAttr(attribute.getKey());//Remove the attribute
        }

    }


    private void translateAttribute(Attribute attribute, Element element, LanguageBundle bundle){
        String name = attribute.getKey();
        int firstHyphenIndex = name.indexOf('-');

        //Translate inner HTML
        if(firstHyphenIndex==-1){

            String translateKey = attribute.getValue();

            //inner HTML
            if(translateKey==null||translateKey.isEmpty())
                translateKey = element.html();

            if(translateKey.isEmpty())
                return;

            String translated = bundle.translate(translateKey);
            if(translated!=null)
                element.html(translated);
            return;

        }

        String targetAttrName = name.substring(firstHyphenIndex+1,name.length());

        if(attribute.getValue()!=null&&!attribute.getValue().isEmpty()){
            //Translate based in a key: do nothing if cant find the key
            String keyTranslation = bundle.translate(attribute.getValue());

            if(keyTranslation!=null)//Key translated successfully
                element.attr(targetAttrName,keyTranslation);

            return;
        }

        //Translate the value of the target attribute
        String targetAttrValue = element.attr(targetAttrName);
        if(targetAttrValue.isEmpty())
            return;

        String translatedValue = bundle.translate(targetAttrValue);

        if(translatedValue!=null)//Value translated successfully
            element.attr(targetAttrName,translatedValue);
    }



}
