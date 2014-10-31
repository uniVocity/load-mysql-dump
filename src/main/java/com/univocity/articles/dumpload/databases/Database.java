/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.dumpload.databases;

import java.io.*;
import java.util.*;

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

		if(tablesToCreate != null){
			initializeDatabase(tablesToCreate);
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
	 * Iterates over all scripts under {@code src/main/resources/database/*database_name*} and executes them
	 * against your database. If required, the tables and other scripts necessary to execute the project will
	 * will be created.
	 *
	 * @param tablesToCreate a sequence of table names to create in this database, if they have not been created yet
	 */
	private void initializeDatabase(String tablesToCreate) {
		File dirWithCreateTableScripts = new File("src/main/resources/database/" + getDatabaseName().toLowerCase());
		Map<String, String> scripts = new HashMap<String, String>();
		for (File scriptFile : dirWithCreateTableScripts.listFiles()) {
			String name = scriptFile.getName();
			String script = readFile(scriptFile);
			scripts.put(name.toLowerCase(), script);
		}

		if (createTables(tablesToCreate, scripts)) {
			executeScripts(scripts);
		}
	}

	/**
	 * Attempts to create the required tables in your database.
	 * @param scripts a map with script file names and their contents to be executed against your database.
	 * @return {@code true} if tables were created with the given scripts, {@code false} if the tables already exist.
	 */
	private boolean createTables(String scriptOrder, Map<String, String> scripts) {
		String[] order = scriptOrder.split(",");

		boolean tablesCreated = false;
		for (String tableName : order) {
			String createTableScript = scripts.get(tableName + ".tbl");

			try {
				jdbcTemplate.execute("select count(*) from " + tableName);
			} catch (Exception ex) {
				jdbcTemplate.execute(createTableScript);
				tablesCreated = true;
			}
		}
		return tablesCreated;
	}

	/**
	 * Executes scripts (usually to create sequences and triggers if required). This script is optional and
	 * it should be under a file named "scripts.sql". Each line in this file will be executed individually
	 * against the your database.
	 * @param scripts a map with script file names and their contents to be executed against your database.
	 */
	private void executeScripts(Map<String, String> scripts) {
		String sequences = scripts.get("scripts");
		if (sequences != null) {
			for (String script : sequences.split("\\n")) {
				if (!script.trim().isEmpty()) {
					jdbcTemplate.execute(script);
				}
			}
		}
	}

	/**
	 * Reads a file line by line and returns the resulting content in a String
	 * @param file the file to be read
	 * @return the text content of the given file.
	 */
	private String readFile(File file) {
		StringBuilder out = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				out.append(str).append('\n');
			}
			in.close();
		} catch (IOException e) {
			throw new IllegalStateException("Error reading file " + file.getAbsolutePath(), e);
		}
		return out.toString();
	}

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
