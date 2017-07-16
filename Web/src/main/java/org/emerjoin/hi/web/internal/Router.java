package org.emerjoin.hi.web.internal;

import org.emerjoin.hi.web.RequestContext;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.req.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Mário Júnior
 */
@ApplicationScoped
public class Router {


    private HashMap<String,String> matchedUrls = new HashMap();
    private static Logger _log = LoggerFactory.getLogger(Router.class);

    public void init(ServletContext context, ServletConfig config) throws HiException {

        if(AppConfigurations.get()!=null){

            ReqHandler.register(CDI.current().select(MVCReqHandler.class).get(),MVCReqHandler.class);
            ReqHandler.register(CDI.current().select(WebrootReqHandler.class).get(),WebrootReqHandler.class);
            ReqHandler.register(CDI.current().select(ES5ReqHandler.class).get(),ES5ReqHandler.class);
            ReqHandler.register(CDI.current().select(FrontiersReqHandler.class).get(),FrontiersReqHandler.class);
            ReqHandler.register(CDI.current().select(TestsReqHandler.class).get(),TestsReqHandler.class);
            ReqHandler.register(CDI.current().select(TestFilesReqHandler.class).get(),TestFilesReqHandler.class);

            try {

                ES5ReqHandler.prepareTemplates(context);


            }catch (ServletException ex){

                throw new HiException("Failed to prepare templates",ex);

            }
        }

    }

    private  boolean wasPreviouslyMatched(String route){

        boolean wasIt = false;

        synchronized (matchedUrls){

            wasIt = matchedUrls.containsKey(route);

        }

        return wasIt;

    }

    private synchronized String getPreviouslyMatchedHandler(String route){

        String previousHandler = null;

        synchronized (matchedUrls){

            previousHandler = matchedUrls.get(route);

        }

        return previousHandler;

    }

    private synchronized void storeMatchedUrl(String routeURL, Class<? extends ReqHandler> clazz){

        synchronized (matchedUrls){

            matchedUrls.put(routeURL,clazz.getCanonicalName());

        }

    }

    public int doRoute(RequestContext requestContext, String routeURL, boolean isPost) throws ServletException, IOException{

        boolean handled = false;

        if(wasPreviouslyMatched(routeURL)){

            ReqHandler reqHandler = ReqHandler.getHandler(getPreviouslyMatchedHandler(routeURL));
            reqHandler.handle(requestContext);
            return 200;

        }


        ReqHandler[] reqHandlers  = ReqHandler.getAllHandlers();
        for(ReqHandler reqHandler : reqHandlers){

            try {

                Class handlerClazz = ReqHandler.getHandlerClass(reqHandler);

                if (ReqHandler.matches(requestContext, handlerClazz,isPost)){
                    //_log.debug(String.format("Request handler match : %s",handlerClazz.getSimpleName()));
                    handled = reqHandler.handle(requestContext);

                    if(handled){

                        storeMatchedUrl(routeURL,handlerClazz);

                        break;

                    }

                }

            }catch (ServletException ex){

                requestContext.getResponse().sendError(500);
                throw ex;

            }catch (Exception ex){

                requestContext.getResponse().sendError(500);
                throw new ServletException(ex);
            }

        }

        if(!handled){

            requestContext.getResponse().sendError(404);
            return 404;

        }

        return 200;

    }



}
