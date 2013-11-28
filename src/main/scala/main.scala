import play.api.libs.json.Json
import scala.io.Source
import scala.reflect.io.Path
import scala.Some

/**
 * Small application to download and parse partition layout files.
 */
object main extends App {
  val URLLayoutList = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/codenames"

  val URLMTDDevicesList = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/mtd_devices"

  val URLBaseLayoutRaw = "https://raw.github.com/ameer1234567890/OnlineNandroid/master/part_layouts/raw/partlayout4nandroid."
  
  val brandList = Seq("Acer", "Alcatel", "Asus", "Barnes & Noble", "Commtiva", "Dell", "Geeksphone", "Google", "HP", "HTC", "Huawei", "LG", "Lenovo", "Micromax", "Motorola", "NVIDIA", "Pantech", "Samsung", "Sony", "Visual", "WonderMedia", "ZTE")

  case class Device(brandName:String, codeName: String, commercialName: String, var partitionTable: Map[String, String] = Map()) {
    override def toString = brandName + " (model: " + commercialName + ")\n" + partitionTable.mkString(", ")
    def getAddress = URLBaseLayoutRaw + codeName
  }

  object Device {
    implicit val format = Json.writes[Device]
  }

//  def getSeqPartition(UrlPartLayout: String): (String, String) = {
//    val map:(String, String) = _
//    UrlPartLayout match {
//      case u if u.startsWith("dev:") => None
//      case r"""(.+)$codeName: (.+")$t1(.+)$tail(")$t2""" => map(codeName) = tail
//      case u => throw new IllegalStateException("Not expected line in partition layout:\n" + u)
//    }
//    map
//  }

  def getDevice(s: String) = s match {
    case r"(.+)$codeName\t([A-Za-z]+)$brandName(.+)$commercialName" => Device(brandName, codeName, commercialName)
    case _ => throw new IllegalStateException("failed on: " + s)
  }

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  val pathToSaveJson = args match {
    case p if p.size == 0 => System.getProperty("user.dir")
    case p if p.size == 1 => p(0)
    case _ => println("Too many arguments.\nJust enter the path where you want to save the file."); sys.exit(1)
  }

  val result = Seq() ++ Source
    .fromURL(URLLayoutList)
    .getLines()
    .map(getDevice)
    .map(x => {
    x.partitionTable =
       Source
        .fromURL(x.getAddress)
        .getLines()
         .filter(p => !p.startsWith("dev:"))
        .map{_ match{
         case r"""(.+)$mountPoint: (.+")$t1(.*)$partition(".*)$t2""" => (partition, mountPoint)
         case t => throw new IllegalStateException("Error during parsing:\n" + t + "\n at the address:\n" + x.getAddress)
       }
       }

       .toMap
    x
  })
    .zipWithIndex
    .map(i => {
    println("Device " + i._2 + " " + i._1)
    i._1
  })

  val fileToSave = new java.io.File(pathToSaveJson, "onandroid.json")

  val JSonStringToSave = Json.prettyPrint(Json.toJson(result))

  Path(fileToSave).toFile.writeAll(JSonStringToSave)

  println(result.size + " devices parsed and saved to " + fileToSave.getAbsolutePath)

  //println(getListOfBrand)
  /**
   * Don't forget to add Barnes & Noble
   *
   * @return a list of String representing each available brand
   **/
  def getListOfBrand = Source.fromURL(URLLayoutList).getLines()
    .zip(Source.fromURL(URLMTDDevicesList).getLines())
    .flatMap(t => List(t._1, t._2))
    .map(getDevice)
    .map(x => x.commercialName.split(" ")(0))
    .toList
    .groupBy(i => i)
    .map(i => "\"" + i._1 + "\"")
    .toList
    .sortWith(_ < _)
    .mkString(", ")
}