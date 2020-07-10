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

package com.power4j.kit.seq.persistent.provider;

import com.power4j.kit.seq.core.exceptions.SeqException;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * MySql支持
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/1
 * @since 1.0
 */
@Slf4j
@AllArgsConstructor
public class MySqlSynchronizer extends AbstractJdbcSynchronizer implements SeqSynchronizer {

	// @formatter:off

	private final static String MYSQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "seq_name VARCHAR ( 32 ) NOT NULL," +
                "seq_partition VARCHAR ( 255 ) NOT NULL," +
                "seq_next_value BIGINT NOT NULL," +
                "seq_create_time TIMESTAMP NOT NULL," +
                "seq_update_time TIMESTAMP NULL," +
                "PRIMARY KEY ( `seq_name`, `seq_partition` ) " +
            ")";

	private final static String MYSQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME";

	private final static String MYSQL_INSERT_IGNORE = "INSERT IGNORE INTO $TABLE_NAME"
			+ "(seq_name,seq_partition,seq_next_value,seq_create_time,seq_update_time)" + " VALUE(?,?,?,?,?)";

	private final static String MYSQL_UPDATE_VALUE = "UPDATE $TABLE_NAME SET seq_next_value=?,seq_update_time=? "
			+ "WHERE seq_name=? AND seq_partition=? AND seq_next_value=?";

	private final static String MYSQL_SELECT_VALUE = "SELECT seq_next_value FROM $TABLE_NAME WHERE seq_name=? AND seq_partition=?";

    // @formatter:on

	private final String tableName;

	private final DataSource dataSource;

	@Override
	public void createMissingTable() {
		log.info("create table if not exists : {}", tableName);
		final String sql = MYSQL_CREATE_TABLE.replace("$TABLE_NAME", tableName);
		log.debug(sql);
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.executeUpdate();
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public void dropTable() {
		log.warn("drop table if exists : {}", tableName);
		final String sql = MYSQL_DROP_TABLE.replace("$TABLE_NAME", tableName);
		log.debug(sql);
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.executeUpdate();
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	protected Optional<Long> selectSeqValue(Connection connection, String name, String partition) throws SQLException {
		final String sql = MYSQL_SELECT_VALUE.replace("$TABLE_NAME", tableName);
		log.debug(sql);
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, name);
			statement.setString(2, partition);
			log.debug(String.format("param: [%s] [%s]", name, partition));
			try (ResultSet resultSet = statement.executeQuery();) {
				if (resultSet.next()) {
					if (resultSet.getObject(1) == null) {
						throw new IllegalStateException("bad seq value");
					}
					return Optional.of(resultSet.getLong(1));
				}
				return Optional.empty();
			}
		}
	}

	@Override
	protected boolean createMissingSeqEntry(Connection connection, String name, String partition, long nextValue)
			throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		final String sql = MYSQL_INSERT_IGNORE.replace("$TABLE_NAME", tableName);
		log.debug(sql);
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, name);
			statement.setString(2, partition);
			statement.setLong(3, nextValue);
			statement.setTimestamp(4, now);
			statement.setTimestamp(5, null);
			log.debug(String.format("param: [%s] [%s] [%d] [%s] [%s]", name, partition, nextValue, now, null));
			int rows = statement.executeUpdate();
			log.debug(String.format("update rows: %d", rows));
			return rows > 0;
		}
	}

	@Override
	protected boolean updateSeqValue(Connection connection, String name, String partition, long nextValueOld,
			long nextValueNew) throws SQLException {
		final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
		final String sql = MYSQL_UPDATE_VALUE.replace("$TABLE_NAME", tableName);
		log.debug(sql);
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, nextValueNew);
			statement.setTimestamp(2, now);
			statement.setString(3, name);
			statement.setString(4, partition);
			statement.setLong(5, nextValueOld);
			log.debug(String.format("param: [%d] [%s] [%s] [%s] [%d]", nextValueNew, now.toString(), name, partition,
					nextValueOld));
			int rows = statement.executeUpdate();
			log.debug(String.format("update rows: %d", rows));
			return rows > 0;
		}
	}

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

}
