package org.emerjoin.hi.web.req;

import org.emerjoin.hi.web.Helper;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.RequestContext;
import org.emerjoin.hi.web.config.AppConfigurations;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mario Junior.
 */
@HandleRequests(regexp = "^test")
@ApplicationScoped
public class TestsReqHandler extends ReqHandler {


    @Override
    public boolean handle(RequestContext requestContext) throws ServletException, IOException {
        if(!requestContext.getRouteUrl().equals("test"))
            return false;
        URL testsHtml = requestContext.getServletContext().getResource("/webroot/tests/run.html");
        URL testsJs = requestContext.getServletContext().getResource("/webroot/tests/run.js");

        if(testsHtml==null||testsJs==null)
            throw new HiException("Could not invoke tests runner. Make sure the files /webroot/tests/run.html AND /webroot/tests/run.js are BOTH present");

        String hi_test_js = "<script src=\"hi-es5-tests.js\"></script>\n";
        String testsHtmlContent = Helper.readLines(testsHtml.openStream(),null);
        String bodyClose = "</body>";

        StringBuilder scriptTags = new StringBuilder();
        Map<String,Boolean> testFiles = AppConfigurations.get().getTestFiles();
        Set<String> testFilesSet = testFiles.keySet();
        for(String testFile : testFilesSet)
            scriptTags.append("<script src =\"test/"+testFile+"\"></script>\n");

        String bootstrapSnipet = "<script>Hi.$angular.run();</script>\n<script>angular.bootstrap($(document),['hi']);</script>\n";
        String appendedScript = hi_test_js+bootstrapSnipet+scriptTags;
        testsHtmlContent = testsHtmlContent.replace(bodyClose,appendedScript+bodyClose+"\n");
        requestContext.echo(testsHtmlContent);
        return true;

    }

}
