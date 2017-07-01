package org.emerjoin.hi.web.frontier;

import org.emerjoin.hi.web.internal.ES5Library;
import org.emerjoin.hi.web.internal.Logging;
import org.emerjoin.hi.web.meta.MultipleCalls;
import org.emerjoin.hi.web.meta.SingleCall;
import org.emerjoin.hi.web.frontier.model.FrontierClass;
import org.emerjoin.hi.web.frontier.model.FrontierMethod;
import org.emerjoin.hi.web.frontier.model.MethodParam;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class FrontiersGenerator {

    private class FrontierFunction{

        private Map<String,String> params = new HashMap<>();
        private String signature="";
        private String body = "";

        private void setParam(String name, String value){

            params.put(name,value);

        }

        private String get(String name){

            if(!params.containsKey(name))
                return "undefined";

            return params.get(name);

        }

        private FrontierFunction(){

            setParam("_$fmut","undefined");
            setParam("_$abpon","undefined");
            setParam("_$abpnew","undefined");
            setParam("_$si_method","undefined");
            setParam("_$si_params","undefined");

        }

    }

    private String genericFrontierJS;
    private Logger log = Logging.getInstance().getLogger(FrontiersGenerator.class);
    private java.util.Base64.Encoder encoder = Base64.getEncoder();

    public FrontiersGenerator(){

        ready();

    }

    private String generateUrl(String beanName, String method){

        return "jbind:"+encoder.encodeToString((beanName+"/"+method).getBytes());

    }

    private FrontierFunction generateSignatureAndObject(FrontierMethod method){

        FrontierFunction function = new FrontierFunction();

        StringBuilder signature = new StringBuilder();
        signature.append("(");

        StringBuilder methodBody = new StringBuilder();
        java.security.SecureRandom secureRandom = new SecureRandom();
        String token = String.valueOf(secureRandom.nextLong());

        methodBody.append("var params = {");
        MethodParam[] parameters = method.getParams();

        int index = 0;
        for(MethodParam parameter : parameters){

            String paramName = parameter.getName();
            signature.append(paramName);
            methodBody.append("\""+paramName+"\":"+paramName);
            //Not last item
            if(index!=parameters.length-1) {
                signature.append(",");
                methodBody.append(",");
            }
            index++;

        }

        methodBody.append("};");
        signature.append(")");
        function.signature = signature.toString();
        function.body = methodBody.toString();

        Annotation mInvocations = method.getMethod().getAnnotation(MultipleCalls.class);
        if(mInvocations!=null){
            function.setParam("_$fmut","\""+token+"\"");
            function.setParam("_$mi","true");
            function.setParam("_$si","false");

        }else handleSingleInvocation(token,function,method);
        return function;

    }

    private void handleSingleInvocation(String token, FrontierFunction function, FrontierMethod method ){

        Annotation sInvocation = method.getMethod().getAnnotation(SingleCall.class);
        function.setParam("_$si","true");
        function.setParam("_$mi","false");

        if(sInvocation!=null){
            SingleCall singleCall = (SingleCall) sInvocation;

            if(singleCall.detectionMethod()== SingleCall.Detection.METHOD_CALL){
                function.setParam("_$fmut","\""+token+"\"");
                function.setParam("_$si_method","true");
                function.setParam("_$si_params","false");
            }else{
                function.setParam("_$fmut","\""+token+"\"+JSON.stringify(params).trim();");
                function.setParam("_$si_params","true");
                function.setParam("_$si_method","false");
                function.setParam("_$si_params","true");
                function.setParam("_$si_method","false");
            }

            if(singleCall.abortionPolicy()== SingleCall.AbortPolicy.ABORT_NEW_INVOCATION){
                function.setParam("_$abpnew","true");
                function.setParam("_$abpon","false");
            }else{
                function.setParam("_$abpnew","false");
                function.setParam("_$abpon","true");
            }

        }else{

            function.setParam("_$fmut","\""+token+"\"");
            function.setParam("_$abpon","true");
            function.setParam("_$abpnew","false");
            function.setParam("_$si_method","true");
            function.setParam("_$si_params","false");
        }

    }

    private String generateMethodMirror(FrontierMethod method,String beanName){

        FrontierFunction function = generateSignatureAndObject(method);
        function.setParam("$functionUrl","App.base_url+\""+generateUrl(beanName,method.getName())+"\"");

        StringBuilder code = new StringBuilder();
        code.append("function "+function.signature+"{");
        code.append(function.body);
        code.append(generateWrapper(function));
        code.append("};");

        return code.toString();

    }

    private String generateWrapper(FrontierFunction f){

        return String.format("return fMx(%s,%s,%s,%s,%s,%s,%s);",
                "params",
                f.get("$functionUrl"),
                f.get("_$fmut"),
                f.get("_$si"),
                f.get("_$si_method"),
                f.get("_$abpon"),
                "arguments");

    }

    private void ready(){

        this.genericFrontierJS = CDI.current().select(ES5Library.class)
                .get().getGenericFrontierJS();

    }

    public String generate(FrontierClass frontierClass){

        try {

            FrontierMethod[] methods = frontierClass.getMethods();
            StringBuilder script = new StringBuilder();
            script.append("var "+frontierClass.getSimpleName()+"={};");

            for(FrontierMethod method : methods){

                String methodName = method.getName();
                String generatedMirrorScript = generateMethodMirror(method,frontierClass.getSimpleName());
                script.append(frontierClass.getSimpleName()+"."+methodName+"="+generatedMirrorScript);

            }

            return script.toString();

        }catch (Throwable ex){
            log.error(String.format("Failed to generate frontier mirror script for class %s",frontierClass.getClassName()),ex);
            return null;
        }

    }

}
