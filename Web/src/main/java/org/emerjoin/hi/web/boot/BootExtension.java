package org.emerjoin.hi.web.boot;

import org.jboss.jandex.Index;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Set;

/**
 * @author Mário Júnior
 */
public interface BootExtension {

    public void boot(Set<Index> indexes, ServletContext servletContext) throws Exception;

}
