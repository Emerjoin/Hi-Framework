package org.emerjoin.hi.web;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.ConfigProvider;
import org.emerjoin.hi.web.boot.BootAgent;
import org.emerjoin.hi.web.internal.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.emerjoin.hi.web.uti.Timing.*;


/**
 * Created by Mario Junior.
 */
@WebServlet(urlPatterns = "/*",name = "Hi-Framework-Dispatcher-Servlet",loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {

    private static Logger _log = LoggerFactory.getLogger(DispatcherServlet.class);

    @Inject
    private BootAgent bootAgent;

    @Inject
    private Router router;

    @Inject
    private ConfigProvider configProvider;

    private String filterRouteURL(String routeURL,HttpServletResponse response) throws IOException {

        if(routeURL.trim().length()==0){

            if(AppConfigurations.get().getWelcomeUrl()!=null) {
                response.sendRedirect(AppConfigurations.get().getWelcomeUrl());
                return null;
            }

        }

        return routeURL;

    }


    private String getRouteURL(HttpServletRequest request){
        String requestURI = request.getRequestURI();
        if(request.getContextPath().isEmpty()){
            return requestURI.substring(1, requestURI.length());
        }
        return request.getRequestURI().replace(request.getContextPath()+"/",
                "");
    }


    private void doHandle(HttpServletRequest request, HttpServletResponse response, boolean isPost) throws ServletException,IOException{

        int status = 200;

        try {

            tic();

            AppContext appContext = CDI.current().select(AppContext.class).get();
            if(!appContext.isBaseURLSet()) {
                //Automatically set base URL
                String computedBaseUrl = computeRequestBaseURL(request);
                _log.info("Computed base URL: "+computedBaseUrl);
                appContext.setBaseURL(computedBaseUrl);
            }

            request.getSession(true);
            String routeURL = getRouteURL(request);
            _log.debug("Route URL: "+routeURL);

            routeURL = filterRouteURL(routeURL, response);
            if (routeURL == null)
                return;

            RequestContext requestContext = CDI.current().select(RequestContext.class).get();
            requestContext.setRouteUrl(routeURL);
            requestContext.setResponse(response);

            status = router.doRoute(requestContext, routeURL, isPost);

        }catch (ServletException | IOException ex){

            status = 500;
            throw ex;

        }finally {

            _log.debug(String.format("[%d] %s  => took %f millisecs",status,request.getRequestURI(),toc()));

        }

    }

    public String computeRequestBaseURL(HttpServletRequest request){
        StringBuilder builder = new StringBuilder();
        builder.append(request.getScheme());
        builder.append(':');
        builder.append("//");
        builder.append(request.getServerName());
        int serverPort = request.getServerPort();
        if(serverPort!=80&&serverPort!=443) {
            builder.append(':');
            builder.append(serverPort);
        }
        String contextPath = request.getContextPath();
        if(!contextPath.isEmpty())
            builder.append(contextPath);
        builder.append('/');
        return builder.toString();
    }


    public void init() throws ServletException{

        _log.info("---Hi-Framework is Booting up...");
        bootAgent.init(getServletContext(),getServletConfig());
        _log.info("---Boot complete!");

    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        doHandle(request,response,true);

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doHandle(request,response,false);

    }





}
