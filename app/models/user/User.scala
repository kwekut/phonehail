package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import org.joda.time.LocalDateTime

case class User(
    id: UUID,
    username: Option[String],
    profiles: Seq[LoginInfo],
    roles: Set[Role] = Set(Role.User),
    fullName: Option[String],
    email: Option[String],
    phone: Option[String],
    street: Option[String],
    city: Option[String],
    state: Option[String],
    zip: Option[String],
    hasstripe: Option[String],
    preferences: Option[String],
    image: Option[String],
    created: LocalDateTime
) extends Identity {
  def isGuest = profiles.isEmpty
  def isAdmin = roles.contains(models.user.Role.Admin)
}
