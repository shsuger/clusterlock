package cluster;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * 
 * @author bernie.xu
 * @Date: 2015年12月24日 下午1:55:37
 */
public interface ClusterLock {

	public Locker transiantLock(String lockPath) throws Exception;

	public Locker lock(String lockPath) throws Exception;

	public static interface Executor<T> {
		public T execute();
	}

	public static interface Locker {

		public boolean acquire(long time, TimeUnit unit) throws Exception;

		public <T> T exeucte(ClusterLock.Executor<T> executor) throws Exception;
	}
}
