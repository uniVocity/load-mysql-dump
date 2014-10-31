/*******************************************************************************
 * Copyright (c) 2014 uniVocity Software Pty Ltd. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in file
 * 'LICENSE.txt', which is part of this source code package.
 ******************************************************************************/
package com.univocity.articles.importcities.databases;

import java.io.*;
import java.util.*;

/**
 * A simple factory {@link Database} instances. Currently available databases are given by {@link #getAvailableDatabases()}.
 *
 * This class depends on the configuration specified in the <i>connection.properties</i> file (under src/main/resources).
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com
 *
 */
public class DatabaseFactory {

	private final Map<String, Class<? extends Database>> databases;
	private final Properties properties;

	private static final DatabaseFactory instance = new DatabaseFactory();

	private DatabaseFactory() {
		databases = new TreeMap<String, Class<? extends Database>>();

		registerDatabase(MySqlDatabase.class);
		registerDatabase(OracleXEDatabase.class);
		registerDatabase(PostgresDatabase.class);
		registerDatabase(HsqlDatabase.class);
		registerDatabase(SqlServerDatabase.class);

		properties = new Properties();
		try {
			properties.load(new FileInputStream("src/main/resources/connection.properties"));
		} catch (Exception e) {
			throw new IllegalStateException("Error loading connection.properties", e);
		}
	}

	/**
	 * Returns the {@code DatabaseFactory} instance
	 * @return the {@code DatabaseFactory} instance
	 */
	public static DatabaseFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a new instance of a given implementation of {@link Database}.
	 *
	 * @param databaseClass a class that extends {@link Database}.
	 * @return the {@link Database} instance.
	 */
	private static Database newInstance(Class<? extends Database> databaseClass) {
		try {
			return databaseClass.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Cannot create instance of database: " + databaseClass);
		}
	}

	/**
	 * Adds a new {@link Database} implementation to the factory.
	 * @param databaseClass a class that extends {@link Database}.
	 */
	public void registerDatabase(Class<? extends Database> databaseClass) {
		databases.put(newInstance(databaseClass).getDatabaseName().toLowerCase(), databaseClass);
	}

	/**
	 * Obtains a {@link Database} instance for the destination database, as specified in the <i>connection.properties</i> file.
	 * @return an instance of {@link Database}, properly initialized with the given credentials.
	 */
	public Database getDestinationDatabase() {
		return newDatabase("destination", null);
	}

	/**
	 * Obtains a {@link Database} instance for the metadata database, as specified in the <i>connection.properties</i> file.
	 * @return an instance of {@link Database}, properly initialized with the given credentials.
	 */
	public Database getMetadataDatabase() {
		return newDatabase("metadata", null);
	}

	/**
	 * Instantiates a new  {@link Database} instance using the configuration provided in the <i>connection.properties</i> file.
	 * @param prefix the prefix of the properties to be read from the <i>connection.properties</i> file.
	 * @param tablesToCreate the list of tables to create for this database, separated by a colon.
	 * @return an instance of {@link Database}, properly initialized with the given credentials.
	 */
	private Database newDatabase(String prefix, String tablesToCreate) {
		String databaseName = properties.getProperty(prefix + ".database.name");
		String connectionUrl = properties.getProperty(prefix + ".database.url");
		String username = properties.getProperty(prefix + ".database.user");
		String password = properties.getProperty(prefix + ".database.password");

		Class<? extends Database> database = databases.get(databaseName.toLowerCase());
		if (database == null) {
			throw new IllegalArgumentException("Unknown database name: " + databaseName + ". Available databases: " + databases.keySet());
		}

		try {
			Database instance = newInstance(database);
			instance.initialize(tablesToCreate, connectionUrl, username, password);
			return instance;
		} catch (Exception ex) {
			throw new IllegalStateException("Unexpected error initializing database " + databaseName + ". Please review your connection.properties file", ex);
		}
	}

	/**
	 * Returns the names of the available databases you can connect to using this factory class.
	 * @return a set of database names available for use.
	 */
	public Set<String> getAvailableDatabases() {
		return Collections.unmodifiableSet(databases.keySet());
	}
}
