package org.emerjoin.hi.web;

/**
 * @author Mário Júnior
 */
public interface Transformable {

    Transformable appendJS(String url);

    Transformable appendCSS(String path);

    Transformable prependJS(String url);

    Transformable prependCSS(String path);

    Transformable append(String content);

    Transformable prepend(String content);

    @Deprecated
    String getMarkup();

    String getHtml();

    @Deprecated
    Transformable setMarkup(String markup);

    Transformable replaceHtml(String html);

}
