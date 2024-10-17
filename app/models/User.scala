package models

case class User(name: String, webAuthUser: Option[String] = None)
