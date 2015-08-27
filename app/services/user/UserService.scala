package services.user

import java.util.UUID
import org.joda.time.LocalDateTime
import com.github.mauricio.async.db.Connection
import com.mohiva.play.silhouette.api.AuthInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import jdub.async.Database
import models.queries._
import models.user.User
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import scala.concurrent.Future

object UserService {
  private[this] val log = LoggerFactory.getLogger(UserService.getClass)

  //If User with this profile's(loginInfo) exists, add/replace profiles link to User and update user 
  //User account being changed must belong to current User
  //If no User with this profile's(loginInfo) exists, then save profile, then update current User to link to it
  def create[A <: AuthInfo](profile: CommonSocialProfile): Future[User] = {
    log.info(s"Saving profile [$profile].")


    UserSearchService.retrieve(profile.loginInfo).flatMap {
      case Some(existingUser) =>
           val u = existingUser.copy(
            profiles = existingUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
          )
          save(u, update = true)

      case None =>     
        UserSearchService.retrieve(profile.email.getOrElse("none")).flatMap {
          case Some(existingUser) => 
              Database.execute(ProfileQueries.insert(profile)).flatMap { x =>
              val u = existingUser.copy(
                profiles = existingUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
              )
                save(u, update = true)
              }
          case None =>  
              Database.execute(ProfileQueries.insert(profile)).flatMap { x =>
          
              val currentUser = User(
                id = UUID.randomUUID(),
                username = None,
                profiles = Nil,
                fullName = None,
                email = profile.email,
                phone = None,
                address = None,
                hasstripe = None,
                preferences = None,
                image = None,
                created = new LocalDateTime()
              )
            val u = currentUser.copy(
              profiles = currentUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
            )
              save(u)
          }
        }
    }
  }

  def createprofile[A <: AuthInfo](currentUser: User, profile: CommonSocialProfile): Future[User] = {
    log.info(s"Updating profile [$profile].")
    UserSearchService.retrieve(profile.loginInfo).flatMap {
      case Some(existingUser) => Logger.info(existingUser.username.getOrElse("empty") + " - Userservice createprofile found user")
        if (existingUser.id == currentUser.id) {
          Database.execute(ProfileQueries.UpdateProfile(profile)).flatMap { x =>
          val u = currentUser.copy(
            profiles = existingUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
          )
          save(u, update = true)
          }
        } else {
          Future.successful(existingUser)
        }
      case None => Logger.info("userservice createprofile no found user")
        Database.execute(ProfileQueries.insert(profile)).flatMap { x =>
          val u = currentUser.copy(
            profiles = currentUser.profiles.filterNot(_.providerID == profile.loginInfo.providerID) :+ profile.loginInfo
          )
          save(u, update = true)
        }
    }
  }

  // Update a User,or else Insert a new User
  def save(user: User, update: Boolean = false): Future[User] = {
    val statement = if (update) {
      log.info(s"Updating user [$user].")
      UserQueries.UpdateUser(user)
    } else {
      log.info(s"Creating new user [$user].")
      UserQueries.insert(user)
    }
    Database.execute(statement).map { i =>
      user
    }
  }

  //Delete User, his Social Profiles, and Profile
  def remove(userId: UUID) = {
    val start = System.currentTimeMillis
    Database.transaction { conn =>
      for {
        profiles <- removeProfiles(userId, Some(conn)).map(_.length)
        users <- Database.execute(UserQueries.removeById(Seq(userId)), Some(conn))
      } yield Map(
        "users" -> users,
        "profiles" -> profiles,
        "timing" -> (System.currentTimeMillis - start).toInt
      )
    }
  }

  private[this] def removeProfiles(userId: UUID, conn: Option[Connection]) = Database.query(ProfileQueries.FindProfilesByUser(userId)).flatMap { profiles =>
    Future.sequence(profiles.map { profile =>
      (profile.loginInfo.providerID match {
        case "credentials" => Database.execute(PasswordInfoQueries.removeById(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)), conn)
        case "facebook" => Database.execute(OAuth2InfoQueries.removeById(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)), conn)
        case "google" => Database.execute(OAuth2InfoQueries.removeById(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)), conn)
        case "twitter" => Database.execute(OAuth1InfoQueries.removeById(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)), conn)
        case p => throw new IllegalArgumentException(s"Unknown provider [$p].")
      }).flatMap { infoCount =>
        Database.execute(ProfileQueries.remove(Seq(profile.loginInfo.providerID, profile.loginInfo.providerKey)), conn).map { i =>
          profile
        }
      }
    })
  }
}
