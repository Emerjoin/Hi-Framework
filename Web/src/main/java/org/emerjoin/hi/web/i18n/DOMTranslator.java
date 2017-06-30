package org.emerjoin.hi.web.i18n;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mário Júnior
 */
public class DOMTranslator {

    public DOMTranslator(){

    }

    public void translateFragment(Document fragment, LanguageBundle bundle){
        if(fragment==null)
            throw new IllegalArgumentException("Html fragment must not be null");
        if(bundle==null)
            throw new IllegalArgumentException("LanguageBundle instance must not be null");

        Elements elements = fragment.select("[translate]");
        for(Element element : elements)
            translateElement(element,bundle);

    }


    private void translateElement(Element node, LanguageBundle bundle){
        Attributes attributes = node.attributes();
        List<Attribute> attributesList = attributes.asList()
                .parallelStream()
                .filter((attribute -> attribute.getKey().startsWith("translate-")))
                .collect(Collectors.toList());

        //Translate inner HTML
        if(attributesList.size()==0){

            String translateAttrValue = node.attr("translate");
            if(translateAttrValue.isEmpty()){
                //Translate content directly
                String htmlTranslated = bundle.translate(node.html());
                if(htmlTranslated!=null)//HTML content translated successfully
                    node.html(htmlTranslated);

                node.removeAttr("translate");
                return;
            }

            //Translate inner HTML using a key
            String translatedKey = bundle.translate(translateAttrValue);
            if(translatedKey!=null)//Key translated successfully
                node.html(translatedKey);

            node.removeAttr("translate");
            return;
        }

        for(Attribute attribute: attributesList) {
            translateAttribute(attribute, node, bundle);
            node.removeAttr(attribute.getKey());//Remove the attribute
        }

        node.removeAttr("translate");

    }


    private void translateAttribute(Attribute attribute, Element element, LanguageBundle bundle){
        String name = attribute.getKey();
        int firstHyphenIndex = name.indexOf('-');
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
        String translatedValue = bundle.translate(targetAttrValue);

        if(translatedValue!=null)//Value translated successfully
            element.attr(targetAttrName,translatedValue);
    }



}
