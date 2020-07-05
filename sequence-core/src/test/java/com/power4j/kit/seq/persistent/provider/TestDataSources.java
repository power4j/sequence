/*
 * Copyright (c) 2020, ChenJun(powe4j@outlook.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.kit.seq.persistent.provider;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author CJ (jclazz@outlook.com)
 * @date 2020/7/3
 * @since 1.0
 */
public class TestDataSources {

	public final static String MYSQL_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false";

	public static DataSource getMySqlDataSource() {
		String pwd = System.getenv("MYSQL_TEST_PWD");
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(MYSQL_JDBC_URL);
		config.setUsername("root");
		config.setPassword(pwd == null ? "" : pwd.trim());
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "100");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		return new HikariDataSource(config);
	}

}
