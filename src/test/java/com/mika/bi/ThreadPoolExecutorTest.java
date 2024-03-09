package com.mika.bi;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

@Slf4j
public class ThreadPoolExecutorTest {

    @Test
    void testPool(){
        ThreadFactory threadFactory = new ThreadFactory() {

            private  int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程"+count);
                count++;
                return thread;
            }
        };

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,3,10,
                TimeUnit.SECONDS,new ArrayBlockingQueue<>(20),threadFactory);

        String s = "1";
        class A{
            int a = 1;

            int getA(){
                return a++;
            }
        }
        A a = new A();
        for(int i = 0 ; i< 23; i++){
            CompletableFuture.runAsync(()->{

                log.info("{},任务:{}",Thread.currentThread().getName(),a.getA());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            },threadPoolExecutor);

        }
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
