/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.dumpload.databases;

import com.univocity.api.entity.jdbc.*;

/**
 * A {@link Database} implementation for Microsoft SQL Server
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public class SqlServerDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "SqlServer";
	}

	@Override
	String getDriverClassName() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		//no specific configuration required.
	}

}
