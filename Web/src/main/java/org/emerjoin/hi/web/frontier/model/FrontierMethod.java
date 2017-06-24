package org.emerjoin.hi.web.frontier.model;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mario Junior.
 */
public class FrontierMethod {

    private String name;
    private HashMap<String,MethodParam> paramsMap = new HashMap();
    private List<MethodParam> paramList = new ArrayList<MethodParam>();
    private Method method;

    public FrontierMethod(String name, Method method){
        this.name = name;
        this.method = method;

    }


    public MethodParam[] getParams(){

        MethodParam methodParam[] = new MethodParam[paramsMap.size()];
        paramList.toArray(methodParam);
        return methodParam;

    }

    public void addParam(MethodParam param){

        paramsMap.put(param.getName(),param);
        paramList.add(param);

    }

    public boolean hasParam(String name){

        return paramsMap.containsKey(name);

    }

    public MethodParam getParam(String name){


        return paramsMap.get(name);

    }

    public String getName() {

        return name;

    }

    public Method getMethod() {
        return method;
    }
}
