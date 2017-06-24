package org.emerjoin.hi.web.boot;

import org.emerjoin.hi.web.exceptions.HiException;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Mário Júnior
 */
public class BootManager {

    private static List<BootExtension> extensions = new ArrayList<>();

    static {

        ServiceLoader<BootExtension> extensionsSPI =  ServiceLoader.load(BootExtension.class);
        for(BootExtension e: extensionsSPI)
            extensions.add(e);

    }


    public static List<BootExtension> getExtensions() throws HiException {

        return extensions;

    }

}
