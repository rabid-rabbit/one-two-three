package com.sungevity

import java.awt.image.{DataBufferInt, BufferedImage}
import java.io.File

import com.sun.media.imageioimpl.plugins.tiff.{TIFFImageWriterSpi, TIFFImageReaderSpi}
import com.sungevity.imageio.Image
import com.typesafe.config.ConfigFactory

object Main extends App {

  import com.sungevity.util.Int._

  implicit val reader = new TIFFImageReaderSpi().createReaderInstance()

  implicit val writer = new TIFFImageWriterSpi().createWriterInstance()

  implicit val config = ConfigFactory.parseFile(new File(args(0)))

  val mapPath = config.getString("one-two-three.map.path")

  val inPath = config.getString("one-two-three.input.path")

  val outPath = config.getString("one-two-three.output.path")

  val l1 = config.getInt("one-two-three.map.l1").toRange(0 to 255)

  val l2 = config.getInt("one-two-three.map.l2").toRange(0 to 255)

  val in = Image(inPath)

  val map = Image(mapPath).map {

    (x, y, rgb) =>

      def threshold(c: Int): Int = if(c > l1 && c <= l2) c else 255

      def grayScale(rgb: Int): Int = {
        val r: Int = (rgb >> 16) & 0xff
        val g: Int = (rgb >> 8) & 0xff
        val b: Int = rgb & 0xff
        (r * 77 + g * 151 + b * 28) >> 8
      }

      def binary(c: Int): Int = if(c < 255) 0 else 255

      val gray = binary(threshold(grayScale(rgb)))

      (x, y, rgb & 0xff000000 | (gray << 16) | (gray << 8) | gray)
  }.resize(in.width, in.height)


  val out = in.map{

    (x, y, rgb) =>

      def isOn(rgb: Int) = {
        val r: Int = (rgb >> 16) & 0xff
        val g: Int = (rgb >> 8) & 0xff
        val b: Int = rgb & 0xff
        ((r + g + b) >> 8) == 0
      }

      if(isOn(map.rgb(x, y))){
        (x, y, in.rgb(x, y))
      } else {
        (x, y, 0x00FFFFFF & in.rgb(x, y))
      }

  }

  out.write(outPath)

  println("done")

}
