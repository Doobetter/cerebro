package models

import controllers.auth.AuthRequest
import elastic.sql.HTTPElasticSQLClient
import exceptions.{MissingRequiredParamException, MissingTargetHostException}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}
import play.api.mvc.Cookies

case class CerebroRequest(val target: ElasticServer, body: JsValue, val user: Option[User]) {
  private var cookies :Cookies = null;
  private var x_webauth_user : String = null; // 统一登录用户名
  def getCookies(): Cookies = {
    return cookies
  }
  def getXWebAuthUser(): String = {
    return x_webauth_user
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
  private val  noprints =  Array("/navbar", "/overview","/cluster_changes","/connect","/sql","/sql/history","/rest","/rest/history")
  private val log = org.slf4j.LoggerFactory.getLogger(classOf[CerebroRequest])
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
    val cookies = request.cookies
    rq.cookies =  request.cookies
    var x_webauth_user: String = ""
    if(cookies!=null){
      // x_webauth_user=xxxx 单点登录 获取cookies 用户名
      val  x_webauth_user_cookie = cookies.get("x_webauth_user").orNull
      if (null!=x_webauth_user_cookie){
        x_webauth_user = x_webauth_user_cookie.value
      }
    }
    rq.x_webauth_user = x_webauth_user

    if(!noprints.contains(request.path)){
      log.info("login_user={}, path={},  rawQueryString={}, body={}", x_webauth_user,request.path,request.rawQueryString, body)
    }

    return rq
  }

}
