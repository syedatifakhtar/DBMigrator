package com.thoughtworks.dbmigrator.plugin.repository

import java.io.File
import java.sql.{Connection, DriverManager, SQLException}

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}



trait Repository extends LazyLogging{

  def getConnection: Connection

  def getMigrationPath: String

  def filesFromPathFilter(path: File): List[File]

  def recordDelta(deltaFile: Delta): Try[Delta]

  def getLastRunDelta: Try[Long]

  def runMigration(delta: Delta): Try[Unit]

  private def runMigrations(connection: Connection)(migrationFile: List[Delta]): Seq[Try[Unit]] = {
    val triedMigrations = migrationFile.map {
      m =>
        recordDelta(m).flatMap(runMigration)
    }
    triedMigrations
  }

  def prepareForMigration(repo: Repository):  Try[Repository]

  def getAllMigrations: List[Try[Delta]] = {
    filesFromPathFilter(new File(getMigrationPath)).map(f => Try {
      Delta(f)
    })
  }

  def execute() : Try[Unit] = {
    logger.debug("Executing migrator")
    prepareForMigration(this).map {
      repo =>

        val ignoreNonConformingMigrations = getAllMigrations.filter(_.isSuccess).map {
          _.get
        }

        logger.debug(s"Conforming migrations: ${ignoreNonConformingMigrations.mkString}")

        val deltasToRun = getLastRunDelta.map {
          lrd =>
            ignoreNonConformingMigrations.filter(d => d.version > lrd)
        }

        deltasToRun.map {
          runnableDeltas =>
            repo.runMigrations(getConnection)(runnableDeltas)
        }

    }
  }
}

case class MySQLRepository(path: String, changeLogTable: String, jdbcUrl: String, username: String, password: String) extends Repository {

  override def getConnection: Connection = {
    DriverManager.getConnection(jdbcUrl, username, password)
  }

  override def recordDelta(deltaFile: Delta): Try[Delta] = {
    Try {
      logger.debug(s"Logging delta ${deltaFile}")
      val connection = getConnection
      val stmt = connection.createStatement
      val query = s"INSERT INTO ${changeLogTable} (version,name,runAt) VALUES(${deltaFile.version}, '${deltaFile.name}', NOW())"
      stmt.execute(query)
      deltaFile
    }
  }

  override def getLastRunDelta: Try[Long] = {
    Try {
      val statement = getConnection.createStatement
      val query = s"SELECT * FROM ${changeLogTable} ORDER BY version DESC"
      val resultSet = statement.executeQuery(query)
      if (resultSet.next()) resultSet.getLong("version") else 0
    }
  }

  override def runMigration(delta: Delta): Try[Unit] = {
    Try {
      val statement = getConnection.createStatement
      val migrationString = delta.read()
      logger.debug(s"Running migration query: ${migrationString}")
      statement.execute(migrationString)
    }
  }

  override def getMigrationPath: String = path

  def filesFromPathFilter(path: File): List[File] = {
    val cwdFiles = path.listFiles
    logger.debug(s"List of files in dir: ${cwdFiles}" )
    (cwdFiles ++ cwdFiles.filter(_.isDirectory).flatMap(filesFromPathFilter)).toList
  }

  override def prepareForMigration(repo: Repository) = {

      Try {
        logger.debug("Creating changelog")
        val statement = repo.getConnection.createStatement()
        val query = s"CREATE TABLE IF NOT EXISTS ${changeLogTable} (version BIGINT NOT NULL, name VARCHAR(255), runAt TIMESTAMP, PRIMARY KEY (version))"
        logger.debug(s"Running changelog create query : ${query}")
        statement.execute(query)
        repo
      }
  }
}
