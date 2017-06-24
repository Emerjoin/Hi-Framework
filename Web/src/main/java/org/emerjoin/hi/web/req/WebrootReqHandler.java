package org.emerjoin.hi.web.req;

import org.emerjoin.hi.web.RequestContext;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.Tunings;
import org.apache.tika.Tika;
import org.emerjoin.hi.web.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Mario Junior.
 */
@HandleRequests(regexp = "(webroot[A-Za-z0-9_\\/.-]+[.][A-Za-z]{1,10}|[A-Za-z_\\/-]+\\/webroot[A-Za-z0-9_\\/.-]+[.][A-Za-z]{1,10})\\w+")
@ApplicationScoped
public class WebrootReqHandler extends ReqHandler {

    private RequestContext requestContext = null;
    private HashMap<String,Long> cachingStatus = new HashMap<>();

    private static Logger _log = LoggerFactory.getLogger(WebrootReqHandler.class);

    @Inject
    private AppContext appContext;

    public WebrootReqHandler(){}

    private void avoidCaching(RequestContext requestContext){
        requestContext.getResponse()
                .setHeader("Cache-Control", "no-cache");
    }

    private String decideCaching(String assetUrl){

        if(AppConfigurations.get().getDeploymentMode()== AppConfigurations.DeploymentMode.DEVELOPMENT) {
            avoidCaching(requestContext);
            return assetUrl;
        }


        Tunings tunings = AppConfigurations.get().getTunings();
        Tunings.CachingDecision decision = tunings.decision(assetUrl);

        if(decision.getMode()== Tunings.CachingMode.NoCaching) {
            if(_log.isDebugEnabled())
                _log.debug("[No cache] : "+assetUrl);
            avoidCaching(requestContext);
            return assetUrl;

        }else if(decision.getMode()== Tunings.CachingMode.FixedCaching){
            if(_log.isDebugEnabled())
                _log.debug("[Fixed cache] : "+decision.getResourcePath());
            tunings.emmitFixedCachingHeaders(decision,requestContext);
            return decision.getResourcePath();
        }

        if(_log.isDebugEnabled())
            _log.debug("[Smart cache] : "+assetUrl);
        tunings.emmitSmartCachingHeaders(requestContext);
        return decision.getResourcePath();

    }

    private void writeAssetOut(URL assetURL,String mime,OutputStream outputStream) throws  IOException{

        InputStream fileStream = assetURL.openStream();
        outputStream = requestContext.getOutputStream();
        requestContext.getResponse().setHeader("Content-Type", mime);

        int fileSize = fileStream.available();
        requestContext.getResponse().setHeader("Content-Length", String.valueOf(fileSize));

        while (fileStream.available() > 0) {

            byte[] buffer = new byte[4048];
            if (fileStream.available() < 4048)
                buffer = new byte[fileStream.available()];

            int totalRead = fileStream.read(buffer);
            if (totalRead != buffer.length)
                buffer = Arrays.copyOf(buffer, totalRead);

            outputStream.write(buffer);

        }

    }


    public boolean handle(RequestContext requestContext) throws ServletException, IOException {

        this.requestContext = requestContext;
        String assetUrl = requestContext.getRequest().getRequestURI().replace(requestContext.getRequest().getContextPath()+"/","");
        int indexAssetsSlash = assetUrl.lastIndexOf("webroot/");
        assetUrl = assetUrl.substring(indexAssetsSlash,assetUrl.length());

        try {

            assetUrl = decideCaching(assetUrl);
            URL assetURL = requestContext.getServletContext().getResource("/"+assetUrl);

            if(assetURL==null){
                _log.warn("Web resource could not be located : "+assetUrl);
                return false;
            }

            String mime = new Tika().detect(assetURL);
            if(mime==null)
                return false;

            writeAssetOut(assetURL,mime,requestContext.getOutputStream());

        }catch (Throwable ex){

            requestContext.getResponse().sendError(500);

        }

        return true;

    }



}
