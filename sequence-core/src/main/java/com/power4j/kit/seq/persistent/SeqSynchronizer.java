package com.power4j.kit.seq.persistent;

import com.power4j.kit.seq.utils.Pair;

import java.util.Optional;

/**
 * Seq记录同步接口 <br>
 * 记录唯一性 = 名称 + 分片
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/1
 * @since 1.0
 */
public interface SeqSynchronizer {

	/**
	 * 尝试创建记录
	 * @param name 名称
	 * @param piece 分片
	 * @param nextValue 初始值
	 * @return true 表示创建成功,false 表示记录已经存在
	 */
	boolean tryCreate(String name, String piece, long nextValue);

	/**
	 * 尝试更新记录
	 * @param name
	 * @param piece
	 * @param nextValueOld
	 * @param nextValueNew
	 * @return true 表示更新成功
	 */
	boolean tryUpdate(String name, String piece, long nextValueOld, long nextValueNew);

    /**
     * 尝试加法操作
     * @param name
     * @param piece
     * @param addend 加数
     * @param retry 重试次数,小于0表示无限制. 0 表示重试零次(总共执行1次) 1 表示重试一次(总共执行2次)
     * @return 返回执行加法操作后的旧值和新值,null表示更新失败
     */
    Optional<Pair<Long,Long>> tryAddAndGet(String name, String piece, int addend, int retry);

	/**
	 * 获取值
	 * @param name
	 * @param piece
	 * @return 返回null表示记录不存在
	 */
	Optional<Long> getNextValue(String name, String piece);

}
