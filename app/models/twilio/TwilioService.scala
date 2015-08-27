package models.twilio

import scala.util.Try
import com.twilio.sdk.resource.list.MessageList

 /**
 * Give access to the Twilio object.
 */	
trait TwilioService {
   /**
   * Sends sms to user
   *
   * @param to, The user to send to.
   * @return 
   */
	def sendSMS(to: String, msg: String, driverphone: String) 

   /**
   * Gets list of sms
   *
   * @return 
   */
	def getSMSList(num: String): MessageList
   /**
   * Gets an sms 
   *
   * @param msid, The sms message unige id.
   * @return 
   */
	def getSMS(msid: String) 
}