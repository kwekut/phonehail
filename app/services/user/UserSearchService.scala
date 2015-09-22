package services.user

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import jdub.async.Database
import models.queries.UserQueries
import models.user.User
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

//Retrieve User by various param types(overloaded)
object UserSearchService extends IdentityService[User] {

  def search(fullName: String, orderby: String = "full_name", limit: Option[Int] = Some(1000), offset: Option[Int] = Some(0)): Future[List[User]] = Database.query(UserQueries.search(fullName, orderby, limit, offset))

  def retrieve(id: UUID): Future[Option[User]] = Database.query(UserQueries.getById(Seq(id)))

  def retrieve(email: String): Future[Option[User]] = Database.query(UserQueries.FindUserByEmail(email))

  def retrievebyusername(username: String): Future[Option[User]] = Database.query(UserQueries.FindUserByUsername(username))

  def retrievebyphone(phone: String): Future[Option[User]] = Database.query(UserQueries.FindUserByPhone(phone))

  override def retrieve(loginInfo: LoginInfo) = if (loginInfo.providerID == "anonymous") {
    Database.query(UserQueries.getById(Seq(UUID.fromString(loginInfo.providerKey)))).map {
      case Some(dbUser) =>
        if (dbUser.profiles.nonEmpty) {
          Logger.warn(s"Attempt to authenticate as anonymous for user with profiles [${dbUser.profiles}].")
          None
        } else {
          Some(dbUser)
        }
      case None => None
    }
  } else {
    Database.query(UserQueries.FindUserByProfile(loginInfo))
  }
}
