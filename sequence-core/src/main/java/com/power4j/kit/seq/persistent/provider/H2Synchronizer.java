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

package com.power4j.kit.seq.persistent.provider;

import com.power4j.kit.seq.core.exceptions.SeqException;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * H2数据源支持
 *
 * @author lishangbu
 * @date 2021/8/28
 * @since 1.5.0
 */
@Slf4j
@AllArgsConstructor
public class H2Synchronizer extends AbstractSqlStatementProvider implements SeqSynchronizer {

	// @formatter:off

	private final static String H2_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "seq_name VARCHAR ( 255 ) NOT NULL," +
                "seq_partition VARCHAR ( 255 ) NOT NULL," +
                "seq_next_value BIGINT NOT NULL," +
                "seq_create_time TIMESTAMP NOT NULL," +
                "seq_update_time TIMESTAMP NULL," +
                "PRIMARY KEY ( `seq_name`, `seq_partition` ) " +
            ")";

	private final static String H2_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME";

	private final static String H2_INSERT_IGNORE =
			"INSERT IGNORE INTO $TABLE_NAME" +
					"(seq_name,seq_partition,seq_next_value,seq_create_time)" +
					" VALUES (?,?,?,?)";

	private final static String H2_UPDATE_VALUE =
			"UPDATE $TABLE_NAME SET seq_next_value=?,seq_update_time=? " +
					"WHERE seq_name=? AND seq_partition=? AND seq_next_value=?";

	private final static String H2_SELECT_VALUE =
			"SELECT seq_next_value FROM $TABLE_NAME WHERE seq_name=? AND seq_partition=?";

    // @formatter:on

	private final String tableName;

	private final DataSource dataSource;

	@Override
	protected Connection getConnection() {
		try {
			return dataSource.getConnection();
		}
		catch (Exception e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	protected String getCreateTableSql() {
		return H2_CREATE_TABLE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getDropTableSql() {
		return H2_DROP_TABLE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getCreateSeqSql() {
		return H2_INSERT_IGNORE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getSelectSeqSql() {
		return H2_SELECT_VALUE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getUpdateSeqSql() {
		return H2_UPDATE_VALUE.replace("$TABLE_NAME", tableName);
	}

}
