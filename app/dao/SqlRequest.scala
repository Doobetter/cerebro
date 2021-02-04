package dao

import java.security.MessageDigest
import java.util.Date

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.Tag

case class HashedSqlRequest( body: String, username: String, hostname: String, createdAt: Long, md5: String)

case class SqlRequest( body: String, username: String, hostname: String, createdAt: Date) {

  val md5 = MessageDigest.getInstance("MD5").digest(
    (username + hostname + body ).getBytes
  ).map("%02x".format(_)).mkString

}

object SqlRequest {

  def apply(request: HashedSqlRequest): SqlRequest = {
    SqlRequest(request.body, request.username, request.hostname, new Date(request.createdAt))
  }

}

class SqlRequests(tag: Tag) extends Table[HashedSqlRequest](tag, "sql_requests") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def username = column[String]("username")

  def hostname  = column[String]("hostname")

  def body = column[String]("body")

  def md5 = column[String]("md5")

  def createdAt = column[Long]("created_at")

  def * = ( body, username, hostname, createdAt, md5) <> (HashedSqlRequest.tupled, HashedSqlRequest.unapply)

}
