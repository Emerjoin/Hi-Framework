package org.emerjoin.hi.web.req;

import org.emerjoin.hi.web.FrontEnd;
import org.emerjoin.hi.web.HiCDI;
import org.emerjoin.hi.web.RequestContext;
import org.emerjoin.hi.web.events.ControllerRequestEvent;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.i18n.I18nRuntime;
import org.emerjoin.hi.web.mvc.ControllersMapper;
import org.emerjoin.hi.web.AppContext;
import org.emerjoin.hi.web.mvc.HTMLizer;
import org.emerjoin.hi.web.mvc.exceptions.MvcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@HandleRequests(regexp = "(([a-zA-Z-]{2,}\\/[a-zA-Z-]{2,})|([a-zA-Z-]{2,}))")
@ApplicationScoped
public class MVCReqHandler extends ReqHandler{

    private static char[] alphabet = new char[]
            {'A','B','C','D','E','F','G','H','I','J',
            'K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    private static HashMap<String,String> templates = new HashMap<String, String>();
    private static Logger LOG = LoggerFactory.getLogger(MVCReqHandler.class);

    public static void storeTemplate(String name,String content){

        templates.put(name,content);

    }

    public static String getTemplate(String name){

        return templates.get(name);

    }

    public static String getControllerClassFromURLPart(String urlPart){

        String capitalized = urlPart.substring(0,1).toLowerCase()
                +urlPart.substring(1,urlPart.length());
        return noHyphens(capitalized);

    }

    public static String getActionMethodFromURLPart(String urlPart){

        return noHyphens(urlPart);

    }

    public static String getURLAction(String method){

        return getURLController(method);

    }

    public static String getURLController(String clazz){
        StringBuilder alphabetStr = new StringBuilder();
        alphabetStr.append(alphabet);
        char[] controllerChars = clazz.toCharArray();

        StringBuilder urlController = new StringBuilder();
        urlController.append(controllerChars[0]);

        for(int i=1;i<controllerChars.length;i++){

            StringBuilder stringBuilder = new StringBuilder();
            char character = controllerChars[i];
            stringBuilder.append(character);
            //It is a capital character
            if(alphabetStr.indexOf(stringBuilder.toString())!=-1)
                urlController.append('-');
            urlController.append(character);

        }

        return urlController.toString().toLowerCase();

    }



    private RequestContext requestContext = null;

    @Inject
    private AppContext appContext;

    @Inject
    private Event<ControllerRequestEvent> controllerRequestEvent;

    @Inject
    private FrontEnd frontEnd;

    private Logger log;

    private Map<String,Object> getValues(HttpServletRequest request){

        Map<String,Object> finalValues = new HashMap<String, Object>();
        Map<String,String[]> map =  request.getParameterMap();

        for(String key : map.keySet()){
            String[] values = map.get(key);
            if(values.length==1)
                finalValues.put(key,values[0]);
            else finalValues.put(key,values);
        }

        return finalValues;

    }

    private boolean callAction(String action, Class controller,RequestContext requestContext) throws ServletException{

        try {

            Method actionMethod = null;
            boolean withParams = true;

            try {

                actionMethod = controller.getMethod(action, Map.class);

            }catch (Exception ex){
                withParams = false;
                actionMethod = controller.getMethod(action, null);
            }

            String mappedTemplate = ControllersMapper.getPathTemplate(requestContext.getRouteUrl());
            if(mappedTemplate!=null){
                String currentTemplate = frontEnd.getTemplate();
                if(!currentTemplate.equals(mappedTemplate)) {
                    LOG.debug(String.format("Setting mapped template : %s",mappedTemplate));
                    frontEnd.setTemplate(mappedTemplate);
                }
            }

            if(!ReqHandler.accessGranted(controller,actionMethod)){

                try {
                    requestContext.getResponse().sendError(403);
                    return true;

                }catch (Throwable ex){
                    log.error(String.format("Failed no send 403 error code for controller %s and action %s",
                            controller.getCanonicalName(),action),ex);
                    return true;
                }

            }

            actionMethod.setAccessible(true);
            invokeControllerActionMethod(controller,actionMethod,withParams);
            return true;

        }catch (NoSuchMethodException ex){
            return false;
        }catch (InvocationTargetException e2 ) {
            throw new MvcException("Exception thrown while invoking action <" + action + "> on controller <" + controller.getCanonicalName() + ">", e2.getTargetException());
        }catch (IllegalAccessException e3){
            throw new MvcException("Could not access constructor of Controller <"+controller.getCanonicalName()+">",e3);
        }
    }


    private void invokeControllerActionMethod( Class controller, Method actionMethod,  boolean withParams)
    throws InvocationTargetException, IllegalAccessException, HiException {

        Object instance;
        HiCDI.shouldHaveCDIScope(controller);

        try {

            instance = CDI.current().select(controller).get();

        }catch (Throwable ex){

            throw new MvcException("Injection of controller <"+controller.getCanonicalName()+"> failed",ex);

        }

        Map<String,Object> arguments = null;

        ControllerRequestEvent call = new ControllerRequestEvent();
        call.setMethod(actionMethod);
        call.setClazz(controller);
        call.setBefore();
        if(withParams) {
            arguments = getValues(requestContext.getRequest());
            call.setArguments(arguments);
        }

        //Before action Event
        controllerRequestEvent.fire(call);

        if(call.wasInterrupted())
            return;

        if(withParams)
            actionMethod.invoke(instance,arguments);
        else
            actionMethod.invoke(instance);

        //After action Event
        call.setAfter();
        controllerRequestEvent.fire(call);

    }


    private static String noHyphens(String urlToken){

        if(urlToken==null)
            return null;

        char[] tokenChars = urlToken.toCharArray();
        StringBuilder hyphenLessToken = new StringBuilder();

        boolean capitalizeNext=false;
        for(char character:  tokenChars){
            if(capitalizeNext==true) {
                hyphenLessToken.append(Character.toUpperCase(character));
                capitalizeNext = false;
                continue;
            }

            if(character=='-') {
                capitalizeNext = true;
                continue;
            }

            hyphenLessToken.append(character);
        }

        return hyphenLessToken.toString();

    }


    @PostConstruct
    private void ready(){

        log = LoggerFactory.getLogger(MVCReqHandler.class);

    }



    @Produces
    public HTMLizer getHTMLizer(){

        HTMLizer htmLizer = HTMLizer.getInstance();
        htmLizer.setGsonBuilder(appContext.getGsonBuilder());
        return htmLizer;

    }


    public boolean handle(RequestContext requestContext) throws ServletException, IOException {
        this.requestContext = requestContext;
        String mvcUrl = requestContext.getRouteUrl();
        int indexSlash = mvcUrl.indexOf('/');
        if(indexSlash==-1)
            indexSlash = mvcUrl.length();

        String controller = mvcUrl.substring(0,indexSlash);
        requestContext.getData().put("controllerU",controller);
        controller = getControllerClassFromURLPart(controller);
        String action;
        if(indexSlash==mvcUrl.length())
            action = "index";
        else action = mvcUrl.substring(indexSlash+1,mvcUrl.length());

        requestContext.getData().put("actionU",action);
        action = getActionMethodFromURLPart(action);
        Class controllerClass= ControllersMapper.getInstance().findController(controller);
        if(controllerClass==null)
            return false;

        boolean actionFound = false;
        requestContext.getData().put("action",action);
        requestContext.getData().put("controller",controller);

        try {

            String language = frontEnd.getLanguage();
            if(I18nRuntime.isReady())
                I18nRuntime.get().setLanguage(language);

            actionFound = callAction(action, controllerClass, requestContext);

        }finally {

            if(I18nRuntime.isReady())
                I18nRuntime.get().unsetLanguage();

        }

        return actionFound;

    }

    private void renewCsrfToken(){



    }




}
