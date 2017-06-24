package org.emerjoin.hi.web.req;

import org.emerjoin.hi.web.exceptions.HiException;

/**
 * Created by Mario Junior.
 */
public class ReqMatchException extends HiException {

    public ReqMatchException(String handler,String error){

        super("Could not finish request matching on Request Handler <"+handler+"> : "+error);

    }

}
