package models

import controllers.auth.AuthRequest
import exceptions.{MissingRequiredParamException, MissingTargetHostException}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}
import play.api.mvc.Cookies

case class CerebroRequest(val target: ElasticServer, body: JsValue, val user: Option[User]) {
  private var cookies :Cookies = null;
  def getCookes(): Cookies = {
    return cookies
  }

  def get(name: String) =
    (body \ name).asOpt[String].getOrElse(throw MissingRequiredParamException(name))

  def getOpt(name: String) =
    (body \ name).asOpt[String]

  def getInt(name: String) =
    (body \ name).asOpt[Int].getOrElse(throw MissingRequiredParamException(name))

  def getBoolean(name: String) =
    (body \ name).asOpt[Boolean].getOrElse(throw MissingRequiredParamException(name))

  def getArray(name: String) = (body \ name).asOpt[Array[String]].getOrElse(throw MissingRequiredParamException(name))

  def getObj(name: String) =
    (body \ name).asOpt[JsObject].getOrElse(throw MissingRequiredParamException(name))

  def getObjOpt(name: String) =
    (body \ name).asOpt[JsValue]

  def getOptArray(name: String): Option[JsArray] =
    (body \ name).asOpt[JsArray]

  def getAsStringArray(name: String): Option[Array[String]] =
    (body \ name).asOpt[Array[String]]

}

object CerebroRequest {

  def apply(request: AuthRequest[JsValue], hosts: Hosts): CerebroRequest = {
    val body = request.body

    // get value from cookies



    val hostName = (body \ "host").asOpt[String].getOrElse(throw MissingTargetHostException)
    val username = (body \ "username").asOpt[String]
    val password = (body \ "password").asOpt[String]

    val requestAuth = (username, password) match {
      case (Some(u), Some(p)) => Some(ESAuth(u, p))
      case _ => None
    }

    val server = hosts.getHost(hostName) match {
      case Some(host @ Host(h, a, s, headersWhitelist)) =>
        val headers = headersWhitelist.flatMap(headerName => request.headers.get(headerName).map(headerName -> _))
        ElasticServer(host.copy(authentication = a.orElse(requestAuth)), headers)
      case None => ElasticServer(Host(hostName, requestAuth))
    }
    var rq = CerebroRequest(server, body, request.user)
    rq.cookies =  request.cookies
    return rq
  }

}
