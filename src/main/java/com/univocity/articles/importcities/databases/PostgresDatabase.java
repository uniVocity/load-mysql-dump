/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import com.univocity.api.entity.jdbc.*;

/**
 * A {@link Database} implementation for Postgres
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
class PostgresDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "Postgres";
	}

	@Override
	String getDriverClassName() {
		return "org.postgresql.Driver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		/*
		 * Most JDBC drivers convert values to the appropriate types of each column in a table.
		 * For example: statement.setObject(1, "1234") will convert the String "1234" automatically to
		 * match the underlying column type (Integer, Decimal, BigInt, etc)
		 *
		 * Postgres' driver requires parameters to be explicitly converted to the appropriate type.
		 * By enabling the parameter conversion, uniVocity will try to convert input values and call
		 * the appropriate statement method. Using the example above, if the column is of type
		 * Decimal, uniVocity will call statement.setBigDecimal(1, new BigDecimal("1234")).
		 */
		jdbcDataStoreConfig.getDefaultEntityConfiguration().setParameterConversionEnabled(true);
	}
}
