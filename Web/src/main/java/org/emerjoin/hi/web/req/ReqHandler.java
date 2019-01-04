package org.emerjoin.hi.web.req;

import org.emerjoin.hi.web.AuthComponent;
import org.emerjoin.hi.web.RequestContext;
import org.emerjoin.hi.web.meta.Denied;
import org.emerjoin.hi.web.meta.Granted;
import org.emerjoin.hi.web.meta.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mario Junior.
 */
public abstract class ReqHandler {

    public abstract boolean handle(RequestContext requestContext) throws ServletException, IOException;

    private static List<ReqHandler> reqHandlers = new ArrayList<ReqHandler>();
    private static Map<String,ReqHandler> handlers = new HashMap();
    private static Map<ReqHandler,Class> handlersClasses = new HashMap();

    private static Logger _log = LoggerFactory.getLogger(ReqHandler.class);

    public static void register(ReqHandler reqHandler,Class<? extends ReqHandler> clazz){

        reqHandlers.add(reqHandler);
        handlers.put(clazz.getCanonicalName(),reqHandler);
        handlersClasses.put(reqHandler,clazz);

    }

    public static ReqHandler getHandler(String className){

        if(handlers.containsKey(className)){

            return handlers.get(className);

        }

        return null;

    }

    public static Class<? extends ReqHandler> getHandlerClass(ReqHandler reqHandler){

        return  handlersClasses.get(reqHandler);

    }

    public static ReqHandler[] getAllHandlers(){

        ReqHandler[] allReqHandlers = new ReqHandler[reqHandlers.size()];
        reqHandlers.toArray(allReqHandlers);
        return allReqHandlers;

    }

    public static boolean matches(RequestContext requestContext, Class<? extends ReqHandler> reqHandler, boolean post) throws ReqMatchException{
        Annotation annotation = reqHandler.getDeclaredAnnotation(HandleRequests.class);
        if(annotation==null){
            throw new ReqMatchException(reqHandler.getCanonicalName(),"handler <"+reqHandler.getCanonicalName()+"> is not annoted");
        }
        String url = requestContext.getRouteUrl();
        HandleRequests handleRequests = (HandleRequests) annotation;
        if(post){
            if(!handleRequests.supportPostMethod()){
                return false;
            }
        }
        String regex = handleRequests.regexp();
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            return matcher.matches();
        }catch (Exception ex){

            throw new ReqMatchException(reqHandler.getCanonicalName(), "handler <"+reqHandler.getCanonicalName()+"> has an invalid match regular expression");

        }
    }

    private static boolean checkPermission(Granted granted, RequestContext requestContext){
        if(granted.value().length==0)
            return true;
        boolean allowed = false;
        for(String role : granted.value()){
            if(requestContext.getRequest().isUserInRole(role)){
                allowed = true;
                break;
            }
        }
        return allowed;
    }



    private static boolean validateAccess(Annotation grantedAnnotation,
                                          Annotation deniedAnnotation, Annotation requirePermissionAnnotation){

        boolean accessGranted = true;

        AuthComponent authComponent = null;

        try {

            authComponent = CDI.current().select(AuthComponent.class).get();

        }catch (Throwable ex){

            _log.error(String.format("Failed to get a %s instance",AuthComponent.class.getSimpleName()),ex);
            return accessGranted;

        }

        if(grantedAnnotation==null&&deniedAnnotation==null&&requirePermissionAnnotation==null)
            return accessGranted;

        if(grantedAnnotation!=null){

            accessGranted =  authComponent.isUserInAnyOfThisRoles(((Granted) grantedAnnotation).value());

        }else if(deniedAnnotation!=null){

            accessGranted = !authComponent.isUserInAnyOfThisRoles(((Denied) deniedAnnotation).value());

        }else if(requirePermissionAnnotation!=null){

            accessGranted =  authComponent.doesUserHavePermission(
                    ((RequirePermission) requirePermissionAnnotation).value());

        }

        return accessGranted;

    }

    protected static boolean accessGranted(AnnotatedElement annotatedElement){

        Annotation grantedAnnotation = annotatedElement.getAnnotation(Granted.class);
        Annotation deniedAnnotation = annotatedElement.getAnnotation(Denied.class);
        Annotation requirePermissionAnnotation = annotatedElement.getAnnotation(RequirePermission.class);

        if(grantedAnnotation==null&&deniedAnnotation==null&&requirePermissionAnnotation==null)
            return true;

        return validateAccess(grantedAnnotation,deniedAnnotation,requirePermissionAnnotation);



    }

    protected static boolean accessGranted(Class clazz, Method method){

        return accessGranted(clazz)&& accessGranted(method);

    }

}
