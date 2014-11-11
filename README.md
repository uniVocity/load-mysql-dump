load-mysql-dump
===============

Very simple project to demonstrate how to load a database dump file into any database through JDBC. 

Currently this project uses a snapshot version of (uniVocity)[http://www.univocity.com/pages/univocity-features] to load a 42GB file from [GHTorrent](http://ghtorrent.org/downloads.html).

## But you should use the database native import tools

We used to agree, however:

 * You may want to load data from an incompatible dump file. For example: from a MySQL dump file into an Oracle database.
 
 * Maybe you just want to convert that data into CSV, or load information from just a few tables, or even perform some data mapping directly from the file into an completely different schema. That's exactly [what uniVocity does with other sources of data](https://github.com/uniVocity/worldcities-import/blob/master/src/main/java/com/univocity/articles/importcities/MigrateWorldCities.java), why not dump files?
 
 * We created this for a client who was not very happy with the performance he was getting with MySQLImport. They managed to load the **42 GB** file in **3.9 days** on a **16 core** server with **64Gb ram** and a **10 disk SSD array**.


## Notes

Everything is done in the class [LoadMysqlDump](./src/main/java/com/univocity/articles/dumpload/LoadMysqlDump.java).

(uniVocity)[http://www.univocity.com/pages/univocity-features] can parse a database dump file and restore the data into a given database. ANY database: Postgres, Oracle, SQL server, etc... whatever you have should work.

Currently, we are loading the data into MySQL only. We'll add create table scripts for other databases (as we did [here](https://github.com/uniVocity/worldcities-import/tree/master/src/main/resources/database) later on.

To make this work at maximum speed, a few tweaks are required. I compiled a list of problems found and workarounds at the end of this README.

Please make sure you run with a license file on your classpath otherwise the process will execute with batching disabled.

This is work in progress. We are working on the upcoming release of uniVocity 1.1.0. and the API is being adjusted to allow better flexibility. Check out the [latest commits in uniVocity-api's development branch](https://github.com/uniVocity/univocity-api/commits/develop). 


## STATISTICS


### The hardware used to test:

I tested this using an old laptop, with outdated software, so expect your execution time to be much shorter. 

 * **CPU:** Intel(R) Core(TM) i7-2670QM CPU @ 2.20GHz (8 cores)

 * **RAM:** 16GB

 * **STORAGE:** 750 HDD 5200 RPM

 * **JDK Version:** JDK 1.6.0_45 64bit (linux)

 * **OS:** Linux Mint 16 (petra), Kernel version 3.11.0-15-generic 


### Parsing the dump file
To just *parse* the entire *42GB* file and extract String arrays with the values to produce each insert statement, plus all DDL scripts, it took *17 minutes*.

### Actually parsing and inserting the 1 billion+ rows from this file into a MySQL database

Using an old and slow HDD, the throughput averaged at **115,000/second**.

 * Using MySQL's **InnoDB** engine, the entire database load took **4 and a half hours**. 
 * Using MySQL's **MyISAM** engine, the entire database load took **90 minutes**.


## MySQL woes and how to fix them

This is the list of problems I found when processing this dump file, and the fixes I applied. Note these are settings for MySQL only.

### Errors with duplicate keys and missing foreign keys

When using MySQL native's dump load, a few settings are used, namely:

 * SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0
 * SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0
 * SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO'

These disable validation of unique keys and foreign keys. Generated values are also not processed on insertion.
 
These settings did not seem to have **any** effect when applied using the JDBC driver. They only work when you load the dump file using MySQL's own data load facilities (i.e. MySQLImport) 

#### Workaround

Instead of creating tables using the the commands in the dump file, I adapted the create table scripts [here](./src/main/resources/database/mysql). The foreign keys and unique key constraints were omitted.

### Errors with values that are too long for the column type.

When the values are inserted through JDBC, I got weird errors where the database threw exceptions complaining about the length of some input values.

#### Workaround

After re-re-re-checking, and being sure nothing was nothing wrong with the input values, I did some research and found that this may be a problem with the database configuration.
I tried different settings to no avail so I simply expanded the column lengths on my own [create table scripts](./src/main/resources/database/mysql).

### Timestamp in the incorrect format

After persisting a few hundred thousand rows, the database decided that a specific String did not match its timestamp format (even though it was formatted in exactly the same way as the timestamps for other records)

#### Workaround

I simply replaced the timestamp type with Varchar(32) to store the String literal instead of letting the driver convert it to throw random exceptions at me.

### Insert speed was not good enough

This part is fun. If you want to insert 1 billion+ rows into a database, you need to make sure it is optimized for insertion, otherwise a process that could take a few hours will take a day or more!

#### Configuration

I added the following configuration options to MySQL's `my.cfg` file.

```
	innodb_doublewrite = 0
	innodb_buffer_pool_size = 8000M
	# innodb_log_file_size = 512M - If I enable this one the server won't start. Couldn't identify why.
	log-bin = 0
	innodb_support_xa = 0
	innodb_flush_log_at_trx_commit = 0
```

After saving the file, restart the database.

#### No primary keys

When inserting new rows, the primary keys of each row will be validated against the existing values. This makes the execution of the batch process exponentially slow as more rows are added.

The tables were modified to be created without primary keys.

#### MyISAM vs InnoDB

InnoDB is more reliable but this comes at the cost of speed. We changed our create table scripts so all tables use the MyISAM engine. The process that took 4.5 hours completed in 90 minutes after this modification.