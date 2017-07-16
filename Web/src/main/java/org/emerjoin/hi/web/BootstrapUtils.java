package org.emerjoin.hi.web;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mário Júnior
 */
public class BootstrapUtils {

    private static Set<URL> libraries = null;
    private static Set<URL> classFiles = null;
    private static Set<Index> indexSet = null;
    private static Logger log = LoggerFactory.getLogger(BootstrapUtils.class);
    public static boolean DISABLE_SERVLET_CONTEXT_SCANNING = false;

    private static void indexClassURLs(Set<URL> classes, Indexer indexer, ServletContext context){

        for(URL classURL : classes)
            indexClassURL(classURL,indexer,context);

    }

    private static void indexClassURL(URL classURL, Indexer indexer, ServletContext context){

        try {

            if(!isClassFile(classURL))
                return;

            indexer.index(classURL.openStream());

        }catch (Throwable ex){

            log.error(String.format("Error indexing class: %s",classURL.toString()),ex);
            return;

        }

    }


    private static boolean isDirectory(URL url){

        return url.toString().endsWith("/")&&url.getProtocol().equals("file");

    }


    private static void indexDirectory(File directory, Indexer indexer, ServletContext context){

        File[] files = directory.listFiles();
        if(files==null)
            return;


        for(File file : files){

            if(file.isDirectory()) {
                indexDirectory(file, indexer,context);
                continue;
            }

            try {

                indexClassURL(file.toURI().toURL(),indexer,context);

            }catch (IOException ex){



            }

        }

    }

    private static void indexDirectoryURL(URL url, Indexer indexer,ServletContext context){

        if(!isDirectory(url))
            return;


        File directory = new File(url.getFile());
        if(!directory.isDirectory())
            return;

        indexDirectory(directory,indexer,context);


    }


    private static boolean isClassFile(URL url){

        String urlStr = url.toString();
        if(urlStr.length()<=6)
            return false;

        String extension = urlStr.substring(urlStr.length()-6,urlStr.length());
        if(!extension.equals(".class"))
            return false;

        return true;

    }

    private static boolean isJarFile(URL url){


        String urlStr = url.toString();
        if(urlStr.length()<=4)
            return false;

        String extension = urlStr.substring(urlStr.length()-4,urlStr.length());
        if(!extension.equals(".jar"))
            return false;

        return true;

    }

    private static void indexClassLoader(URLClassLoader classLoader, Indexer indexer, ServletContext context){

        for(URL url : classLoader.getURLs()) {

            log.debug(String.format("Indexing URL : %s",url.toString()));
            indexClassURL(url, indexer,context);
            indexJarURL(url, indexer,context);
            indexDirectoryURL(url,indexer,context);

        }

    }


    private static void indexJarURL(URL jarURL, Indexer indexer,ServletContext context){

        try {

            if(!isJarFile(jarURL))
                return;

            File realFile = getFile(jarURL,context);
            File tempFile = File.createTempFile(realFile.getName(),"jandex");
            Index index = JarIndexer.createJarIndex(realFile, indexer,tempFile,false, false, false).getIndex();
            indexSet.add(index);

            try {

                tempFile.delete();

            }catch (Exception ex){
                log.error("Failed to deleted Jandex temporary index file",ex);
            }

        }catch (Throwable ex){

            log.error(String.format("Error indexing lib %s",jarURL.toString()),ex);
            return;

        }


    }


    private static File getFile(URL url, ServletContext context){

        String urlString = url.toString();
        if(urlString.startsWith("jndi")) {

            String realPath = context.getRealPath(urlString);
            if(!realPath.contains("jndi:"))
                return new File(context.getRealPath(urlString));

            String[] pieces = realPath.split("/jndi:");// foo/bar/bazz/jndi:/localhost/whatever/else/bazz/more/tokens
            if(pieces.length<2){

                new File(realPath);

            }

            String leftPiece = pieces[0]; // foo/bar/bazz/
            String leftPieceParts[] = leftPiece.split(File.separator);
            String lastToken = leftPieceParts[leftPieceParts.length-1];//bazz

            String rightPiece = pieces[1]; // localhost/whatever/else/bazz/more/tokens
            String[] rightPieceParts = rightPiece.split(lastToken);
            String realPathPiece = rightPieceParts[1]; // more/tokens

            String hardRealPath = leftPiece+realPathPiece; //foo/bar/more/tokens
            return new File(hardRealPath);

        }

        return new File(url.getFile());


    }


    private static Set<URL> getFilesContext(ServletContext context,String path,Set<URL> set){

        Set<String> libs = context.getResourcePaths(path);

        if(libs==null)
            return set;

        for(String filePath : libs) {

            try {

                //Directory
                if(filePath.substring(filePath.length()-1,filePath.length()).equals("/")){

                    getFilesContext(context,filePath,set);
                    continue;

                }


                URL resource = context.getResource(filePath);
                if(resource!=null)
                    set.add(resource);

            }catch (Exception ex){

                log.error(String.format("Error getting files in path %s",path),ex);

            }
        }

        return set;


    }

    private static Set<URL> getFiles(ServletContext context,String path){

        Set<URL> set = new HashSet<>();
        getFilesContext(context,path,set);
        return set;

    }


    public static Set<Index> getIndexes(ServletContext servletContext){

        if(indexSet==null){

            Indexer indexer = new Indexer();
            indexSet = new HashSet<>();

            ClassLoader classLoader = DispatcherServlet.class.getClassLoader();

            if( classLoader instanceof URLClassLoader && DISABLE_SERVLET_CONTEXT_SCANNING ){

                log.info("Indexing beans based in ClassLoader : Servlet context scanning disabled");
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                indexClassLoader(urlClassLoader,indexer,servletContext);
                indexSet.add(indexer.complete());
                return indexSet;

            }

            Set<URL> classes = getClassFiles(servletContext);
            if(classes!=null)
                indexClassURLs(classes,indexer,servletContext);

            Set<URL> libraries = getLibraries(servletContext);

            if(libraries!=null){
                for(URL libURL : libraries)
                    indexJarURL(libURL,indexer,servletContext);

            }

            indexSet.add(indexer.complete());

        }

        return indexSet;


    }

    public static Set<URL> getLibraries(ServletContext context){

        if(libraries==null)
            libraries = getFiles(context,"/WEB-INF/lib/");
        return libraries;

    }

    public static Set<URL> getClassFiles(ServletContext context){

        if(classFiles==null)
            classFiles = getFiles(context,"/WEB-INF/classes/");

        return  classFiles;

    }


}
