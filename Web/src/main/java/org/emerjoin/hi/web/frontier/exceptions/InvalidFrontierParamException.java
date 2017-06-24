package org.emerjoin.hi.web.frontier.exceptions;

/**
 * Created by Mario Junior.
 */
public class InvalidFrontierParamException extends FrontierCallException {

    public InvalidFrontierParamException(String frontier, String method, String paramName){

        super(frontier,method,"Invalid value supplied for frontier param <"+paramName+"> for method <"+method+"> on "+frontier);

    }

}
