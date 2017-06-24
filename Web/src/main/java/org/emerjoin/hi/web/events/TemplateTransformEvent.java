package org.emerjoin.hi.web.events;

import org.emerjoin.hi.web.Template;

/**
 * @author Mário Júnior
 */
public class TemplateTransformEvent extends HiEvent {

    private Template template;

    public TemplateTransformEvent(String name, String content){
        super();
        this.template = new Template(name,content);

    }

    public Template getTemplate(){

        return template;

    }

}
