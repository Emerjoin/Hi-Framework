package org.emerjoin.hi.web.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mário Júnior
 */
public class Logging {

    private static Logging instance = null;

    public static Logging getInstance(){

        if(instance==null)
            instance = new Logging();

        return instance;

    }


    private String loggerName = "hi-web";

    private Logging(){ }

    public Logger getLogger(){

        return LoggerFactory.getLogger(loggerName);

    }

    public Logger getLogger(Class type){

        return LoggerFactory.getLogger(type);

    }
}
