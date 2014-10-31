/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.dumpload.databases;

import org.springframework.jdbc.core.*;

import com.univocity.api.entity.jdbc.*;

/**
 * A {@link Database} implementation for HSQLDB
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
class HsqlDatabase extends Database {

	@Override
	public String getDatabaseName() {
		return "HSQLDB";
	}

	@Override
	String getDriverClassName() {
		return "org.hsqldb.jdbcDriver";
	}

	@Override
	public void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig) {
		/*
		 * Shuts down HSQLDB after executing the data migration processes.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				new JdbcTemplate(getDataSource()).execute("SHUTDOWN");
			}
		});
	}
}
