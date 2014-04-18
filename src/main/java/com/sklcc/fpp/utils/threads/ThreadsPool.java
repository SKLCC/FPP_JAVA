package com.sklcc.fpp.utils.threads;

import java.util.concurrent.TimeUnit;

public class ThreadsPool {
    
    private static ThreadPoolExecutor executor = null;

    public static ThreadPoolExecutor getInstance() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(2000, // core size
                                              5000, // max size
                                              60, // after 60h thread die
                                              TimeUnit.HOURS,
                                              new TaskQueue());
        }
        return executor;
    }
}
