package org.emerjoin.hi.web;

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

    public Transformable appendJS(String url){

        append(String.format("<script src='%s'></script>",url));
        return this;

    }

    public Transformable appendCSS(String path){

        append(String.format("<link rel='stylesheet' href='%s'/>",path));
        return this;

    }

    public Transformable prependJS(String url){

        prepend(String.format("<script src='%s'></script>",url));
        return this;

    }

    public Transformable prependCSS(String path){

        prepend(String.format("<link rel='stylesheet' href='%s'/>",path));
        return this;

    }


    public Transformable append(String content){

        markup = markup.replace("</body>",content+"</body>");
        return this;

    }


    public Transformable prepend(String content){

        markup = markup.replace("<body>",content+"<body>");
        return this;

    }

    @Deprecated
    public String getMarkup(){

        return this.markup;

    }

    @Deprecated
    public Transformable setMarkup(String markup){
        if(markup==null||markup.isEmpty())
            throw new IllegalArgumentException("markup must not be null nor empty");
        this.markup = markup;
        return this;

    }

    public Transformable replaceHtml(String html){
        if(html==null||html.isEmpty())
            throw new IllegalArgumentException("html must not be null nor empty");
        this.markup = html;
        return this;
    }


    public String getHtml(){

        return this.markup;

    }

}
