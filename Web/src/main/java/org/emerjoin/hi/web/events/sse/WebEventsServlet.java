package org.emerjoin.hi.web.events.sse;

import org.emerjoin.hi.web.ActiveUser;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.security.SecureTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Mario Junior.
 */
@WebServlet(urlPatterns = {"/event-stream"}, asyncSupported = true)
public class WebEventsServlet extends HttpServlet {

    private static final long serialVersionUID = -2827663265593547983L;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebEventsServlet.class);

    @Inject
    private ActiveUser activeUser;

    @Inject
    private WebEventsController webEventsController;

    private SecureTokenUtil tokenUtil = new SecureTokenUtil();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        if(!accessCheck(request,response))
            return;
        final AsyncContext asyncContext = request.startAsync(request, response);
        asyncContext.setTimeout(0);
        WebEventsListener webEventsListener = webEventsController
                .addListener(activeUser, asyncContext);
        AppConfigurations configurations = AppConfigurations.get();
        webEventsListener.setReconnectInterval(configurations.getEventsConfig().
                getReconnectInterval());
    }


    private boolean accessCheck(HttpServletRequest request, HttpServletResponse response) throws  IOException {
        String token = request.getParameter("token");
        if(token==null||token.isEmpty()){
            LOGGER.warn("Rejecting connection request without token...");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
        //Token expired
        if(!activeUser.getEventsToken().equals(token)){
            if(tokenUtil.checkJwtToken(token)){
                LOGGER.info("Listener authenticating with expired token.");
                AsyncContext context = request.startAsync(request, response);
                WebEventsListener eventsListener = new WebEventsListener(context);
                eventsListener.setReconnectInterval(60000*600);
                ContentExpiredEvent contentExpiredEvent = new ContentExpiredEvent();
                eventsListener.deliver(contentExpiredEvent);
                LOGGER.info("ContentExpiredEvent sent to Listener!");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                }finally {
                    LOGGER.info("Listener rejected!");
                    eventsListener.dispose();
                }
                return false;
            }else{
                LOGGER.warn("Listener provided an Invalid token. Rejecting...");
                response.sendError(HttpServletResponse.
                        SC_BAD_REQUEST);
            }
        }

        return true;

    }

}
