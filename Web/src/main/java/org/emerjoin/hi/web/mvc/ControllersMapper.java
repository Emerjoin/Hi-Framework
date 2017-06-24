package org.emerjoin.hi.web.mvc;

import org.emerjoin.hi.web.exceptions.HiException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class ControllersMapper {

    private static ControllersMapper instance;
    private static Map<String,Class> controllers = new HashMap<>();


    public static boolean isControllerNameTaken(Class<? extends Controller> clazz){

        return controllers.containsKey(clazz.getSimpleName());

    }

    public static void map(Class<? extends Controller> clazz) throws HiException {

        if(isControllerNameTaken(clazz))
            throw new HiException("Ambiguous controller detected : "+clazz.getSimpleName());

        controllers.put(clazz.getSimpleName(),clazz);

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

}
