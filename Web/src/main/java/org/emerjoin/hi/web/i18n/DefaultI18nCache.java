package org.emerjoin.hi.web.i18n;

/**
 * @author Mário Júnior
 */
public class DefaultI18nCache implements I18nCache {


    @Override
    public boolean contains(String key) {

        return false;

    }

    @Override
    public void push(String key, String content) {

    }

    @Override
    public String pull(String key) {

        return null;

    }
}
