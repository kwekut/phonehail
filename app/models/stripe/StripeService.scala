package models.stripe

import scala.util.Try
import java.util.UUID
import scala.concurrent.Future
import StripeImpl._

 /**
 * Give access to the Twilio object.
 */	
trait StripeService {
   /**
   * Sends sms to user
   *
   * @param to, The user to send to.
   * @return 
   */
	def createCustomer(userId: UUID, token: String): Future[CustomerCreateResponse]

	def chargeCustomer(phone: String, amt: java.lang.Integer): Future[CustomerChargeResponse]

	def refundCustomer(chargeId: String): Future[CustomerRefundResponse]

   def retrieveCustomer(phone: String)

   def deleteCustomer(userId: UUID)

   def updateCustomer(userId: UUID, token: String)

   def listCharges(limit: java.lang.Integer)//: Future[Try[CustomerChargeResponse]]
}