package controllers

import java.sql.Date
import java.text.SimpleDateFormat

import controllers.auth.AuthenticationModule
import dao._
import elastic.sql.HTTPElasticSQLClient
import elastic.{ElasticClient, Error, Success}
import javax.inject.Inject
import models.commons.Indices
import models.sql.ElasticSQLServer
import models.{CerebroResponse, Hosts}
import play.api.Logger
import play.api.libs.json.{JsArray, JsString, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import scala.util.control.NonFatal

class SqlController @Inject()(val authentication: AuthenticationModule,
                              val hosts: Hosts,
                              client: HTTPElasticSQLClient, elasticClient: ElasticClient,
                              sqlHistoryDAO: SqlHistoryDAO) extends BaseController {

  private val logger = Logger("application")
  private final val defualtElasticSQLServer = new ElasticSQLServer("http://localhost:9400/elastic/search/a4",None,None)

  def request = process { request =>

    val body = request.get("data") // sql语句
    //val cookies = request.getCookies()
    client.process(body, request.getXWebAuthUser(), request.target).map {
      case s: Success =>
        val bodyAsString = body
        val username = request.user.map(_.name).getOrElse("")
        val hostname = request.target.host.name;
        Try(sqlHistoryDAO.save(SqlRequest(bodyAsString, username, hostname, new Date(System.currentTimeMillis)))).recover {
          case DAOException(msg, e) => logger.error(msg, e)
        }
        CerebroResponse(s.status, s.body)

      case e: Error => CerebroResponse(e.status, e.body)
    }

  }

  def index = process { request =>
    elasticClient.getIndicesName(request.target).map {
      case Success(status, body) =>
        val data = Json.obj(
          "indices" -> Indices(body),
          "url"    -> request.target.host.sql.getOrElse(defualtElasticSQLServer).url
        )
        CerebroResponse(status, data)

      case Error(status, error) =>
        CerebroResponse(status, error)
    }
  }

  def history = process { request =>
    implicit val writes = Json.writes[RestRequest]
    val dateFormat = new SimpleDateFormat("dd/MM HH:mm:ss")
    sqlHistoryDAO.all(request.user.map(_.name).getOrElse(""), request.target.host.name).map {
      case requests =>
        val body = requests.map { request =>
          Json.obj(
            //"method" -> JsString(request.path), // ui method <--> back path
            "body" -> JsString(request.body),
            "created_at" -> JsString(dateFormat.format(request.createdAt))
          )
        }

        CerebroResponse(200, JsArray(body))
    }.recover {
      case NonFatal(e) =>
        CerebroResponse(500, Json.obj("Error" -> JsString(s"Error while loading requests history: ${e.getMessage}")))
    }
  }

}
