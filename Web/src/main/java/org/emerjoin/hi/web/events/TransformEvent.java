package org.emerjoin.hi.web.events;

import org.emerjoin.hi.web.Transformable;

/**
 * @author Mário Júnior
 */
public abstract class TransformEvent extends HiEvent {

    private Transformable transformable = null;

    public TransformEvent(Transformable transformable){
        super();
        if(transformable==null)
            throw new IllegalArgumentException("Transformable must not be null");
        this.transformable = transformable;
    }

    public Transformable getTransformable(){

        return transformable;

    }

}
