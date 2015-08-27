package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import jdub.async.Database
import models.queries.ProfileQueries
import models.user.User
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object ProfileSearchService {
  def retrieve(id: UUID): Future[List[CommonSocialProfile]] = Database.query(ProfileQueries.FindProfilesByUser(id))

  def retrieve(provider: String, key: String): Future[Option[CommonSocialProfile]] = Database.query(ProfileQueries.FindProfile(provider, key))
  
}