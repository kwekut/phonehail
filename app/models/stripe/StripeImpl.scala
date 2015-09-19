package models.stripe

import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.List
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import scala.util.Try
import java.util.UUID
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import javax.inject._
import com.google.inject.name.Named
import services.user.AuthenticationEnvironment
import play.api.Logger
import com.stripe.Stripe
import com.stripe.model.Charge
import com.stripe.model.Customer
import com.stripe.net.RequestOptions._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import com.google.common.collect.ImmutableMap
import com.stripe.exception.APIConnectionException
import com.stripe.exception.APIException
import com.stripe.exception.AuthenticationException
import com.stripe.exception.CardException
import com.stripe.exception.InvalidRequestException
import com.stripe.exception.StripeException
import com.stripe.model.Account
import com.stripe.model.Address
import com.stripe.model.Card

import com.stripe.model.ChargeRefundCollection
import com.stripe.model.Coupon
import com.stripe.model.Recipient
import com.stripe.model.Refund
import com.stripe.model.Subscription
import com.stripe.model.Token
import java.util.HashMap
import java.util.Map

object StripeImpl{
	case class DBException(message: String) extends Exception(message)
	case class CustomerChargeResponse(name: String, email: String, phone: String, amount: String, created: String, status: String)
	case class CustomerCreateResponse(name: String, email: String, phone: String, amount: String, created: String, status: String)
}

class StripeImpl @Inject() (val env: AuthenticationEnvironment) extends StripeService {
	import StripeImpl._
	//Set secret key
	//private val Stripe.apiKey = "sk_test_BQokikJOvBiI2HlWgH4olfQ2"
	private val requestOptions = (new RequestOptionsBuilder()).setApiKey("sk_test_BQokikJOvBiI2HlWgH4olfQ2").build()

	def createCustomer(userId: UUID, token: String) = {
		//Get customer - emmail, Get token from submit
		Logger.info(s"Creating stripe Customer $token")
	
	    env.identityService.retrieve(userId).flatMap {
	    	case Some(user) =>	Future.successful { 
	    		Logger.info("Saving stripe customer")

	    	val customerParams: HashMap[String, Object] = new HashMap()
				customerParams += ("description" -> user.fullName.getOrElse("Na"))
				customerParams += ("email" -> user.email.getOrElse("Na"))
				customerParams += ("source" -> token)
			Logger.info(s"Saving stripe customer customerParams: $customerParams")
			val customer = Customer.create(customerParams, requestOptions)
			Logger.info(s"stripe returned customer val: $customer")
			    val updateduser = user.copy( hasstripe =  Some(customer.getId) )
			    Logger.info(s"user copy stripe cus id val: $updateduser")
				env.userService.save(updateduser, update = true)

				CustomerCreateResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), customer.getAccountBalance.toString, customer.getCreated.toString, customer.getDelinquent.toString)
				
			}
	        case None => Future.successful { Logger.info("No User found from Database for stripe customer creation")
	        	CustomerCreateResponse("no user", "no email", "no phone", "unknown bal", "unknown date", "unknown delinquency")
	        }
	        			
	    }
	} 

//Charge the customer
	def chargeCustomer(phone: String, amt: java.lang.Integer) = {
		Logger.info(s"Charging stripe Customer $phone")
		env.identityService.retrievebyphone(phone).flatMap {
			case Some(user) => Future.successful {
				val chargeParams: HashMap[String, Object] = new HashMap()
					chargeParams += ("amount" -> amt)
					chargeParams += ("currency" -> "usd")
					chargeParams += ("customer" -> user.hasstripe.getOrElse("none"))
				val chargeRaw = Charge.create(chargeParams, requestOptions)
				//val charge: Charge = chargeRaw.capture(requestOptions)


				// Try {chargeRaw.capture(requestOptions)} map {
				//   case Success(charge) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), charge.getAmount.toString, charge.getCreated.toString, charge.getStatus.toString)
				//   case Failure(e) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "exception", "error", e.getMessage.mkString)
				// }


				val charger = Try( chargeRaw.capture(requestOptions) ) map { charge =>
					CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), charge.getAmount.toString, charge.getCreated.toString, charge.getStatus.toString)
				} recover {
					case e: CardException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "card exception", "error", e.getMessage.mkString)					
				    case e: InvalidRequestException =>  CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "invalid request", "error", e.getMessage.mkString)					
				    case e: AuthenticationException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "auth exception", "error", e.getMessage.mkString)					
				    case e: APIConnectionException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "api exception", "error", e.getMessage.mkString)					
				    case e: StripeException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "stripe exception", "error", e.getMessage.mkString)					
				    case e: Exception => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "exception", "error", e.getMessage.mkString)							
				}



				// try {
				// 	val charge: Charge = chargeRaw.capture(requestOptions)
				// 		CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), charge.getAmount.toString, charge.getCreated.toString, charge.getStatus.toString)

				// } catch {
				// 	case CardException(e) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "card exception", "error", e.getMessage.mkString)					
				//     case InvalidRequestException(e) =>  CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "invalid request", "error", e.getMessage.mkString)					
				//     case AuthenticationException(e) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "auth exception", "error", e.getMessage.mkString)					
				//     case APIConnectionException(e) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "api exception", "error", e.getMessage.mkString)					
				//     case StripeException(e) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "stripe exception", "error", e.getMessage.mkString)					
				//     case Exception(e) => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "exception", "error", e.getMessage.mkString)							
				// }
				charger
			}
			case None => Future.successful { Try ( 
				CustomerChargeResponse("unknown user", "unknown email", "unknown phone", "unknown amt", "unknown date", "unknown status")
			)}		
		}
	} 

//Refund a customer
	def refundCustomer(chargeId: String, amt: java.lang.Integer) = Try {
		Logger.info("Refund stripe Customer customer")

	val chargeParams: HashMap[String, Object] = new HashMap()
		chargeParams += ("amount" -> amt)
		chargeParams += ("currency" -> "usd")
		val charge = Charge.retrieve(chargeId, requestOptions)
		val refund = charge.getRefunds().create(chargeParams, requestOptions)
	refund
	} 
//Retrieve Customer
	def retrieveCustomer(phone: String) =Try {

	val customer = env.identityService.retrievebyphone(phone).flatMap {
		case Some(user) => Future.successful {
				val stripeId = user.hasstripe.getOrElse("none")
				Customer.retrieve(stripeId, requestOptions)
		}
		case None => Future.successful { Logger.info("No user from Database to hit stripe charge against") 
					throw DBException("No user from Database to hit stripe charge against")
		}	
	}	
	customer
	} 
//Delete Customer
	def deleteCustomer(userId: UUID) =Try {

		val user = env.identityService.retrieve(userId).flatMap {
			case Some(user) => Future.successful {
					val stripeId = user.hasstripe.getOrElse("none")
					val customer = Customer.retrieve(stripeId, requestOptions)
					customer.delete(requestOptions)
					user
			}
			case None => Future.successful { Logger.info("No user from Database to hit stripe charge against") 
						throw DBException("No user from Database to hit stripe charge against")
			}	
		}	
		user
	} 


//Retrieve and Update customer
	def updateCustomer(userId: UUID, token: String) = Try {

		val customer = env.identityService.retrieve(userId).flatMap {
		    case Some(user) =>	Future.successful { Logger.info("Updating stripe customer")

		    	val customerParams: HashMap[String, Object] = new HashMap()
					customerParams += ("description" -> user.fullName.getOrElse("Na"))
					customerParams += ("email" -> user.email.getOrElse("Na"))
					customerParams += ("source" -> token)
				val customer = Customer.retrieve(user.hasstripe.getOrElse("none"))
					customer.update(customerParams, requestOptions)
					customer
			}
		    case None => Future.successful { Logger.info("No User found from Database for stripe customer creation")
		    			throw DBException("No User found from Database for stripe customer creation")
		    }    	
	    }
	customer
	} 



	def listCharges(limit: java.lang.Integer) = {
			val chargeParams: HashMap[String, Object] = new HashMap()
				chargeParams += ("limit" -> limit)
				Charge.all(chargeParams, requestOptions)

			// 	val charger = Try( Charge.all(chargeParams, requestOptions) ) map { charge =>
			// 		CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), charge.getAmount.toString, charge.getCreated.toString, charge.getStatus.toString)
			// 	} recover {
			// 		case e: CardException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "card exception", "error", e.getMessage.mkString)					
			// 	    case e: InvalidRequestException =>  CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "invalid request", "error", e.getMessage.mkString)					
			// 	    case e: AuthenticationException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "auth exception", "error", e.getMessage.mkString)					
			// 	    case e: APIConnectionException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "api exception", "error", e.getMessage.mkString)					
			// 	    case e: StripeException => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "stripe exception", "error", e.getMessage.mkString)					
			// 	    case e: Exception => CustomerChargeResponse(user.fullName.getOrElse("Na"), user.email.getOrElse("Na"), user.phone.getOrElse("Na"), "exception", "error", e.getMessage.mkString)							
			// 	}
			// charger
	}
}


	






