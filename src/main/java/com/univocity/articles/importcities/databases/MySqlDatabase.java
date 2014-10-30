/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import com.univocity.api.entity.jdbc.*;

/**
 * A {@link Database} implementation for MySQL (also works with MariaDB).
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
class MySqlDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "MySql";
	}

	@Override
	String getDriverClassName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		/*
		 * uniVocity escapes column names that may conflict with database identifiers.
		 * By default it uses double quotes ("), but in MySQL identifiers are escaped with the backtick character (`).
		 */
		jdbcDataStoreConfig.setIdentifierEscaper(new DefaultEscaper("`"));
	}
}
