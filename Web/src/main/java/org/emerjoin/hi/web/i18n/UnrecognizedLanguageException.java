package org.emerjoin.hi.web.i18n;

/**
 * @author Mário Júnior
 */
public class UnrecognizedLanguageException extends I18nException {

    public UnrecognizedLanguageException(String name){

        super(String.format("Language with name \"%s\" is not recognized",name));

    }

}
