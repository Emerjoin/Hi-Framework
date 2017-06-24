package org.emerjoin.hi.web.frontier.exceptions;

/**
 * Created by Mario Junior.
 */
public class FrontierMethodException extends FrontierCallException {

    public FrontierMethodException(String frontier, String method, Exception throwable){

        super(frontier,method,"The invocation of frontier method <"+method+"> on <"+frontier+"> failed. An exception thrown during invocation.",throwable);

    }

}
