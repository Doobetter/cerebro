package controllers

import controllers.auth.AuthenticationModule
import exceptions.MissingTargetHostException
import javax.inject.Inject
import models.sql.ElasticSQLServer
import models.{CerebroResponse, ESAuth, Host, Hosts}
import play.api.libs.json.Json

import scala.concurrent.Future

class HostController @Inject()(val authentication: AuthenticationModule,
                               val hosts: Hosts) extends BaseController {

  def save = process { request =>
    val body = request.body
    val host = (body \ "host").asOpt[String].getOrElse(throw MissingTargetHostException)
    val name = (body \ "name").asOpt[String].getOrElse(host)
    val username = (body \ "username").asOpt[String]
    val password = (body \ "password").asOpt[String]
    val sqlUrl = (body \ "sqlUrl").asOpt[String]
    val sqlUsername = (body \ "sqlUsername").asOpt[String]
    val sqlPassword = (body \ "sqlPassword").asOpt[String]
    val sql = sqlUrl.map(ElasticSQLServer((_),sqlUsername,sqlPassword))
    val headersWhitelist = Seq.empty[String]
    val hostObj = (username, password) match {
      case (Some(username), Some(password)) => Host(host, Some(ESAuth(username, password)), sql, headersWhitelist)
      case _ =>  Host(host, None, sql, headersWhitelist)
    }
    val added = hosts.addHost(name, hostObj)
    if(added){
      Future.successful(CerebroResponse(200, Json.obj()))
    }else{
      Future.successful(CerebroResponse(500, Json.obj()))
    }

  }

}
