package models.user

import org.joda.time.LocalDateTime
import java.util.UUID

case class AdminDashUpdateData(
      id: String,
      clientname: String,
      clientemail: String,
      clientphone: String,
      clientzip: String,
      clientcity: String,
      clientstate: String,
      driverphone: String,
      drivername: String,
      drivercompany: String,
      pickuplocation: String,
      attendantnamecomment: String,
      chargedamount: String,
      chargecomment: String,
      other: String
)

