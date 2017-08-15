package org.emerjoin.hi.web.transformers;

import org.emerjoin.hi.web.Template;
import org.emerjoin.hi.web.Transformable;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.events.TransformEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

/**
 * @author Mário Júnior
 */
@ApplicationScoped
public class IfDevTrasformer {

    public static final String IF_DEV_ATTR = "if-dev";

    private AppConfigurations app = null;

    @PostConstruct
    public void init(){

        this.app = AppConfigurations.get();


    }


    public void watch(@Observes TransformEvent transform){

        Transformable transformable = transform.getTransformable();
        if(transformable instanceof Template)
            template(transformable);
        else view(transformable);


    }


    private void template(Transformable transformable){

         String transformed = apply(Jsoup
                 .parse(transformable.getHtml()));
        transformable.replaceHtml(transformed);

    }


    private void view(Transformable transformable){

        String transformed = apply(Jsoup
                .parseBodyFragment(transformable.getHtml()));
        transformable.replaceHtml(transformed);

    }


    private String apply(Document document){
        document.select(String.format("[%s]",
                IF_DEV_ATTR))
                .forEach(this::ifDevelopment);
        return document.outerHtml();
    }

    private void ifDevelopment(Element element){

        String attrValue = element.attr(IF_DEV_ATTR);
        boolean keepElement = true;

        if(attrValue.equalsIgnoreCase("false"))
            keepElement = false;

        //Remove element if NOT in DEVELOPMENT_MODE
        if(keepElement&&!app.underDevelopment())
            element.remove();


        //Remove element if in DEVELOPMENT_MODE
        if(!keepElement&&app.underDevelopment())
            element.remove();

    }


}
