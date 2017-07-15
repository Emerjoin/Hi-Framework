package org.emerjoin.hi.web.i18n;

/**
 * @author Mário Júnior
 */
public class UnknownDictionaryException extends I18nException {

    public UnknownDictionaryException(String name){

        super(String.format("No dictionary with name \"%s\" could be found",name));

    }

}
