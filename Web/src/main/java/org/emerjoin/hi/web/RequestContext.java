package org.emerjoin.hi.web;

import org.emerjoin.hi.web.internal.JSCommandSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


//TODO: JavaDoc
@RequestScoped
public class RequestContext {

    public static String AJAX_HEADER_KEY = "AJAX_MVC";

    @Inject
    private AppContext appContext;

    @Inject
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    @Inject
    private Event<JSCommandSchedule> jsCommandScheduleEvent;

    @Inject
    private ServletContext servletContext = null;
    private Map<String,Object> data = new HashMap<String, Object>();
    private String url = null;
    private String routeUrl;

    private OutputStream outputStream = null;
    private Logger log = LoggerFactory.getLogger(RequestContext.class);

    @PostConstruct
    private void getReady(){

        this.url = request.getRequestURI();

    }

    protected void setRouteUrl(String r){

        this.routeUrl = r;

    }

    public void setUrl(String url) {
        this.url = url;
    }

    protected  void setResponse(HttpServletResponse response){

        this.response = response;

    }

    public void echo(String  str){

        Helper.echo(str,this);

    }

    public void echoln(String  str){

        Helper.echoln(str,this);

    }


    public String readToEnd(InputStream inputStream){

        return  Helper.readLines(inputStream,this);

    }


    public String getUsername(){

        return request.getRemoteUser();

    }

    public boolean isUserLogged(){

        return request.getRemoteUser()!=null;

    }

    public OutputStream getOutputStream(){

        if(outputStream ==null){
            try {

                outputStream = response.getOutputStream();

            }catch (Throwable ex){
                log.error("Failed to get the HttpServletResponse OutputStream",ex);
            }
        }

        return outputStream;

    }

    public String getRouteUrl(){

        return routeUrl;

    }

    public  HttpServletRequest getRequest(){

        return request;

    }

    public HttpServletResponse getResponse(){

        return response;

    }

    public  boolean hasAjaxHeader(){

        if(request==null)
            return false;
        return request.getHeader(AJAX_HEADER_KEY)!=null;

    }



    public ServletContext getServletContext(){

        return  servletContext;

    }


    public String getUrl() {
        return url;
    }

    public Map<String,Object> getData(){

        return data;

    }

    public void sendRedirect(String route) throws IOException{
        if(hasAjaxHeader()){
            log.debug("Ajax Redirect: "+route);
            this.ajaxRedirect(routeUrl);
            return;
        }
        String path = appContext.getBaseURL() + route;
        log.debug("HTTP Redirect: "+path);
        getResponse().sendRedirect(path);
    }

    private void ajaxRedirect(String url){
        JSCommandSchedule redirectCommand = new JSCommandSchedule(FrontEnd.
                COMMAND_REDIRECT);
        redirectCommand.getParameters().put(FrontEnd.COMMAND_URL_PARAM, url);
        jsCommandScheduleEvent.fire(
                redirectCommand);
    }

}
