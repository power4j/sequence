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

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 描述信息
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/29
 * @since 1.0
 */
@Slf4j
public abstract class AbstractSqlStatementProvider extends AbstractJdbcSynchronizer {

	/**
	 * 建表SQL
	 * @return
	 */
	protected abstract String getCreateTableSql();

	/**
	 * 删表SQL
	 * @return
	 */
	protected abstract String getDropTableSql();

	/**
	 * 创建记录SQL
	 * @return
	 */
	protected abstract String getCreateSeqSql();

	/**
	 * 查询SQL
	 * @return
	 */
	protected abstract String getSelectSeqSql();

	/**
	 * 更新SQL
	 * @return
	 */
	protected abstract String getUpdateSeqSql();

	@Override
	protected PreparedStatement getCreateTableStatement(Connection connection) throws SQLException {
		final String sql = getCreateTableSql();
		if (log.isDebugEnabled()) {
			log.debug("Create Table Sql:[{}]", sql);
		}
		return connection.prepareStatement(sql);
	}

	@Override
	protected PreparedStatement getDropTableStatement(Connection connection) throws SQLException {
		final String sql = getDropTableSql();
		if (log.isDebugEnabled()) {
			log.debug("Drop Table Sql:[{}]", sql);
		}
		return connection.prepareStatement(sql);
	}

	@Override
	protected PreparedStatement getCreateSeqStatement(Connection connection, String name, String partition,
			long nextValue) throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		final String sql = getCreateSeqSql();
		if (log.isDebugEnabled()) {
			log.debug("Create Seq Sql:[{}]", sql);
		}
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, name);
		statement.setString(2, partition);
		statement.setLong(3, nextValue);
		statement.setTimestamp(4, now);
		log.debug(String.format("param: [%s] [%s] [%d] [%s]", name, partition, nextValue, now));
		return statement;
	}

	@Override
	protected PreparedStatement getSelectSeqStatement(Connection connection, String name, String partition)
			throws SQLException {
		final String sql = getSelectSeqSql();
		if (log.isDebugEnabled()) {
			log.debug("Select Seq Sql:[{}]", sql);
		}
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, name);
		statement.setString(2, partition);
		log.debug(String.format("param: [%s] [%s]", name, partition));
		return statement;
	}

	@Override
	protected PreparedStatement getUpdateSeqStatement(Connection connection, String name, String partition,
			long nextValueOld, long nextValueNew) throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		final String sql = getUpdateSeqSql();
		if (log.isDebugEnabled()) {
			log.debug("Update Seq Sql:[{}]", sql);
		}
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setLong(1, nextValueNew);
		statement.setTimestamp(2, now);
		statement.setString(3, name);
		statement.setString(4, partition);
		statement.setLong(5, nextValueOld);
		log.debug(String.format("param: [%d] [%s] [%s] [%s] [%d]", nextValueNew, now.toString(), name, partition,
				nextValueOld));
		return statement;
	}

}
