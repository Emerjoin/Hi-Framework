package org.emerjoin.hi.web;

/**
 * @author Mário Júnior
 */
public class Template {

    private String markup;
    private String name;

    public Template(String name,String markup){

        this.markup = markup;
        this.name = name;

    }

    public Template appendJS(String url){

        append(String.format("<script src='%s'></script>",url));
        return this;

    }

    public Template appendCSS(String path){

        append(String.format("<link rel='stylesheet' href='%s'/>",path));
        return this;

    }

    public Template prependJS(String url){

        prepend(String.format("<script src='%s'></script>",url));
        return this;

    }

    public Template prependCSS(String path){

        prepend(String.format("<link rel='stylesheet' href='%s'/>",path));
        return this;

    }


    public Template append(String content){

        markup = markup.replace("</body>",content+"</body>");
        return this;

    }


    public Template prepend(String content){

        markup = markup.replace("<body>",content+"<body>");
        return this;

    }

    public String getMarkup(){

        return this.markup;

    }

    public void setMarkup(String markup){

        this.markup = markup;

    }

    public String getName() {
        return name;
    }
}
