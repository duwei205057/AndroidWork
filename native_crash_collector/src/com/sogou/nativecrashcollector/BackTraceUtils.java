package com.sogou.nativecrashcollector;

import android.os.Process;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by dw on 18-11-19.
 */

public class BackTraceUtils implements BackTraceService {

    private static final boolean DEBUG = true;
//    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static volatile BackTraceUtils mInstance ;

    private ConcurrentHashMap<Long,Future<String>> mBackTraceInfo;
    private ExecutorService  mExeService;
    private ThreadLocal<FutureTask> mThreadLocal;

    public static BackTraceUtils getInstance () {
        if (mInstance == null)
            synchronized (BackTraceUtils.class) {
                if (mInstance == null) {
                    mInstance = new BackTraceUtils();
                }
            }
        return mInstance;
    }

    private BackTraceUtils() {
        mBackTraceInfo = new ConcurrentHashMap<>();
        mThreadLocal = new InnerThreadLocal();
        mExeService = new InnerThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new InnerThreadFactory());
    }

    public void register() {
        try {
            final Thread current = Thread.currentThread();
            LOGD("register() mExeService.isShutdown()="+mExeService.isShutdown()+" mExeService.isTerminated()="+mExeService.isTerminated());
            FutureTask future = mThreadLocal.get();
            mExeService.execute(future);
            mBackTraceInfo.put(current.getId(), future);
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void withdraw() {
        Thread current = Thread.currentThread();
        LOGD(" withdraw----------------------");
        Future<String> future = mBackTraceInfo.get(current);
        if (future != null) future.cancel(true);
        mBackTraceInfo.remove(current.getId());
    }

    public String getBackTrace(Thread thread) {
        if (thread == null) return "";
        Future<String> future = mBackTraceInfo.get(thread.getId());
        LOGD( "BackTraceUtils  getBackTrace() future:"+future);
        if (future != null)
            try {
                return future.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (CancellationException e) {
                e.printStackTrace();
            }
        return "";
    }

    class InnerThreadLocal extends ThreadLocal<FutureTask> {
        @Override
        protected FutureTask initialValue() {
            final Thread current = Thread.currentThread();
            FutureTask<String> future = new FutureTask<String>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    LOGD("start====================pid=="+ Process.myPid()+" thread name="+current.getName()+" current.isAlive()="+current.isAlive());
                    if (!current.isAlive()) return "";
                    StackTraceElement[] ste = current.getStackTrace();
                    StringBuilder sb = new StringBuilder();
                    for (StackTraceElement steone : ste ){
                        sb.append("\t at  "+steone.getClassName() + "." + steone.getMethodName() + "(" +steone.getClassName()+ ".java:" + steone.getLineNumber() + ")" + "\n");
                    }
                    LOGD( "end====================current.getName() :"+current.getName()+" "+sb.toString());
                    return sb.toString();
                }
            });
            return future;
        }
    }


    class InnerThreadFactory implements ThreadFactory {
        int threadNum = 0;
        @Override
        public Thread newThread(Runnable runnable) {
            final Thread result = new Thread(runnable, "back-trace-thread-" + threadNum);
            threadNum++;
            LOGD("/////////////////////////////////InnerThreadFactory newThread----------------------="+result.getName());
            return result;
        }
    }

    class InnerThreadPoolExecutor extends ThreadPoolExecutor {

        public InnerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            LOGD( "beforeExecute t=" + t + "----------------------r=" + r);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            LOGD( "afterExecute Throwable=" + t + "----------------------r=" + r);
            if (t == null && r instanceof Future<?>) {
                LOGD( "cannel:"+((Future<?>) r).isCancelled());
            }
        }
    }

    private void LOGD(String message) {
        if (DEBUG) Log.d("xx", message);
    }

}
