/*
 * Copyright 2020 ChenJun (power4j@outlook.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class JdbcSynchronizerConfigure {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(DataSource.class)
	@ConditionalOnProperty(prefix = SequenceProperties.PREFIX, name = "backend", havingValue = "mysql")
	public SeqSynchronizer mysqlSynchronizer(SequenceProperties sequenceProperties, DataSource dataSource) {
		MySqlSynchronizer synchronizer = new MySqlSynchronizer(sequenceProperties.getTableName(), dataSource);
		if (!sequenceProperties.isLazyInit()) {
			synchronizer.init();
		}
		return synchronizer;
	}

}
