package controllers.auth.basic

import controllers.auth.AuthConfig
import play.api.Configuration

class BasicAuthConfig(config: Configuration) extends AuthConfig {

  implicit val conf = config

  final val username = getSetting("username")
  final val password = getSetting("password")

  final val normal_user = getSetting("normal_user")
  final val normal_upwd = getSetting("normal_upwd")
}
