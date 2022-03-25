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
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于JDBC的同步抽象
 *
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/6
 * @since 1.0
 */
@Slf4j
public abstract class AbstractJdbcSynchronizer implements SeqSynchronizer {

	protected final AtomicLong queryCount = new AtomicLong();

	protected final AtomicLong updateCount = new AtomicLong();

	/**
	 * 获取数据库连接
	 * @return
	 * @throws SQLException
	 */
	protected abstract Connection getConnection() throws SQLException;

	/**
	 * 建表
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	protected abstract PreparedStatement getCreateTableStatement(Connection connection) throws SQLException;

	/**
	 * 删表
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	protected abstract PreparedStatement getDropTableStatement(Connection connection) throws SQLException;

	/**
	 * 创建记录
	 * @param connection
	 * @param name
	 * @param partition
	 * @param nextValue
	 * @return
	 * @throws SQLException
	 */
	protected abstract PreparedStatement getCreateSeqStatement(Connection connection, String name, String partition,
			long nextValue) throws SQLException;

	/**
	 * 查询 value
	 * @param connection
	 * @param name
	 * @param partition
	 * @return
	 * @throws SQLException
	 */
	protected abstract PreparedStatement getSelectSeqStatement(Connection connection, String name, String partition)
			throws SQLException;

	/**
	 * 更新 value
	 * @param connection
	 * @param name
	 * @param partition
	 * @param nextValueOld
	 * @param nextValueNew
	 * @return
	 * @throws SQLException
	 */
	protected abstract PreparedStatement getUpdateSeqStatement(Connection connection, String name, String partition,
			long nextValueOld, long nextValueNew) throws SQLException;

	/**
	 * 建表,表已经存在则忽略
	 * @throws SQLException
	 */
	public void createMissingTable() {
		try (Connection connection = getConnection();
				PreparedStatement statement = getCreateTableStatement(connection)) {
			statement.execute();
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	/**
	 * 删表,表不存在则忽略
	 * @throws SQLException
	 */
	public void dropTable() {
		try (Connection connection = getConnection(); PreparedStatement statement = getDropTableStatement(connection)) {
			statement.execute();
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	/**
	 * 查询当前值
	 * @param connection
	 * @param name
	 * @param partition
	 * @return 无查询结果(比如Seq记录不存在)返回null
	 * @throws SQLException 数据库异常
	 */
	protected Optional<Long> selectSeqValue(Connection connection, String name, String partition) throws SQLException {
		try (PreparedStatement statement = getSelectSeqStatement(connection, name, partition)) {
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					if (resultSet.getObject(1) == null) {
						throw new IllegalStateException("Bad seq value");
					}
					return Optional.of(resultSet.getLong(1));
				}
				return Optional.empty();
			}
		}
	}

	/**
	 * 创建某个序号的记录，如果该序号已经存在，则忽略
	 * @param connection
	 * @param name
	 * @param partition
	 * @param nextValue 记录初始值,若记录已经存在,此参数没有实际用途
	 * @return
	 * @throws SQLException 数据库异常
	 */
	protected boolean createMissingSeqEntry(Connection connection, String name, String partition, long nextValue)
			throws SQLException {
		try (PreparedStatement statement = getCreateSeqStatement(connection, name, partition, nextValue)) {
			int rows = statement.executeUpdate();
			log.debug(String.format("Update rows: %d", rows));
			return rows > 0;
		}
	}

	/**
	 * 更新某个序号的记录值
	 * @param connection
	 * @param name
	 * @param partition
	 * @param nextValueOld 旧的值,用于 {@code MVCC}
	 * @param nextValueNew
	 * @return 更新成功返回true,失败返回false
	 * @throws SQLException 数据库异常
	 */
	protected boolean updateSeqValue(Connection connection, String name, String partition, long nextValueOld,
			long nextValueNew) throws SQLException {
		try (PreparedStatement statement = getUpdateSeqStatement(connection, name, partition, nextValueOld,
				nextValueNew)) {
			int rows = statement.executeUpdate();
			log.debug(String.format("Update rows: %d", rows));
			return rows > 0;
		}
	}

	@Override
	public long getQueryCounter() {
		return queryCount.get();
	}

	@Override
	public long getUpdateCounter() {
		return updateCount.get();
	}

	@Override
	public boolean tryCreate(String name, String partition, long nextValue) {
		try (Connection connection = getConnection()) {
			return createMissingSeqEntry(connection, name, partition, nextValue);
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public boolean tryUpdate(String name, String partition, long nextValueOld, long nextValueNew) {
		try (Connection connection = getConnection()) {
			boolean ret = updateSeqValue(connection, name, partition, nextValueOld, nextValueNew);
			updateCount.incrementAndGet();
			return ret;
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public AddState tryAddAndGet(String name, String partition, int delta, int maxReTry) {
		int totalOps = 0;
		try (Connection connection = getConnection()) {
			do {
				++totalOps;
				long lastValue = selectSeqValue(connection, name, partition).get();
				queryCount.incrementAndGet();
				final long target = lastValue + delta;
				boolean updateDone = updateSeqValue(connection, name, partition, lastValue, target);
				updateCount.incrementAndGet();
				if (updateDone) {
					// 需要抢占成功,返回号段的起止区间(前闭后开)
					return AddState.success(lastValue, target, totalOps);
				}
			}
			while (maxReTry < 0 || totalOps <= maxReTry + 1);
			return AddState.fail(totalOps);
		}
		catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public Optional<Long> getNextValue(String name, String partition) {
		try (Connection connection = getConnection()) {
			Optional<Long> ret = selectSeqValue(connection, name, partition);
			queryCount.incrementAndGet();
			return ret;
		}
		catch (SQLException e) {
			throw new SeqException(e.getMessage(), e);
		}
	}

	@Override
	public void init() {
		createMissingTable();
	}

}
