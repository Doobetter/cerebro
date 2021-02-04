package dao

import java.util.Date

import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal


@ImplementedBy(classOf[SqlHistoryDAOImpl])
trait SqlHistoryDAO {

  def all(username: String, hostname: String): Future[Seq[SqlRequest]]

  def save(entry: SqlRequest): Future[Option[String]]

  def clear(username: String): Future[Int]

}

class SqlHistoryDAOImpl @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                   config: Configuration) extends SqlHistoryDAO {

  private val max = config.getOptional[Int]("sql.history.size").getOrElse(50)

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  private val requests = TableQuery[SqlRequests]

  def all(username: String , hostname: String): Future[Seq[SqlRequest]] =
    dbConfig.db.run(requests.filter(_.username === username).filter( _.hostname === hostname ).sortBy(_.createdAt.desc).result).map { reqs =>
      reqs.map { r => SqlRequest(r.body, r.username, r.hostname, new Date(r.createdAt)) }
    }.recover {
      case NonFatal(e) => throw DAOException(s"Error loading requests for [$username]", e)
    }

  def save(req: SqlRequest): Future[Option[String]] =
    findByMd5(req.md5).flatMap {
      case Some(p) =>
        update(p, req.createdAt.getTime)
      case None    =>
        create(req).map { md5 => trim(req.username); md5 }
    }

  def clear(username: String): Future[Int] =
    dbConfig.db.run(requests.filter(_.username === username).delete).recover {
      case NonFatal(e) => throw DAOException(s"Error clearing all requests for [$username]", e)
    }

  private def findByMd5(md5: String): Future[Option[SqlRequests#TableElementType]] =
    dbConfig.db.run(requests.filter(_.md5 === md5).result.headOption).recover {
      case NonFatal(e) => throw DAOException(s"Error finding request with MD5 [$md5]", e)
    }

  private def update(req: SqlRequests#TableElementType, createdAt: Long): Future[Option[String]] = {
    val q = for { r <- requests if r.md5 === req.md5 } yield r.createdAt
    val action = q.update(createdAt)
    dbConfig.db.run(action).map { _ => Some(req.md5) }.recover {
      case NonFatal(e) => throw DAOException(s"Error while updating request [$req]", e)
    }
  }

  private def create(req: SqlRequest): Future[Option[String]] = {
    val newReq = HashedSqlRequest(req.body, req.username, req.hostname, req.createdAt.getTime, req.md5)
    val action = requests returning requests.map(_.id) += newReq
    dbConfig.db.run(action).map { _ => Some(newReq.md5) }.recover {
      case NonFatal(e) => throw DAOException(s"Error while storing request [$req]", e)
    }
  }

  private def trim(username: String): Unit = {
    val action = sqlu"""
          DELETE FROM sql_requests WHERE id IN (
            SELECT id FROM sql_requests WHERE username=$username ORDER BY created_at DESC LIMIT -1 OFFSET $max
          )
        """
    dbConfig.db.run(action).recover {
      case NonFatal(e) => throw DAOException(s"Error while triming history for [$username]", e)
    }
  }

}
