package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import org.joda.time.LocalDateTime
import org.joda.time.DateTime

case class Dash(
    id: UUID,
    clientname: Option[String],
    clientemail: Option[String],
    clientphone: Option[String],
    clientzip: Option[String],
    clientcity: Option[String],
    clientstate: Option[String],
    driverphone: Option[String],
    drivername: Option[String],
    drivercompany: Option[String],
    pickuplocation: Option[String],
    attendantnamecomment: Option[String],
    chargedamount: Option[String],
    chargecomment: Option[String],
    other: Option[String],
    created: LocalDateTime
) 


