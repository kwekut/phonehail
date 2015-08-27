// package models.cloudinary

// import scala.util.Try
// import org.joda.time.DateTime
// import cloudinary.model.CloudinaryResource
// import cloudinary.model.CloudinaryResource.preloadedFormatter
// import org.joda.time.DateTime
// import com.cloudinary.parameters.UploadParameters
// import com.cloudinary.Implicits._
// import com.cloudinary.Cloudinary
// //import models.photo.{ Photo, PhotoDetails }
// import play.api.libs.concurrent.Execution.Implicits._
// import play.api.Logger
// //import com.cloudinary.Util.definedMap


// class CloudinaryImpl extends CloudinaryService {

//   val cloudinary = new Cloudinary(Map(
// 	  "cloud_name" -> "n07t21i7",
// 	  "api_key" -> "123456789012345",
// 	  "api_secret" -> "abcdeghijklmnopqrstuvwxyz12"
// 	))

// //Future[com.cloudinary.response.UploadResponse]
// 	def uploadImage(file: String) = {
// 		import java.io.File
// 	cloudinary.uploader().upload(new File(file), UploadParameters().faces(true).colors(true).imageMetadata(true).exif(true))

// 	}


// //Deprecated: requires plugin which conflicts with guice
// 	def uploadPhoto(file: String) = {
// 		import java.io.File
//           CloudinaryResource.upload(new File(file), UploadParameters().faces(true).colors(true).imageMetadata(true).exif(true)).map {
//             resource =>
// 				resource.url()
//           }
//     }

// }