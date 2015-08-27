// package controllers

// import play.api.i18n.MessagesApi
// import services.user.AuthenticationEnvironment
// import models.user.{ User, UserForms }
// //import models.photo.{ Photo, PhotoDetails }
// import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
// import play.api.i18n.I18nSupport
// import com.mohiva.play.silhouette.api._
// import play.api.i18n.MessagesApi
// import play.api.libs.concurrent.Execution.Implicits._
// import play.api.mvc.AnyContent
// import play.api.mvc.Action
// import scala.util.{Success, Failure}
// import models.cloudinary.{ CloudinaryService, CloudinaryImpl } 
// import scala.concurrent.Future
// import play.api.Logger

// @javax.inject.Singleton
// class ImageController @javax.inject.Inject() (val cloudinarySer: CloudinaryService, val messagesApi: MessagesApi, val env: AuthenticationEnvironment) extends Silhouette[User, CookieAuthenticator] {
  
//   def imageForm = UserAwareAction.async { implicit request =>
//     request.identity match {
//       case Some(user) =>  Future.successful { Ok(views.html.image(UserForms.imageForm)) }
//       case None => Future.successful { Redirect(controllers.routes.HomeController.index()) }
//     }
//   }

//       def upload = SecuredAction.async { implicit request =>
//           env.identityService.retrieve(request.identity.id).flatMap {
//             case Some(currentUser) => 
//             // request.body.asMultipartFormData.get.file("picture").map { picture =>
//             //     import java.io.File
//             //     val filename = picture.filename
//             //     val contentType = picture.contentType
//             //     picture.ref.moveTo(new File(s"C:/Users/kweku/Desktop/$filename"))
//               // if (new java.io.File(s"C:/Users/kweku/Desktop/$filename").exists) {
//               //   Logger.info("image entered")
//               //   val uploadResult = cloudinarySer.uploadImage(s"C:/Users/kweku/Desktop/$filename")
//               //   uploadResult.onComplete {
//               //       case Success(response) => 
//               //           Logger.info("Adding image url to user")
//               //               val updateduser = currentUser.copy(
//               //                 image =  Some(response.url)
//               //               )
//               //               env.userService.save(updateduser, update = true)
//               //       case Failure(ex) => 
//               //     }
//                 Future.successful {Redirect(controllers.routes.HomeController.index())}
//               //   //Future.successful {  Ok(views.html.index())  }
//               // } else {
//               //   Logger.info("Cliams no image entered")
//               //   Future.successful { Ok(views.html.image(UserForms.imageForm)).flashing("error" -> "Must supply photo") }
//               // }
//               // }.getOrElse {
//               // Future.successful { Ok(views.html.image(UserForms.imageForm)).flashing("error" -> "Must supply photo") }
//               //}
//             case None => Logger.info("Non existing user calling for user image update") 
//                    Future.successful {Ok(views.html.index())}
//               }  
//             }
// }

//   // def upload = SecuredAction.async { implicit request =>

//   //     	env.identityService.retrieve(request.identity.id).flatMap {
//   //     		case Some(currentUser) => Logger.info(currentUser.username.getOrElse("empty") + "  userimage-form loggedin user check sucess")
// 		//         val body = request.body.asMultipartFormData
// 		//         val resourceFile = body.get.file("photo")
// 		//         if (resourceFile.isEmpty) {
//   //             Logger.info("Cliams no image entered")
// 		//           Future.successful { Ok(views.html.image(UserForms.imageForm)).flashing("error" -> "Must supply photo") }
// 		//         } else {
//   //             Logger.info("Starting CloudinaryResource")
// 		//           CloudinaryResource.upload(resourceFile.get.ref.file, UploadParameters().faces(true).colors(true).imageMetadata(true).exif(true)).map {
// 		//             cr =>
// 		//               val photo = Photo(0, currentUser.username.getOrElse("empty"), cr, cr.data.get.bytes.toInt, DateTime.now)
// 		//               updateUser(photo.thumbnailUrl, currentUser)
// 		//           }
// 		//         }
//   //         	case None => Logger.info("Non existing user calling for user image update") 
//   //                     Future.successful {  Ok(views.html.index())  }
// 		// }
//   // }

// build   "com.cloudinary" %% "cloudinary-scala-play" % "0.9.7-SNAPSHOT",
//views/userprofile   <li><a href='@routes.ImageController.imageForm'>Edit Image</a></li>


// @(form: Form[models.user.ImageData])(
//   implicit request: Request[AnyContent], session: Session, flash: Flash, messages: Messages
// )@layout.bootstrap("Register") {
//   <div class="sign-up">
//     @flash.get("error").map { e =>
//       <div class="error">@e</div>
//     }

//     <nav id="main-navbar" class="navbar navbar-default navbar-fixed-top" role="navigation">
//         <a class="navbar-brand">Thumbnail</a>

//         <ul class="nav navbar-nav">
//             <li><a href='@routes.ProfileController.userprofile'>Your Profile</a></li>
//             <li><a href='@routes.HomeController.index'>Back To Home</a></li>
//             <li><li><a href='@routes.AuthenticationController.signOut'><i class="fa fa-power-off fa-6"></i>SignOut</a></li>
//         </ul>
//     </nav>
//   <div class="container">
//   @helper.form(action = routes.ImageController.upload, 'enctype -> "multipart/form-data") {
      
//       <input type="file" name="picture">
      
//       <p>
//           <input type="submit">
//       </p>    
//   }
// </div>
// }

