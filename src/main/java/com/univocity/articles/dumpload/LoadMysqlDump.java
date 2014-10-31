/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.dumpload;

import javax.sql.*;

import com.univocity.api.*;
import com.univocity.api.config.*;
import com.univocity.api.entity.custom.*;
import com.univocity.api.entity.jdbc.*;
import com.univocity.articles.dumpload.databases.*;

public class LoadMysqlDump {

	private final Database database;

	private int batchSize = 10000;
	private final String engineName;

	public LoadMysqlDump() {

		this.engineName = "LoadMySqlDump";
		this.database = DatabaseFactory.getInstance().getDestinationDatabase();

		System.out.println("Starting " + getClass().getName() + " with " + database.getDatabaseName());

		DataStoreConfiguration databaseConfig = createDatabaseConfiguration();

		EngineConfiguration config = new EngineConfiguration(engineName, databaseConfig);

		Univocity.registerEngine(config);

	}

	public void loadDumpFile() {
		try {
			//simply initializes the engine. The database data store will be loaded from the dump file.
			Univocity.getEngine(engineName);
		} finally {
			//done, shut the engine down.
			Univocity.shutdown(engineName);
		}
	}

	/**
	 * Creates a {@link JdbcDataStoreConfiguration} configuration object with the appropriate settings
	 * for the underlying database.
	 *
	 * @return the configuration for the "database" data store.
	 */
	public DataStoreConfiguration createDatabaseConfiguration() {
		//Gets a javax.sql.DataSource instance from the database object.
		DataSource dataSource = database.getDataSource();

		JdbcDataStoreConfiguration config = new JdbcDataStoreConfiguration("database", dataSource);

		// ### this one is important! ###
		database.applyDatabaseSpecificConfiguration(config);

		config.setLimitOfRowsLoadedInMemory(batchSize);

		//Format configuration to determine how to process and parse the dump file.
		JdbcDataStoreDumpLoadConfiguration dump = new JdbcDataStoreDumpLoadConfiguration("/home/jbax/Downloads/dump/mysql-2014-08-18.sql", "UTF-8");

		dump.setBatchSize(10000);
		dump.setProcessDDLScripts(false);
		dump.getFormat().setRecordIdentifier("INSERT INTO `?` VALUES"); //MySQL enclosed the table name between ` `.
		dump.getFormat().setOneInsertPerRow(false);
		

		config.setInitialDumpLoadConfiguration(dump);

		return config;
	}

	//Simply run and that's it.
	public static void main(String... args) {
		new LoadMysqlDump().loadDumpFile();

	}
}
