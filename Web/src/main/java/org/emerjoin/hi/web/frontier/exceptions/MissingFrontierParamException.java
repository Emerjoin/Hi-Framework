package org.emerjoin.hi.web.frontier.exceptions;

/**
 * Created by Mario Junior.
 */
public class MissingFrontierParamException extends FrontierCallException {

    public MissingFrontierParamException(String frontier, String method, String paramName){

        super(frontier,method,"No value supplied for frontier param <"+paramName+"> for method <"+method+"> on "+frontier);

    }


}
