package org.emerjoin.hi.web;

/**
 * @author Mário Júnior
 */
public interface Transformable {


    String getHtml();

    Transformable replaceHtml(String html);

}
