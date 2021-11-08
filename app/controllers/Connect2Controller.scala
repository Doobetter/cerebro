package controllers

import controllers.auth.AuthenticationModule
import elastic.{ElasticClient, Error, Success}
import javax.inject.Inject
import models.{CerebroResponse, Hosts}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Connect2Controller @Inject()(val authentication: AuthenticationModule,val hosts: Hosts) extends BaseController {

  def index = process { request =>
    val body =  Json.obj("username" -> request.user.get.name)

    Future.successful(CerebroResponse(200, body))
  }

}
