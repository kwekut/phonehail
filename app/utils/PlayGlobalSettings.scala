package utils

import java.util.TimeZone
import java.net.URI

import jdub.async.Database
import org.joda.time.DateTimeZone
import play.api.{ Application, GlobalSettings }
import services.database.Schema
import play.api.Logger

object PlayGlobalSettings extends GlobalSettings {
  override def onStart(app: Application) = {
    DateTimeZone.setDefault(DateTimeZone.UTC)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    val defurl = play.api.Play.current.configuration.getString("db.default.url").get

    val dbUri = new URI(defurl)
    val host = dbUri.getHost()
    val port = dbUri.getPort()
    val database = Some(dbUri.getPath().stripPrefix("/"))
    val username = dbUri.getUserInfo().split(":")(0)
    val pword = dbUri.getUserInfo().split(":")(1)
    val password = Some(pword)


    Database.open(username, host, port, password, database)
    Schema.update()

    super.onStart(app)
  }

  override def onStop(app: Application) = {
    Database.close()
    super.onStop(app)
  }
}






