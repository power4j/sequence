package com.power4j.kit.seq.persistent.provider;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.power4j.kit.seq.utils.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class MySqlSynchronizerTest {

	private final static String SEQ_TABLE = "tb_seq";

	private final static String JDBC_URL = "jdbc:mysql://localhost:3306/seq_test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	private MySqlSynchronizer mySqlSynchronizer;

	private static DataSource getDataSource() {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("root");
		dataSource.setPassword("root");
		dataSource.setUrl(JDBC_URL);
		return dataSource;
	}

	@Before
	public void prepare() {
		mySqlSynchronizer = new MySqlSynchronizer(SEQ_TABLE, getDataSource());
		mySqlSynchronizer.createTable();
	}

	@After
	public void teardown() {
		mySqlSynchronizer.dropTable();
	}

	@Test
	public void simpleTest() {
		final String seqName = "power4j";
		final String seqGroup = LocalDateTime.now().toString();
		final long initValue = 1000L;
		final long newValue = 1L;
		mySqlSynchronizer.tryCreate(seqName, seqGroup, initValue);
		Optional<Long> consume = mySqlSynchronizer.getNextValue(seqName, seqGroup);
		Assert.assertTrue(consume.get().longValue() == initValue);

		boolean ret = mySqlSynchronizer.tryUpdate(seqName, seqGroup, -1, newValue);
		Assert.assertFalse(ret);

		ret = mySqlSynchronizer.tryUpdate(seqName, seqGroup, initValue, newValue);
		Assert.assertTrue(ret);

		consume = mySqlSynchronizer.getNextValue(seqName, seqGroup);
		Assert.assertTrue(consume.get().longValue() == newValue);
	}

	@Test
	public void threadsTest1() {
		final String seqName = "power4j";
		final String seqGroup = LocalDateTime.now().toString();
		final long initValue = 1L;
		final long finalValue = 1000L;
		final int threads = 8;
		CountDownLatch threadReady = new CountDownLatch(threads);
		CountDownLatch threadDone = new CountDownLatch(threads);
		AtomicLong updateCount = new AtomicLong();
		ExecutorService executorService = Executors.newFixedThreadPool(threads);

		for (int t = 0; t < threads; ++t) {
			CompletableFuture.runAsync(() -> {
				threadReady.countDown();
                wait(threadReady);
				mySqlSynchronizer.tryCreate(seqName, seqGroup, initValue);
				long current;
				int loop = 0;
				while ((current = mySqlSynchronizer.getNextValue(seqName, seqGroup).get()) != finalValue) {
					if (loop % 100 == 0) {
						System.out.println(String.format("[thread %s] loop %08d, current = %08d",
								Thread.currentThread().getName(), loop, current));
					}
					++loop;
					mySqlSynchronizer.tryUpdate(seqName, seqGroup, current, current + 1);
					updateCount.incrementAndGet();
					try {
						Thread.sleep(new Random().nextInt(3) + 1);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				threadDone.countDown();
			}, executorService);
		}
        wait(threadDone);
        long lastValue = mySqlSynchronizer.getNextValue(seqName, seqGroup).get();
		System.out.println(String.format("lastValue value = %d , update count = %d",
                lastValue, updateCount.get()));
        Assert.assertTrue(lastValue == finalValue);
	}

    @Test
    public void threadsTest2(){
        final String seqName = "power4j";
        final String seqGroup = LocalDateTime.now().toString();
        final long initValue = 1L;
        final long finalValue = 1000L;
        final int threads = 8;
        CountDownLatch threadReady = new CountDownLatch(threads);
        CountDownLatch threadDone = new CountDownLatch(threads);
        AtomicLong loopCount = new AtomicLong();
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        for (int t = 0; t < threads; ++t) {
            CompletableFuture.runAsync(() -> {
                try{
                    threadReady.countDown();
                    wait(threadReady);
                    mySqlSynchronizer.tryCreate(seqName, seqGroup, initValue);
                    int loop = 0;
                    Optional<Pair<Long,Long>> ret;
                    do{
                        ret = mySqlSynchronizer.tryAddAndGet(seqName, seqGroup,1,3);
                        if (loop % 20 == 0) {
                            System.out.println(String.format("[thread %s] loop %08d, from %08d to %08d",
                                    Thread.currentThread().getName(), loop,
                                    ret.orElse(Pair.Nulls()).getLeft(),
                                    ret.orElse(Pair.Nulls()).getRight())
                            );
                        }
                        ++loop;
                    }while (!ret.isPresent() || ret.get().getRight() < finalValue);
                    threadDone.countDown();
                    System.out.println(String.format("[thread %s] [done] current = %08d",
                            Thread.currentThread().getName(), ret.get().getRight()));
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }, executorService);
        }

        wait(threadDone);
        long lastValue = mySqlSynchronizer.getNextValue(seqName, seqGroup).get();
        System.out.println(String.format("lastValue value = %d , loop count = %d",
                lastValue, loopCount.get()));

        Assert.assertTrue(lastValue == finalValue + threads -1);
    }

    public static void wait(CountDownLatch countDownLatch){
        try {
            countDownLatch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}