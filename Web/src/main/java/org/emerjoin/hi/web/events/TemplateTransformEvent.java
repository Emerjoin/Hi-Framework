package org.emerjoin.hi.web.events;

import org.emerjoin.hi.web.Template;
import org.emerjoin.hi.web.Transformable;

/**
 * @author Mário Júnior
 */
public class TemplateTransformEvent extends TransformEvent {

    public TemplateTransformEvent(Template template){

        super(template);

    }

    public Template getTemplate(){

        return (Template) getTransformable();

    }


}
