package controllers

import javax.inject.Inject
import com.google.inject.name.Named
import akka.actor.ActorRef
import java.util.UUID
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import actors.WebSocketActor
import actors.TwilioActor._
import actors.CRMActor._
import actors.CommunicateActor._
import actors.AccountActor._
import actors.StripeActor._

import play.api.libs.concurrent.Execution.Implicits._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import services.user.AuthenticationEnvironment
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models.user.User
import play.api.Logger

// class WebSocketController @Inject() (
//     val messagesApi: MessagesApi,
//     val env: AuthenticationEnvironment,
//   @Named("twilio-actor") twilioActor: ActorRef,
//   @Named("crm-actor") crmActor: ActorRef,
//   @Named("stripesupervisor-actor") stripesupActor: ActorRef,
//   @Named("account-actor") accActor: ActorRef,
//   @Named("communicate-actor") commActor: ActorRef)
//   extends Silhouette[User, CookieAuthenticator] {


// 	def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
// 	implicit val req = Request(request, AnyContentAsEmpty)
// 	SecuredRequestHandler { securedRequest =>
// 	  Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
// 		}.map {
// 		  case HandlerResult(r, Some(user)) => Logger.info(s"socket controller recieved ${user.email}")
// 		  	Right(WebSocketActor.props(user.id, twilioActor, crmActor, commActor, accActor, stripesupActor) _)

// 		  case HandlerResult(r, None) => Logger.info("None user in socket controller")
// 		  	Left(r) 
// 		}
// 	}
// }




class WebSocketController @Inject() (
  @Named("twilio-actor") twilioActor: ActorRef,
  @Named("crm-actor") crmActor: ActorRef,
  @Named("stripesupervisor-actor") stripesupActor: ActorRef,
  @Named("account-actor") accActor: ActorRef,
  @Named("communicate-actor") commActor: ActorRef ) extends Controller {

	def socket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
		val userid = UUID.randomUUID()
  		WebSocketActor.props(userid, twilioActor, crmActor, commActor, accActor, stripesupActor, out)
	}

}