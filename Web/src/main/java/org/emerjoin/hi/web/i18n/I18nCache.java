package org.emerjoin.hi.web.i18n;

/**
 * @author Mário Júnior
 */
public interface I18nCache {

    boolean contains(String key);
    void push(String key, String content);
    String pull(String key);

}
