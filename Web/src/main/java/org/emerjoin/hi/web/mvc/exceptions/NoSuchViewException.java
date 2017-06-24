package org.emerjoin.hi.web.mvc.exceptions;

/**
 * Created by Mario Junior.
 */
public class NoSuchViewException extends MvcException {

    private String controller;
    private String view;

    public NoSuchViewException(String controller,String view,String path){
        super("Could not find the view file <"+view+"> for controller <"+controller+"> : "+path);
        this.controller = controller;
        this.view = view;

    }

}
