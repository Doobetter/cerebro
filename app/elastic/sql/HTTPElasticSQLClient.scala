package elastic.sql

import com.google.inject.Inject
import elastic.{ElasticResponse, Error}
import javax.inject.Singleton
import models.ElasticServer
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Cookies
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HTTPElasticSQLClient @Inject()(client: WSClient) {

  val TextContentType: (String, String) = ("Content-type", "text/plain")


  def process( body: String, target: ElasticServer) = {
    execute("POST", body, target, Seq(TextContentType), null)
  }
  def process( body: String, cookies: Cookies, target: ElasticServer) = {
    execute("POST", body, target, Seq(TextContentType), cookies)
  }


  protected def execute(method: String,
                        body: String,
                        target: ElasticServer,
                        headers: Seq[(String, String)] = Seq(), cookies: Cookies): Future[ElasticResponse] = {

    if (target.host.sql.isEmpty) {
      // 没有配置直接返回错误
      return Future {
        Error(404, Json.obj("error" -> "没有配置Elastic-SQL服务"))
      }
    }

    val url = s"${target.host.sql.get.url}"

    val sqlUsername = target.host.sql.get.username.getOrElse("cerebro");
    val sqlPassword = target.host.sql.get.password.getOrElse("cerebro");
    val mergedHeaders = headers ++ Seq((sqlUsername, sqlPassword))

    val request = client.url(url).withMethod(method).withHttpHeaders(mergedHeaders: _*).withBody(body).withRequestTimeout(6.seconds)
    if(cookies!=null){
      // x_webauth_user=xxxx 单点登录 获取cookies 用户名
      val  x_webauth_user = cookies.get("x_webauth_user").getOrElse("").toString()
      request.addQueryStringParameters("x_webauth_user" -> x_webauth_user)
    }

    val future = request.execute()
    return future.map { response => {
      //print(response.body)
      ElasticResponse(response)
    }

    }
  }
}
