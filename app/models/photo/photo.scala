// package models.photo

// import org.joda.time.DateTime
// import cloudinary.model.CloudinaryResource
// import com.cloudinary.Transformation
// import com.cloudinary.Implicits._

// case class PhotoDetails(title: String)

// case class Photo(image:CloudinaryResource, bytes:Int, createdAt:DateTime) {
//   def url = image.url()
//   def thumbnailUrl = image.url(Transformation().w_(150).h_(150).c_("fit").quality(80))
// }