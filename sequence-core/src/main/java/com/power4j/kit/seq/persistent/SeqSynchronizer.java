package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.utils.AddState;

import java.util.Optional;

/**
 * Seq记录同步接口 <br/>
 * <ul>
 * <li>Seq记录唯一性 = 名称 + 分区</li>
 * <li>此接口的所有方法实现必须提供线程安全保障</li>
 * </ul>
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/1
 * @since 1.0
 */
public interface SeqSynchronizer {

	/**
	 * 创建序号记录,如已经存在则忽略
	 * @param name 名称
	 * @param partition 分区
	 * @param nextValue 初始值
	 * @return true 表示创建成功,false 表示记录已经存在
	 */
	boolean tryCreate(String name, String partition, long nextValue);

	/**
	 * 尝试更新记录
	 * @param name
	 * @param partition
	 * @param nextValueOld
	 * @param nextValueNew
	 * @return true 表示更新成功
	 */
	boolean tryUpdate(String name, String partition, long nextValueOld, long nextValueNew);

	/**
	 * 尝试加法操作
	 * @param name
	 * @param partition
	 * @param delta 加数
	 * @param maxReTry 最大重试次数,小于0表示无限制. 0 表示重试零次(总共执行1次) 1 表示重试一次(总共执行2次)
	 * @return 返回执行加法操作执行结果
	 */
	AddState tryAddAndGet(String name, String partition, int delta, int maxReTry);

	/**
	 * 获取值
	 * @param name
	 * @param partition
	 * @return 返回null表示记录不存在
	 */
	Optional<Long> getNextValue(String name, String partition);

}
