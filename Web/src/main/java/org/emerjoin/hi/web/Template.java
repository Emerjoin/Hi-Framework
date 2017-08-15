package org.emerjoin.hi.web;

/**
 * @author Mário Júnior
 */
public class Template extends AbstractTransformable {

    private String name;

    public Template(String html,String name){
        super(html);
        this.name = name;

    }

    public String getName() {

        return name;

    }


    private void validateUrl(String url){
        if(url==null||url.isEmpty())
            throw new IllegalArgumentException("url must not be null nor empty");
    }

    public Transformable appendJS(String url){
        validateUrl(url);
        append(String.format("<script src='%s'></script>",url));
        return this;

    }

    public Transformable appendCSS(String url){
        validateUrl(url);
        append(String.format("<link rel='stylesheet' href='%s'/>",url));
        return this;

    }

    public Transformable prependJS(String url){
        validateUrl(url);
        prepend(String.format("<script src='%s'></script>",url));
        return this;

    }

    public Transformable prependCSS(String url){
        validateUrl(url);
        prepend(String.format("<link rel='stylesheet' href='%s'/>",url));
        return this;

    }



    public Transformable append(String content){
        validateContent(content);
        replaceHtml(getHtml().replace("</body>",content+"</body>"));
        return this;

    }


    public Transformable prepend(String content){
        validateContent(content);
        replaceHtml(getHtml().replace("<body>",content+"<body>"));
        return this;

    }



    @Deprecated
    public String getMarkup(){

        return getHtml();

    }

    @Deprecated
    public Transformable setMarkup(String markup){
        if(markup==null||markup.isEmpty())
            throw new IllegalArgumentException("markup must not be null nor empty");
        replaceHtml(markup);
        return this;

    }

}
