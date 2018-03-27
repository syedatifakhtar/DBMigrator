package com.thoughtworks.dbmigrator.plugin.repository

import java.io.File

case class Delta(file: File) {
  private val splitFile: Array[String] = file.getName.split("#")
  val version: Long = splitFile(0).toLong
  val name = splitFile(1)

  def read() = {
    scala.io.Source.fromFile(file).getLines().mkString
  }
}
