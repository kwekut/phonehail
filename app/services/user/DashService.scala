package services.user

import java.util.UUID
import org.joda.time.LocalDateTime
import org.joda.time.DateTime
import com.github.mauricio.async.db.Connection
import com.mohiva.play.silhouette.api.AuthInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import jdub.async.Database
import models.queries._
import models.user.Dash
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import scala.concurrent.Future

object DashService {
  private[this] val log = LoggerFactory.getLogger(DashService.getClass)

  // Update a User,or else Insert a new User
  def save(dash: Dash, update: Boolean = false): Future[Dash] = {
    val statement = if (update) {
      log.info(s"Updating dash [$dash].")
      DashQueries.UpdateDash(dash)
    } else {
      log.info(s"Creating new dash [$dash].")
      DashQueries.insert(dash)
    }
    Database.execute(statement).map { i =>
      dash
    }
  }

  //Delete User, his Social Profiles, and Profile
  def remove(dashId: UUID) = {
    val start = System.currentTimeMillis
    Database.transaction { conn =>
      for {
        dashes <- Database.execute(DashQueries.removeById(Seq(dashId)), Some(conn))
      } yield Map(
        "dashes" -> dashes,
        "timing" -> (System.currentTimeMillis - start).toInt
      )
    }
  }

}
