package models.other

import com.mohiva.play.silhouette.api.util.Credentials
import play.api.data._
import play.api.data.Forms._

object OtherForms {

  val sendsmsmsgForm = Form(
    mapping(
      "phone" -> nonEmptyText,
      "message" -> nonEmptyText
    )(SendsmsmsgData.apply)(SendsmsmsgData.unapply)
  )

  val sendmmsmsgForm = Form(
    mapping(
      "phone" -> nonEmptyText,
      "message" -> nonEmptyText,
      "driverphone" -> nonEmptyText
    )(SendmmsmsgData.apply)(SendmmsmsgData.unapply)
  )

  val optincustomerForm = Form(
    mapping(
      "phone" -> nonEmptyText(minLength = 10, maxLength = 10),
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText
    )(OptincustomerData.apply)(OptincustomerData.unapply)
  )
  
  val optoutcustomerForm = Form(
    mapping(
      "phone" -> nonEmptyText
    )(OptoutcustomerData.apply)(OptoutcustomerData.unapply)
  )

  val getcustomerinfoForm = Form(
    mapping(
      "fieldval" -> nonEmptyText,
      "value" -> nonEmptyText

    )(GetcustomerinfoData.apply)(GetcustomerinfoData.unapply)
  )

  val createstoreanduserForm = Form(
    mapping(
      "storename" -> nonEmptyText,
      "storeKeyword" -> nonEmptyText,
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "email" -> nonEmptyText,
      "storekeywordnumber" -> nonEmptyText,
      "password" -> nonEmptyText
    )(CreatestoreanduserData.apply)(CreatestoreanduserData.unapply)
  )

  val getcustmsgsbymobileForm = Form(
    mapping(
      "phone" -> nonEmptyText,
      "startdate" -> nonEmptyText,
      "enddate" -> nonEmptyText,
      "startcount" -> nonEmptyText,
      "endcount" -> nonEmptyText
    )(GetcustmsgsbymobileData.apply)(GetcustmsgsbymobileData.unapply)
  )

  val setcallbackForm = Form(
    mapping(
      "callback" -> nonEmptyText
    )(SetcallbackData.apply)(SetcallbackData.unapply)
  )

  val getinboundmsgsForm = Form(
    mapping(
      "startdate" -> nonEmptyText,
      "enddate" -> nonEmptyText
    )(GetinboundmsgsData.apply)(GetinboundmsgsData.unapply)
  )

  val getoutboundmsgsForm = Form(
    mapping(
      "startdate" -> nonEmptyText,
      "enddate" -> nonEmptyText
    )(GetoutboundmsgsData.apply)(GetoutboundmsgsData.unapply)
  )

}

// https://restapi.crmtext.com/smapi/rest?method=createstoreanduser&storename=&storeKeyword=&firstname&=lastname=&emailid= &storekeywordnumber=&password= 
// https://restapi.crmtext.com/smapi/rest?method=getcustmsgsbymobile&phone_number=&startdate=&enddate=&startcount=&endcount= 
// https://restapi.crmtext.com/smapi/rest?method=getcustomerinfo&phone_number= 
// https://restapi.crmtext.com/smapi/rest?method=getinboundmsgs&startdate=&enddate= 
// https://restapi.crmtext.com/smapi/rest?method=getoutboundmsgs&startdate=&enddate= 
// https://restapi.crmtext.com/smapi/rest?method=optincustomer&firstname=&lastname=&phone_number=
// https://restapi.crmtext.com/smapi/rest?method=optoutcustomer&phone_number=
// https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message=&mmsurl=
// https://restapi.crmtext.com/smapi/rest?method=sendsmsmsg&phone_number=&message= 
// https://restapi.crmtext.com/smapi/rest?method=setcallback&callback= 