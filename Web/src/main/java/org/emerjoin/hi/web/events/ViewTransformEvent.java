package org.emerjoin.hi.web.events;

import org.emerjoin.hi.web.View;

/**
 * @author Mário Júnior
 */
public class ViewTransformEvent extends TransformEvent {


    public ViewTransformEvent(View view) {

        super(view);

    }

    public View getView(){

        return (View) getTransformable();

    }

}
