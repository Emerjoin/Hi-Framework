package org.emerjoin.hi.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Mário Júnior
 */
public abstract class AbstractTransformable implements Transformable {

    private String markup = "";

    public AbstractTransformable(String html){
        if(html==null||html.isEmpty())
            throw new IllegalArgumentException("Html must not be null nor empty");
        this.markup = html;
    }



    protected void validateContent(String content){
        if(content==null||content.isEmpty())
            throw new IllegalArgumentException("Content must not be null nor empty");


    }



    public Transformable replaceHtml(String html){
        if(html==null||html.isEmpty())
            throw new IllegalArgumentException("html must not be null nor empty");
        this.markup = html;
        return this;
    }



    public String getHtml(){

        return markup;

    }

}
