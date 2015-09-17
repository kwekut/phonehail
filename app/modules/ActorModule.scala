package modules

import com.google.inject.{ AbstractModule, Provides }
import play.api.libs.concurrent.AkkaGuiceSupport
import models.twilio.{ TwilioService, TwilioImpl } 
import models.stripe.{ StripeService, StripeImpl } 
import models.crm.{ CRMService, CRMImpl } 
import play.api.Play
import play.api.Play.current
import com.google.inject.name.Names
import play.Logger
import actors.{TwilioActor, InBoundActor, CommunicateActor, AccountActor, StripeActor, PGActor, CRMActor} 

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class ActorModule extends AbstractModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  def configure() {
    Logger.debug("Binding actor implementations.")
    bind(classOf[TwilioService]).to(classOf[TwilioImpl])
    bind(classOf[StripeService]).to(classOf[StripeImpl])
    bind(classOf[CRMService]).to(classOf[CRMImpl])
    bindActor[InBoundActor]("inbound-actor")
    bindActor[CommunicateActor]("communicate-actor")
    bindActor[AccountActor]("account-actor")
    bindActor[TwilioActor]("twilio-actor")
    bindActor[CRMActor]("crm-actor")
    bindActor[StripeActor]("stripe-actor")
    bindActor[PGActor]("postgresql-actor")
  }

}
