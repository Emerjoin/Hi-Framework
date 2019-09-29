package org.emerjoin.hi.web.mvc;

import org.emerjoin.hi.web.*;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.Frontiers;
import org.emerjoin.hi.web.config.Security;
import org.emerjoin.hi.web.events.TemplateLoadEvent;
import org.emerjoin.hi.web.events.TemplateTransformEvent;
import org.emerjoin.hi.web.events.ViewTransformEvent;
import org.emerjoin.hi.web.i18n.I18nContext;
import org.emerjoin.hi.web.mvc.exceptions.ConversionFailedException;
import org.emerjoin.hi.web.mvc.exceptions.MvcException;
import org.emerjoin.hi.web.mvc.exceptions.NoSuchViewException;
import org.emerjoin.hi.web.mvc.exceptions.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
//TODO: JavaDoc
//TODO: Refactor
public class Controller {

    public static final String VIEW_DATA_KEY ="dataJson";

    @Inject
    private HTMLizer htmLizer;

    @Inject
    private Event<TemplateLoadEvent> templateLoadEvent;

    @Inject
    private Event<TemplateTransformEvent> templateTransformEvent;

    @Inject
    private Event<ViewTransformEvent> viewTransformEventEvent;

    @Inject
    private I18nContext i18nContext;

    @Inject
    private AppContext appContext;

    @Inject
    private ActiveUser activeUser;

    private static Logger _log = LoggerFactory.getLogger(Controller.class);

    private void prepareView(RequestContext requestContext, String controllerName, String actionName,
                             String viewFile, String viewJsfile, String viewJsMinifiedfile) throws NoSuchViewException {
        URL viewResource = null;
        URL viewJsResource = null;

        try {
            viewResource = requestContext.getServletContext().getResource(viewFile);
            if(viewResource==null)
                throw new NoSuchViewException(controllerName,actionName,viewFile);

        }catch (Throwable ex){
            throw new NoSuchViewException(controllerName,actionName,viewFile);
        }

        try{

            if(AppConfigurations.get().underDevelopment())
                viewJsResource   = requestContext.getServletContext().getResource(viewJsfile);
            else{
                viewJsResource = requestContext.getServletContext().getResource(viewJsMinifiedfile);
                if(viewJsResource==null)
                    viewJsResource   = requestContext.getServletContext().getResource(viewJsfile);
            }

        }catch (Throwable ex){
            _log.error(String.format("Failed to get the View JS Resource using: %s AND %s",viewJsfile,viewJsMinifiedfile),ex);
            return;
        }

        if(requestContext.getRequest().getHeader("Ignore-Js")==null)
            doNotIgnoreJS(requestContext,viewJsResource);

        if(viewJsResource==null)
            throw new NoSuchViewException(controllerName,actionName,viewJsfile);

        if(requestContext.getRequest().getHeader("Ignore-View")==null)
            doNotIgnoreView(requestContext,viewResource);

    }

    private void doNotIgnoreJS(RequestContext requestContext, URL viewJsResource){

        if (viewJsResource != null) {
            try {

                InputStream viewJsInputStream = viewJsResource.openStream();
                String viewJsContent = Helper.readLines(viewJsInputStream,null);
                requestContext.getData().put("view_js", viewJsContent);
            } catch (Exception ex) {

                _log.error(String.format("Failed to read the View JS Resource : %s",viewJsResource.getPath()),ex);

            }
        }

    }

    private void doNotIgnoreView(RequestContext requestContext, URL viewResource){

        try {

            InputStream viewInputStream = viewResource.openStream();
            String viewContent = Helper.readLines(viewInputStream, null);
            requestContext.getData().put("view_content",viewContent);


        }catch (Exception ex){

            _log.error(String.format("Failed to read the View HTML Resource : %s",viewResource.getPath()),ex);

        }

    }

    public void callView() throws MvcException {

        this.callView(null);

    }

    public void callView(Map<String,Object> values) throws NoSuchViewException, TemplateException, ConversionFailedException {
        AppConfigurations config = AppConfigurations.get();
        RequestContext requestContext = CDI.current().select(RequestContext.class).get();

        String actionName = requestContext.getData().get("actionU").toString();
        String controllerName = requestContext.getData().get("controllerU").toString();
        String presentationHtmlFile = actionName.toString();

        String viewMode = requestContext.getRequest().getParameter("$");
        boolean withViewMode = false;
        if(viewMode!=null && viewMode.trim().length()>0){
            presentationHtmlFile = presentationHtmlFile+"."+viewMode;
            withViewMode = true;
        }

        String viewFile = "/"+config.getViewsDirectory()+"/"+controllerName+"/"+presentationHtmlFile+".html";
        String viewJSFile = "/"+config.getViewsDirectory()+"/"+controllerName+"/"+actionName.toString()+".js";
        String viewJSMiniFile = "/"+config.getViewsDirectory()+"/"+controllerName+"/"+actionName.toString()+".min.js";

        FrontEnd frontEnd = CDI.current().select(FrontEnd.class).get();
        TemplateLoadEvent event = null;

        if(!requestContext.hasAjaxHeader()){
            this.emitSecurityHeaders(requestContext);
            event = new TemplateLoadEvent();
            templateLoadEvent.fire(event);
        }

        if(values==null)
            values = new HashMap<>();

        Map<String,Object> templateData = new HashMap<>();
        templateData.putAll(frontEnd.getTemplateData());
        if(event!=null)
            templateData.putAll(event.getValues());

        if(frontEnd.getTemplateData()!=null)
            values.put(HTMLizer.TEMPLATE_DATA_KEY,templateData);

        requestContext.getData().put(VIEW_DATA_KEY,values);

        //Do not need to load the view file
        if(requestContext.getData().containsKey("ignore_view")){
            htmLizer.process(this,true,withViewMode,viewMode,templateTransformEvent,
                    viewTransformEventEvent);
            return;
        }

        prepareView(requestContext,controllerName,actionName,viewFile,viewJSFile,viewJSMiniFile);
        htmLizer.setRequestContext(requestContext);
        htmLizer.setI18nContext(i18nContext);
        htmLizer.process(this,false,withViewMode,viewMode,templateTransformEvent,
                viewTransformEventEvent);

    }

    private void emitSecurityHeaders(RequestContext context){
        this.emitCSRFHeaders(context);
        this.emitCSPHeaders(context);
    }

    private void emitCSRFHeaders(RequestContext context){
        Frontiers frontiersConfig = AppConfigurations.get().getFrontiersConfig();
        Frontiers.Security.CrossSiteRequestForgery crossSiteRequestForgery = frontiersConfig
                .getSecurity()
                .getCrossSiteRequestForgery();
        Frontiers.Security.CrossSiteRequestForgery.Cookie cookieConfig = crossSiteRequestForgery
                .getCookie();
        HttpServletResponse response = context.getResponse();
        String token = activeUser.expireCsrfToken();
        Cookie cookie = new Cookie(Frontiers.Security.CrossSiteRequestForgery.Cookie.NAME,token);
        cookie.setHttpOnly(cookieConfig.isHttpOnly());
        cookie.setSecure(cookieConfig.isSecure());
        cookie.setPath(getCookiePath());
        cookie.setDomain(appContext.getDomain());
        response.addCookie(cookie);
    }

    private String getCookiePath(){
        String baseUrl = appContext.getBaseURL();
        String originUrl = appContext.getOrigin();
        String path = baseUrl.substring(originUrl.length(),baseUrl.length());
        return path;
    }

    private void emitCSPHeaders(RequestContext context){
        Security securityConfig = AppConfigurations.get().getSecurityConfig();
        Security.ContentSecurityPolicy contentSecurityPolicy = securityConfig.getContentPolicy();
        HttpServletResponse response = context.getResponse();
        if(contentSecurityPolicy.isDenyIframeEmbeding())
            response.setHeader("X-Frame-Options","deny");
        String policyStr = contentSecurityPolicy.toString();
        if(!policyStr.isEmpty()) {
            _log.debug("Content-Security-Policy: " + policyStr);
            response.setHeader("Content-Security-Policy", policyStr);
    }
    }

}
