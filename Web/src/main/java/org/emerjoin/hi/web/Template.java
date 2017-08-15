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
