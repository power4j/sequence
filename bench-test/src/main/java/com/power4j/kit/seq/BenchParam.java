package com.power4j.kit.seq;

import com.power4j.kit.seq.core.LongSeqPool;

/**
 * 公共参数
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public interface BenchParam {

	long SEQ_INIT_VAL = LongSeqPool.MIN_VALUE;

	int SEQ_POOL_SIZE = 1000;

}
