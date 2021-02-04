package models

import models.sql.ElasticSQLServer

case class Host(name: String, authentication: Option[ESAuth] = None, sql: Option[ElasticSQLServer] =None, headersWhitelist: Seq[String] = Seq.empty)
