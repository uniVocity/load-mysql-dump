/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import javax.sql.*;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.*;

import com.univocity.api.entity.jdbc.*;

/**
 * A simple class to initialize a given database with the scripts provided under {@code src/main/resources/database/*database_name*}
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public abstract class Database {

	private JdbcTemplate jdbcTemplate;

	/**
	 * Just an empty constructor. Initialization happens when {@link #initialize(String, String, String, String)} is invoked.
	 */
	public Database() {
	}

	/**
	 * Initializes this Database object by creating a {@link DataSource} to provide connections to your database.
	 * Tables will be created automatically if required.
	 *
	 * @param tablesToCreate a sequence of table names to create in this database, if they have not been created yet
	 * @param connectionUrl the JDBC URL to use for accessing the {@link java.sql.DriverManager}
	 * @param username the username to connect to the database
	 * @param password the password of the given username, if required
	 */
	void initialize(String tablesToCreate, String connectionUrl, String username, String password) {
		try {
			Class.forName(getDriverClassName());
			DataSource dataSource = new SingleConnectionDataSource(connectionUrl, username, password, true);
			this.jdbcTemplate = new JdbcTemplate(dataSource);

		} catch (Exception ex) {
			throw new IllegalStateException("Error creating database using scripts for database " + getDatabaseName(), ex);
		}
	}

	/**
	 * Returns the database name
	 * @return the database name
	 */
	public abstract String getDatabaseName();

	/**
	 * Returns the driver class name which will be loaded using the old-fashioned {@link Class#forName(String)} method before
	 * creating a datasource.
	 * @return the driver class name.
	 */
	abstract String getDriverClassName();

	/**
	 * Returns the {@link javax.sql.DataSource} that provides connections to your database.
	 * @return a {@link javax.sql.DataSource} for your database.
	 */
	public DataSource getDataSource() {
		return jdbcTemplate.getDataSource();
	}

	/**
	 * Applies database-specific configurations to an instance of {@link JdbcDataStoreConfiguration}. uniVocity tries to extract as much information
	 * from your database metadata as possible. Some information might not be available from your JDBC driver so you need to provide it manually.
	 *
	 * Additionally, uniVocity comes with some default settings that may not be compatible with your specific JDBC driver. In such cases,
	 * you need to provide your specific configurations.
	 *
	 * @param jdbcDataStoreConfig a pre-initialized {@link JdbcDataStoreConfiguration}, created by
	 * {@link com.univocity.articles.importcities.EtlProcess#createDatabaseConfiguration()}, with
	 * common settings such as batch size and generated key retrieval strategy.
	 */
	public abstract void applyDatabaseSpecificConfiguration(JdbcDataStoreConfiguration jdbcDataStoreConfig);
}
