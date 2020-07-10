/*
 * Copyright (c) 2020 ChenJun(power4j@outlook.com)
 * Sequence is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.power4j.kit.seq.spring.boot.autoconfigure;

import com.power4j.kit.seq.persistent.SeqSynchronizer;
import com.power4j.kit.seq.persistent.provider.MySqlSynchronizer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/7
 * @since 1.0
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class JdbcSynchronizerConfigure {

	@Bean
	@ConditionalOnMissingBean
	public SeqSynchronizer jdbcSynchronizer(SequenceProperties sequenceProperties, DataSource dataSource) {
		if (!SequenceProperties.BackendTypeEnum.mysql.equals(sequenceProperties.getBackend())) {
			throw new UnsupportedOperationException("Not support : " + sequenceProperties.getBackend());
		}
		MySqlSynchronizer synchronizer = new MySqlSynchronizer(sequenceProperties.getTableName(), dataSource);
		if (!sequenceProperties.isLazyInit()) {
			synchronizer.init();
		}
		return synchronizer;
	}

}
