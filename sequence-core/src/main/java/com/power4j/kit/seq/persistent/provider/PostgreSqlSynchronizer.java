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
import com.power4j.kit.seq.persistent.AddState;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PostgreSQL 支持
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/29
 * @since 1.3
 */
@Slf4j
@AllArgsConstructor
public class PostgreSqlSynchronizer extends AbstractSqlStatementProvider implements SeqSynchronizer {

	// @formatter:off

	private final static String POSTGRESQL_CREATE_TABLE =
			"CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
					"seq_name VARCHAR ( 255 ) NOT NULL," +
					"seq_partition VARCHAR ( 255 ) NOT NULL," +
					"seq_next_value BIGINT NOT NULL," +
					"seq_create_time TIMESTAMP NOT NULL," +
					"seq_update_time TIMESTAMP NULL," +
					"PRIMARY KEY ( seq_name, seq_partition ) " +
					")";

	private final static String POSTGRESQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME";

	private final static String POSTGRESQL_INSERT_IGNORE =
			"INSERT INTO $TABLE_NAME" +
					"(seq_name,seq_partition,seq_next_value,seq_create_time)" +
					" VALUES (?,?,?,?) ON CONFLICT(seq_name, seq_partition) DO NOTHING";

	private final static String POSTGRESQL_UPDATE_VALUE =
			"UPDATE $TABLE_NAME SET seq_next_value=?,seq_update_time=? " +
					"WHERE seq_name=? AND seq_partition=? AND seq_next_value=?";

	private final static String POSTGRESQL_ADD_VALUE =
			"UPDATE $TABLE_NAME SET seq_next_value=seq_next_value + ?,seq_update_time=? " +
					"WHERE seq_name=? AND seq_partition=? RETURNING seq_next_value";

	private final static String POSTGRESQL_SELECT_VALUE =
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
	public AddState tryAddAndGet(String name, String partition, int delta, int maxReTry) {
		try (Connection connection = getConnection()) {
			return addAndGet(connection, name, partition, delta).map(val -> AddState.success(val - delta, val, 1))
					.orElseThrow(() -> new SeqException(String.format("Not exist: %s %s", name, partition)));
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	/**
	 * 加法操作
	 * @param connection
	 * @param name
	 * @param partition
	 * @param delta
	 * @return 返回加法操作后的结果,如果记录不存在返回null
	 * @throws SQLException
	 */
	private Optional<Long> addAndGet(Connection connection, String name, String partition, int delta)
			throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		final String sql = POSTGRESQL_ADD_VALUE.replace("$TABLE_NAME", tableName);
		if (log.isDebugEnabled()) {
			log.debug("Add Value Sql:[{}]", sql);
		}
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, delta);
			statement.setTimestamp(2, now);
			statement.setString(3, name);
			statement.setString(4, partition);
			log.debug(String.format("param: [%d] [%s] [%s] [%s]", delta, now.toString(), name, partition));
			updateCount.incrementAndGet();
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getObject(1) != null) {
					return Optional.of(resultSet.getLong(1));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	protected String getCreateTableSql() {
		return POSTGRESQL_CREATE_TABLE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getDropTableSql() {
		return POSTGRESQL_DROP_TABLE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getCreateSeqSql() {
		return POSTGRESQL_INSERT_IGNORE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getSelectSeqSql() {
		return POSTGRESQL_SELECT_VALUE.replace("$TABLE_NAME", tableName);
	}

	@Override
	protected String getUpdateSeqSql() {
		return POSTGRESQL_UPDATE_VALUE.replace("$TABLE_NAME", tableName);
	}

}
