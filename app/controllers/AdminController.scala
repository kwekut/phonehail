package controllers

import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import models.user.{ User, UserForms, AdminUserUpdateData, AdminSearchData, Role }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import java.util.UUID
import org.joda.time.LocalDateTime
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (
  val messagesApi: MessagesApi, 
  val env: AuthenticationEnvironment) extends Silhouette[User, CookieAuthenticator] {
  

  def adminIndex = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          Future.successful(Ok(views.html.adminindex(request.identity)))
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        case None =>
          Future.successful(Redirect(controllers.routes.HomeController.index()))
      }    
  }

  def adminSearch = SecuredAction { implicit req =>
    UserForms.adminSearchForm.bindFromRequest.fold(
      form => Redirect(routes.AdminController.adminUserList("")),
      filter => Redirect(routes.AdminController.adminUserList(filter))
    )
  }

  def adminUserList(filter:String="1")  = SecuredAction.async {
    env.identityService.search(filter).flatMap {
     users => Future.successful {Ok(views.html.adminuserlist(users, UserForms.adminSearchForm))}
    }
  }

  def adminUserShow(userId: UUID) = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
            env.identityService.retrieve(userId).flatMap {
              case Some(user) => Future.successful(Ok(views.html.adminusershow(user))) 
              case None => Future.successful(Redirect(controllers.routes.AdminController.adminUserList(""))) 
          }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  }


  def adminUserUpdateForm(userId: UUID) = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          env.identityService.retrieve(userId).flatMap {
            case Some(user) => 
        val info = AdminUserUpdateData(user.username.getOrElse(""), user.roles.mkString, user.email.getOrElse(""), user.phone.getOrElse(""), user.address.getOrElse(""), user.fullName.getOrElse(""), user.hasstripe.getOrElse(""), user.preferences.getOrElse(""))
        val filledForm = UserForms.adminUserUpdateForm.fill(info)
        Future.successful(Ok(views.html.adminuserupdate(request.identity, filledForm))) 
            case None =>
        Future.successful{Redirect(controllers.routes.AdminController.adminUserList(""))} 
        }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
      case None =>
        Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }

  def adminUserUpdate(userId: UUID) = SecuredAction.async { implicit request =>
    UserForms.adminUserUpdateForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.adminuserupdate(request.identity, form))),
      data => {
        env.identityService.retrieve(request.identity.id).flatMap {
          case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          env.identityService.retrieve(userId).flatMap {
            case Some(user) =>  updateUser(data, user)

            case None =>
              Future.successful{Redirect(controllers.routes.AdminController.adminUserList(""))}
          }} else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
          case None =>  Future.successful {  Ok(views.html.index())  }
        }
      }
    )
  }


  private[this] def updateUser(data: AdminUserUpdateData, user: User)(implicit request: SecuredRequest[AnyContent]) = {
    val position = Role(data.roles) 
    

    val updateduser = user.copy(
      username = Some(data.username),
      roles =  Set(position),
      email =  Some(data.email),
      phone =  Some(data.phone),
      address = Some(data.address),
      fullName =  Some(data.fullName),
      hasstripe =  Some(data.hasstripe),
      preferences =  Some(data.preferences)
    )
    for {
      u <- env.userService.save(updateduser, update = true)
    } yield {
      Redirect(controllers.routes.AdminController.adminUserList(""))
    }
  }

}

