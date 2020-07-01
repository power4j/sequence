package com.power4j.kit.seq.persistent;

import lombok.Builder;
import lombok.Data;

/**
 * 序号的持久化信息
 *
 * @author CJ (jclazz@outlook.com)
 * @date 2020/6/30
 * @since 1.0
 */
@Data
@Builder
public class Seq {

	private String id;

	private String namespace;

	private Long value;

}
