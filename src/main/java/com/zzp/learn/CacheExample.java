package com.zzp.learn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CacheExample {
  public static void constructGuavaCache(String[] args) {
    Cache<String,String> cache = CacheBuilder.newBuilder().build();
    cache.put("word","Hello Guava Cache");
    System.out.println(cache.getIfPresent("word"));
  }
  public static void setMaxSize(String[] args) {
    Cache<String,String> cache = CacheBuilder.newBuilder()
        .maximumSize(2)
        .build();
    cache.put("key1","value1");
    cache.put("key2","value2");
    cache.put("key3","value3");
    System.out.println("第一个值：" + cache.getIfPresent("key1"));
    System.out.println("第二个值：" + cache.getIfPresent("key2"));
    System.out.println("第三个值：" + cache.getIfPresent("key3"));
  }
  public static void expireAfterWriteOrAccess(String[] args) throws InterruptedException {
    Cache<String,String> expireAfterWriteCache = CacheBuilder.newBuilder()
        .maximumSize(2)
        .expireAfterWrite(3, TimeUnit.SECONDS)
        .build();
    Cache<String,String> expireAfterAccessCache = CacheBuilder.newBuilder()
        .maximumSize(2)
        .expireAfterAccess(3, TimeUnit.SECONDS)
        .build();
    expireAfterWriteCache.put("key1","value1");
    expireAfterAccessCache.put("accessKey1","accessValue1");
    int time = 1;
//    while(true) {
//      System.out.println("第" + time++ + "次取到key1的值为：" + expireAfterWriteCache.getIfPresent("key1"));
//      Thread.sleep(1000);
//    }
    while(true) {
      System.out.println("第" + time++ + "次取到key1的值为：" + expireAfterAccessCache.getIfPresent("accessKey1"));
      Thread.sleep(1000+ time*500);
    }
  }

  public static void weakValues(String[] args) throws InterruptedException {
    Cache<String,Object> cache = CacheBuilder.newBuilder()
        .maximumSize(2)
        .weakValues()
        .build();
    Object value = new Object();
    cache.put("key1",value);

    value = new Object();//原对象不再有强引用
    System.gc();
    System.out.println(cache.getIfPresent("key1"));
  }

  public static void invalidAll(String[] args) throws InterruptedException {
    Cache<String,String> cache = CacheBuilder.newBuilder().build();
    Object value = new Object();
    cache.put("key1","value1");
    cache.put("key2","value2");
    cache.put("key3","value3");

    List<String> list = new ArrayList<String>();
    list.add("key1");
    list.add("key2");

    cache.invalidateAll(list);//批量清除list中全部key对应的记录
    System.out.println(cache.getIfPresent("key1"));
    System.out.println(cache.getIfPresent("key2"));
    System.out.println(cache.getIfPresent("key3"));
  }

  public static void removalListener(String[] args) throws InterruptedException {
    RemovalListener<String, String> listener = new RemovalListener<String, String>() {
      public void onRemoval(RemovalNotification<String, String> notification) {
        System.out.println("[" + notification.getKey() + ":" + notification.getValue() + "] is removed!");
      }
    };
    Cache<String,String> cache = CacheBuilder.newBuilder()
        .maximumSize(3)
        .removalListener(listener)
        .build();
    Object value = new Object();
    cache.put("key1","value1");
    cache.put("key2","value2");
    cache.put("key3","value3");
    cache.put("key4","value3");
    cache.put("key5","value3");
    cache.put("key6","value3");
    cache.put("key7","value3");
    cache.put("key8","value3");
  }


  private static Cache<String,String> cache = CacheBuilder.newBuilder()
      .maximumSize(3)
      .build();

  //get方法可以保证调用多个callable时，只有一个callable被执行
  public static void getCacheElementOnlyOnce(String[] args) throws InterruptedException {

    new Thread(new Runnable() {
      public void run() {
        System.out.println("thread1");
        try {
          String value = cache.get("key", new Callable<String>() {
            public String call() throws Exception {
              System.out.println("load1"); //加载数据线程执行标志
              Thread.sleep(1000); //模拟加载时间
              return "auto load by Callable";
            }
          });
          System.out.println("thread1 " + value);
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    }).start();

    new Thread(new Runnable() {
      public void run() {
        System.out.println("thread2");
        try {
          String value = cache.get("key", new Callable<String>() {
            public String call() throws Exception {
              System.out.println("load2"); //加载数据线程执行标志
              Thread.sleep(1000); //模拟加载时间
              return "auto load by Callable";
            }
          });
          System.out.println("thread2 " + value);
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public static void recordStats(String[] args) throws InterruptedException {
    Cache<String,String> cache = CacheBuilder.newBuilder()
        .maximumSize(3)
        .recordStats() //开启统计信息开关
        .build();
    cache.put("key1","value1");
    cache.put("key2","value2");
    cache.put("key3","value3");
    cache.put("key4","value4");

    cache.getIfPresent("key1");
    cache.getIfPresent("key2");
    cache.getIfPresent("key3");
    cache.getIfPresent("key4");
    cache.getIfPresent("key5");
    cache.getIfPresent("key6");

    System.out.println(cache.stats()); //获取统计信息
  }


  public static void getFromOuterIfAbsent(String[] args) throws ExecutionException {
    CacheLoader<String, String> loader = new CacheLoader<String, String>() {
      public String load(String key) throws Exception {
        Thread.sleep(1000); //休眠1s，模拟加载数据
        System.out.println(key + " is loaded from a cacheLoader!");
        return key + "'s value";
      }
    };

    LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder()
        .maximumSize(3)
        .build(loader);//在构建时指定自动加载器

    loadingCache.get("key1");
    loadingCache.get("key2");
    loadingCache.get("key3");
  }
}
