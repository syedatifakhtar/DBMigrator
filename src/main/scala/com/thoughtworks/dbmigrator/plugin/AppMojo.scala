package com.thoughtworks.dbmigrator.plugin

import com.thoughtworks.dbmigrator.plugin.repository.MySQLRepository
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{Mojo, Parameter}

import scala.util.{Failure, Success}

@Mojo(name = "migrate")
class AppMojo extends AbstractMojo{

  @Parameter
  var pathToMigrations: String = null

  @Parameter
  var changeLogTableName: String = null

  @Parameter
  var jdbcUrl: String = null

  @Parameter
  var username: String = null

  @Parameter
  var password: String = null


  override def execute() = {
    println( "Running migrator" )
    println(s"Properties ${pathToMigrations}, ${changeLogTableName}, ${jdbcUrl}, ${username}, ${password}")
    val repository = MySQLRepository(pathToMigrations, changeLogTableName, jdbcUrl, username, password)
    repository.execute() match {
      case Failure(ex) =>
        throw ex
      case Success(s) =>
    }
  }
}

object AppMojo {


}
