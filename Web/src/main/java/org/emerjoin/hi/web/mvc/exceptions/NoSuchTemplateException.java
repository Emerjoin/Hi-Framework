package org.emerjoin.hi.web.mvc.exceptions;

/**
 * Exception thrown when the template file does not exists
 * or when it exists but is not accessible.
 *
 * Created by Mario Junior.
 */
public class NoSuchTemplateException extends TemplateException {

    public NoSuchTemplateException(String templateName){

        super("The template file /"+templateName+" could not be found. Check if file exists and if its accessible to the application server. Make sure that html and javascript files are both present");

    }


}
