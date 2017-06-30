package org.emerjoin.hi.web.i18n;

/**
 * @author Mário Júnior
 */
public class I18nException extends RuntimeException {

    public I18nException(String message, Throwable cause){
        super(message,cause);
    }

    public I18nException(String message){

        super(message);

    }

}
