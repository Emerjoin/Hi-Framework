package org.emerjoin.hi.web.mvc;

import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.meta.Template;
import org.emerjoin.hi.web.req.MVCReqHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class ControllersMapper {

    private static ControllersMapper instance;
    private static Map<String,Class> controllers = new HashMap<>();
    private static Map<String,String> pathTemplatesMappings = new HashMap<>();


    private static Logger LOG = LoggerFactory.getLogger(ControllersMapper.class);


    public static boolean isControllerNameTaken(Class<? extends Controller> clazz){

        return controllers.containsKey(clazz.getSimpleName());

    }

    private static void loadTemplateMappings(Class controller){

        String template = null;
        Annotation annotation = controller.getAnnotation(Template.class);
        if(annotation!=null){
            template = ((Template) annotation).value();
        }

        Method[] methods = controller.getDeclaredMethods();
        String mappedTemplate = template;
        for(Method method : methods){

            Template mapping = method.getAnnotation(Template.class);
            if(mapping!=null)
                mappedTemplate = mapping.value();

            String ctrl = MVCReqHandler.getURLController(controller.getSimpleName());
            String action = MVCReqHandler.getURLAction(method.getName());
            String path = String.format("%s/%s",ctrl,action);

            if(mappedTemplate!=null){
                LOG.info(String.format("Path [%s] => [%s] template.",path,mappedTemplate));
                pathTemplatesMappings.put(path,mappedTemplate);
            }

        }


    }

    public static void map(Class<? extends Controller> clazz) throws HiException {

        if(isControllerNameTaken(clazz))
            throw new HiException("Ambiguous controller detected : "+clazz.getSimpleName());

        controllers.put(clazz.getSimpleName(),clazz);
        loadTemplateMappings(clazz);

    }

    private ControllersMapper(){

        super();

    }

    public static ControllersMapper getInstance(){

        if(instance==null){

            instance = new ControllersMapper();

        }

        return instance;

    }


    public Class findController(String name){


        String capitalName = name.substring(0, 1).toUpperCase() + name.substring(1);
        return controllers.get(capitalName);

    }

    public static String getPathTemplate(String path){

        if(pathTemplatesMappings.containsKey(path))
            return pathTemplatesMappings.get(path);

        return null;

    }

}
