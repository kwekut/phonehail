package controllers

import play.api.i18n.MessagesApi
import services.user.AuthenticationEnvironment
import models.user.{ User, Role }
import models.other.{ OtherForms, SetcallbackData, SendsmsmsgData, SendmmsmsgData, OptoutcustomerData, GetoutboundmsgsData, GetinboundmsgsData, GetcustomerinfoData, GetcustmsgsbymobileData, CreatestoreanduserData }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api._
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import play.api.mvc.AnyContent
import models.crm.{ CRMService, CRMImpl } 
import play.api.Logger
import java.util.UUID
import org.joda.time.LocalDateTime
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.util.{Success, Failure}

@javax.inject.Singleton
class CRMController @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val crmSer: CRMService, 
    val env: AuthenticationEnvironment) extends Silhouette[User, CookieAuthenticator] {
 

  def crmIndex = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crmindex()) }
          } else {
          	Future.successful(Redirect(controllers.routes.HomeController.index()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }


////////////////////////////////////////
  def sendSmsMsgForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crm.sendSmsMsg(OtherForms.sendsmsmsgForm)) }
          } else {
          	Future.successful(Redirect(controllers.routes.CRMController.crmIndex()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def sendSmsMsg = SecuredAction.async { implicit request =>
    Logger.info("sendSmsMsg called")
    OtherForms.sendsmsmsgForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.crm.sendSmsMsg(form))),
      data =>	crmSer.sendsmsmsg(data.phone, data.message) map { 
        result => Logger.info(result)
      Redirect(controllers.routes.CRMController.sendSmsMsgForm).flashing("error" -> result) 
      }                    
    )
  }


//////////////////////////////////
  def sendMmsMsgForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crm.sendMmsMsg(OtherForms.sendmmsmsgForm)) }
          } else {
          	Future.successful(Redirect(controllers.routes.HomeController.index()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }

  def sendMmsMsg = SecuredAction.async { implicit request =>
    OtherForms.sendmmsmsgForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.crm.sendMmsMsg(form))),
      data =>	crmSer.sendmmsmsg(data.phone, data.message, data.driverphone) map { result =>
                 Redirect(controllers.routes.CRMController.sendMmsMsgForm).flashing("error" -> result)            
      }
    )
  }

///////////////////////////
////////////////////////////////////////
  def sendCampaignForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crm.sendCampaign(OtherForms.sendcampaignForm)) }
          } else {
            Future.successful(Redirect(controllers.routes.CRMController.crmIndex()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def sendCampaign = SecuredAction.async { implicit request =>
    Logger.info("sendCampaign called")
    OtherForms.sendcampaignForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.crm.sendCampaign(form))),
      data => crmSer.sendcampaign(data.message) map { 
        result => Logger.info(result)
      Redirect(controllers.routes.CRMController.sendCampaignForm).flashing("error" -> result) 
      }                    
    )
  }


//////////////////////////////////
  def optInCustomerForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crm.optInCustomer(OtherForms.optincustomerForm)) }
          } else {
          	Future.successful(Redirect(controllers.routes.CRMController.crmIndex()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def optInCustomer = SecuredAction.async { implicit request =>
    OtherForms.optincustomerForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.crm.optInCustomer(form))),
      data => crmSer.optincustomer(data.phone, data.firstname, data.lastname) map { result =>
			    Redirect(controllers.routes.CRMController.optInCustomerForm).flashing("error" -> result)                   
      }
    )
  }


//////////////////////////////////////
  def optOutCustomerForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  Future.successful { Ok(views.html.crm.optOutCustomer(OtherForms.optoutcustomerForm)) }
      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def optOutCustomer = SecuredAction.async { implicit request =>
    OtherForms.optoutcustomerForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.crm.optOutCustomer(form))),
      data => crmSer.optoutcustomer(data.phone) map { result =>
        Redirect(controllers.routes.CRMController.optOutCustomerForm).flashing("error" -> result)             
      }
    )
  }




////////////////////////////////NOT USED////////////////////////////////////////
  def getCustMsgsByMobileForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  Future.successful { Ok(views.html.crm.getCustMsgsByMobile(OtherForms.getcustmsgsbymobileForm)) }
      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def getCustMsgsByMobile = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
		    OtherForms.getcustmsgsbymobileForm.bindFromRequest.fold(
		      form => Future.successful(BadRequest(views.html.crm.getCustMsgsByMobile(form))),
		      data => {	val result = crmSer.getcustmsgsbymobile(data.phone, data.startdate, data.enddate, data.startcount, data.endcount)
		                 Future.successful(Redirect(controllers.routes.CRMController.getCustMsgsByMobile))            
		      }
		    )	
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  }

///////////////////////////////NOT USED//////////////////////////////////////////////
  def getInboundMsgsForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  Future.successful { Ok(views.html.crm.getInboundMsgs(OtherForms.getinboundmsgsForm)) }
      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def getInboundMsgs = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
		    OtherForms.getinboundmsgsForm.bindFromRequest.fold(
		      form => Future.successful(BadRequest(views.html.crm.getInboundMsgs(form))),
		      data => {	val result = crmSer.getinboundmsgs(data.startdate, data.enddate)
		                 Future.successful(Redirect(controllers.routes.CRMController.getInboundMsgsForm))            
		      }
		    )	
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  } 

//////////////////////////////NOT USED////////////////////
  def getOutboundMsgsForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  Future.successful { Ok(views.html.crm.getOutboundMsgs(OtherForms.getoutboundmsgsForm)) }
      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def getOutboundMsgs = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
		    OtherForms.getoutboundmsgsForm.bindFromRequest.fold(
		      form => Future.successful(BadRequest(views.html.crm.getOutboundMsgs(form))),
		      data => {	val result = crmSer.getoutboundmsgs(data.startdate, data.enddate)
		                 Future.successful(Redirect(controllers.routes.CRMController.getOutboundMsgsForm))            
		      }
		    )	
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  } 

//////////////////////////
  def getCustomerInfoForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  Future.successful { Ok(views.html.crm.getCustomerInfo(OtherForms.getcustomerinfoForm)) }
      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def getCustomerInfo = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
		    OtherForms.getcustomerinfoForm.bindFromRequest.fold(
		      form => Future.successful(BadRequest(views.html.crm.getCustomerInfo(form))),
		      data => crmSer.getcustomerinfo(data.fieldval, data.value) map { result =>
		        Redirect(controllers.routes.CRMController.getCustomerInfoForm)            
		      }
		    )	 
        } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  }


//////////////////////////////////////
  def createStoreAndUserForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crm.createStoreAndUser(OtherForms.createstoreanduserForm)) }
          } else {
          	Future.successful(Redirect(controllers.routes.CRMController.crmIndex()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def createStoreAndUser = SecuredAction.async { implicit request =>
    //Logger.info("createStoreAndUser called")
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
          OtherForms.createstoreanduserForm.bindFromRequest.fold(
            form => Future.successful(BadRequest(views.html.crm.createStoreAndUser(form))),
            data => crmSer.createstoreanduser(data.storename, data.storekeyword, data.firstname, data.lastname, 
              data.email, data.storenumber, data.password) map { result =>
                Redirect(controllers.routes.CRMController.createStoreAndUserForm).flashing("error" -> result)            
            }
          )
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  }

////////////////////////
  def setCallBackForm = SecuredAction.async { implicit request =>
    env.identityService.retrieve(request.identity.id).flatMap {
      case Some(user) =>  if (user.roles.contains(Role.Admin)) {
            Future.successful { Ok(views.html.crm.setCallBack(OtherForms.setcallbackForm)) }
          } else {
          	Future.successful(Redirect(controllers.routes.CRMController.crmIndex()))
          }

      case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
    }
  }
  def setCallBack = SecuredAction.async { implicit request =>
      env.identityService.retrieve(request.identity.id).flatMap {
        case Some(admin) => if (admin.roles.contains(Role.Admin)) {
			    OtherForms.setcallbackForm.bindFromRequest.fold(
			      form => Future.successful(BadRequest(views.html.crm.setCallBack(form))),
			      data => crmSer.setcallback(data.callback) map { result => 
			          Redirect(controllers.routes.CRMController.setCallBackForm).flashing("error" -> result)            
			      }
			    )	 
          } else {Future.successful(Redirect(controllers.routes.HomeController.index()))}
        
        case None => Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
  }


}

