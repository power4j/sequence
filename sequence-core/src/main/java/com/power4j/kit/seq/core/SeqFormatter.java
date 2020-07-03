package com.power4j.kit.seq.core;

/**
 * 格式化函数
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
@FunctionalInterface
public interface SeqFormatter {

	SeqFormatter DEFAULT = (seqName, partition, value) -> String.format("%s-%08d", partition, value);

	/**
	 * 格式化
	 * @param seqName
	 * @param partition
	 * @param value
	 * @return
	 */
	String format(String seqName, String partition, long value);

}
