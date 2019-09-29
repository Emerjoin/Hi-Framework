package org.emerjoin.hi.web.util;

/**
 * @author Mário Júnior
 */
public class Timing {

    private static ThreadLocal<Long> tickTime = new ThreadLocal<>();

    public static void tic(){

        tickTime.set(System.nanoTime());

    }

    public static double toc(){

        long start_time = tickTime.get();
        tickTime.remove();
        long end_time = System.nanoTime();
        return  (end_time - start_time)/1e6;

    }

}
