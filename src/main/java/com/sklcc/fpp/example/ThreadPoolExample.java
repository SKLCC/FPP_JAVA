package com.sklcc.fpp.example;

import java.util.concurrent.TimeUnit;

import com.sklcc.fpp.utils.threads.ThreadPoolExecutor;
import com.sklcc.fpp.utils.threads.ThreadsPool;

public class ThreadPoolExample {

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = ThreadsPool.getInstance();
        threadPoolExecutor.execute(new Runnable() {
            public void run() {
                System.out.println("fuck");
            }
        });

        threadPoolExecutor.execute(new Runnable() {
            public void run() {
                System.out.println("Waiting...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("OK");
            }
        }, 300, TimeUnit.MILLISECONDS); // it means if the task queue is full
                                        // and no thread available
                                        // the runnable will wait for 300ms and
                                        // exit

        threadPoolExecutor.shutdown();// close the threadpool, otherwise the
                                      // threadpool will wait not exit
    }

}
