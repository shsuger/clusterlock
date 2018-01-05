package cluster.impl;

import cluster.ClusterLock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ClusterLockImpl implements ClusterLock {

	// 默认根目录
	private static String ROOTPATH = "/cluster/lock";

	private static Logger LOG = LoggerFactory.getLogger(ClusterLockImpl.class);

	public Locker transiantLock(String lockPath) {
		return realAcquire(lockPath, true);
	}

	public Locker lock(String lockPath) {
		return realAcquire(lockPath, false);
	}

	private Locker realAcquire(String lockPath, Boolean transiant) {
		CuratorFramework client = CuratorClientBuilder.getCuratorClient();
		lockPath = buildFullPath(lockPath);
		InterProcessMutex mutex = null;
		if (transiant) {
			mutex = new InterProcessMutex(client, lockPath);
		} else {
			// TODO 永久锁
			mutex = new InterProcessMutex(client, lockPath);
		}
		return new LockerImpl(mutex, transiant, lockPath);
	}

	/**
	 * <pre>
	 * 功能描述:构建路径
	 * @param lockPath
	 * @return 返回锁全路径
	 */
	private String buildFullPath(String lockPath) {
		return ROOTPATH + lockPath;

	}

	public static class LockerImpl implements Locker {
		InterProcessMutex mutex;
		Boolean transiant;
		String lockPath;

		public LockerImpl(InterProcessMutex mutex, Boolean transiant, String lockPath) {
			this.mutex = mutex;
			this.transiant = transiant;
			this.lockPath = lockPath;
		}

		public boolean acquire(long time, TimeUnit unit) throws Exception {
			return mutex.acquire(time, unit);
		}

		public <T> T exeucte(Executor<T> executor) throws Exception {
			LOG.info(Thread.currentThread().getName() + ":执行锁操作"+System.currentTimeMillis());
			try {
				return executor.execute();
			} catch (Exception e) {
				throw e;
			} finally {
				mutex.release();
				LOG.info(Thread.currentThread().getName() + ":释放锁"+System.currentTimeMillis());
				if (transiant) {
					try {
						CuratorClientBuilder.getCuratorClient().delete().guaranteed().forPath(lockPath);
						LOG.info(Thread.currentThread().getName() + ":删除zk节点"+System.currentTimeMillis());
					} catch (Exception e) {
						//e.printStackTrace();
						LOG.info(Thread.currentThread().getName() + ":删除zk节点失败"+System.currentTimeMillis());
					}
				}

			}
		}

	}
	static int aaa= 1;
	public static void main(String[] args) {
		ExecutorService services = Executors.newCachedThreadPool();
		for (int i = 0; i < 10; i++) {
			services.submit(new TaskThread("线程-" + i));
		}

	}

	public static class TaskThread extends Thread {

		public TaskThread(String name) {
			super(name);

		}

		@Override
		public void run() {
			/*
			 * 1.客户端连接zookeeper，并在/test下创建临时的且有序的子节点，第一个客户端对应的子节点为/test/lock-0000000000，
			 *   第二个为/test/lock-0000000001，以此类推。
			 * 2.客户端获取/test下的子节点列表，判断自己创建的子节点是否为当前子节点列表中序号最小的子节点，
			 *   如果是则认为获得锁，否则监听刚好在自己之前一位的子节点删除消息，获得子节点变更通知后重复此步骤直至获得锁；
			 * 3.执行业务代码
			 * 4.完成业务流程后，删除对应的子节点释放锁。
			 */

			Locker lock = new ClusterLockImpl().realAcquire("/test", true);
			try {
				//test节点下面的子节点可以等待10s的时间去监听其前一个子节点是否有释放锁，
				// 若超过10s仍有在等待的子节点，则这些子节点将无法获取锁；并会删除所有子节点
				if (lock.acquire(10, TimeUnit.SECONDS)) {
					//LOG.info(Thread.currentThread().getName() + "获取到资源");
					lock.exeucte(new ClusterLock.Executor<Boolean>() {
						public Boolean execute() {
							try {
								LOG.info("aaa="+aaa++);
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							return true;
						}
					});
				} else {
					LOG.info(Thread.currentThread().getName() + "没有获取到资源");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

	}

}
