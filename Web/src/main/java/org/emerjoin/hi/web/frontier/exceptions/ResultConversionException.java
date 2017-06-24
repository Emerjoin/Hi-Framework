package org.emerjoin.hi.web.frontier.exceptions;

/**
 * Created by Mario Junior.
 */
public class ResultConversionException extends FrontierCallException {

    public ResultConversionException(String frontier, String method, Throwable throwable){

        super(frontier,method,"Result of method method <"+method+"> on frontier <"+frontier+"> could not be converted to JSON successfully.",throwable);

    }

}
