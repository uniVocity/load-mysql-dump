load-mysql-dump
===============

Very simple project to demonstrate how to load a database dump file into another


Currently this project uses a snapshot version of uniVocity to load a 42GB file from [GHTorrent](http://ghtorrent.org/downloads.html).
Project parses database dump files (currently tested with MySQL dumps only) and restores the database (any database, postgresql, oracle, SQL server, whatever you have should work).

Everything is done in the class [LoadMysqlDump](./src/main/java/com/univocity/articles/dumpload/LoadMysqlDump.java).

To make this work, a few tweaks are required. I compiled a list of problems found and workarounds at the end of this README.
Please make sure you run with a license file on your classpath otherwise the process will execute with batching disabled.

# STATISTICS

## The hardware used to test:

I tested this using an old laptop, with outdated software, so expect your execution time to be much shorter. 

 * **CPU:** Intel(R) Core(TM) i7-2670QM CPU @ 2.20GHz (8 cores)

 * **RAM:** 16GB

 * **STORAGE:** 750 HDD 5200 RPM

 * **JDK Version:** JDK 1.6.0_45 64bit (linux)

 * **OS:** Linux Mint 16 (petra), Kernel version 3.11.0-15-generic 


## Parsing the dump file
To parse the entire 42GB file and extract String arrays with the values to produce each insert statement, plus all DDL scripts, it took 17 minutes.

## Actually parsing and inserting the values into a MySQL database

Using an old and slow HDD, the throughput averaged at **35,000 rows inserted per second**.
The entire database load took **4 and a half hours**. 

## MySQL woes and how to fix them

This is the list of problems I found when processing this dump file, and the fixes I applied:

### Errors with duplicate keys and missing foreign keys

When using MySQL native's dump load, a few settings are used, namely:

 * SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0
 * SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0
 * SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO'

These disable validation of unique keys and foreign keys. Generated values are also not processed on insertion.
 
These settings did not seem to have any effect when applied using the JDBC driver.

#### Workaround

Instead of creating tables using the the commands in the dump file, I adapted the create table scripts [here](./src/main/resources/database/mysql). The foreign keys and unique key constraints were omitted.

### Errors with values that are too long for the column type.

When the values are inserted through JDBC, I got weird errors where the database threw exceptions complaining about the length of some input values.

#### Workaround

After re-re-re-checking, and being sure nothing was nothing wrong with the input values, I did some research and found that this may be a problem with the database configuration.
I tried different settings to no avail so I simply expanded the column lengths in my own [create table scripts](./src/main/resources/database/mysql).

### Timestamp in the incorrect format

After persisting a few hundred thousand rows, the database decided that a specific String did not match its timestamp format (even though it was formatted in exactly the same way as the timestamps for other records)

#### Workaround

I simply replaced the timestamp type with Varchar(32) to store the String literal instead of letting the driver convert it to throw random exceptions.

### Insert speed was not good enough

This part is fun. If you want to insert 600 million rows into a database, you need to make sure it is optimized for insertion, otherwise a process that could take a few hours will take a day or more!

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

When inserting new rows, the primary keys of each row will be validated against the existing values. This makes the execution of the bach process exponentially slow as more rows are added.
The tables were modified to be created without primary keys.
