package com.sungevity.imageio

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.metadata.IIOMetadata
import javax.imageio.{ImageWriter, IIOImage, ImageIO, ImageReader}

case class Image(bufferedImage: BufferedImage, metadata: Option[IIOMetadata]) {

  def rgb(x: Int, y: Int) = bufferedImage.getRGB(x, y)

  def width = bufferedImage.getWidth

  def height = bufferedImage.getHeight

  def imageType = bufferedImage.getType

  def resize(w2: Int, h2: Int) = {

    val newImage = newBufferedImage(w2, h2)

    val xRatio = ((width<<16)/w2) + 1
    val yRatio = ((height<<16)/h2) + 1

    new Image(newImage, metadata).map {
      (x, y, argb) =>

        val x2 = math.min(width - 1, ((x*xRatio)>>16))
        val y2 = math.min(height - 1, ((y*yRatio)>>16))

        (x, y, rgb(x2, y2))
    }

  }

  def map[U](f: (Int, Int, Int) => (Int, Int, Int)): Image = flatMap (
    (x, y, argb) => Seq(f(x, y, argb))
  )

  def flatMap[U](f: (Int, Int, Int) => Iterable[(Int, Int, Int)]): Image = {

    val newImage = newBufferedImage(width, height)

    foreach {
      (x, y, argb) =>

        for {
          (newX, newY, newRGB) <- f(x, y, argb)
        } {
          newImage.setRGB(newX, newY, newRGB)
        }

    }

    new Image(newImage, metadata)

  }

  def foreach[U](f: (Int, Int, Int) => U): Unit = {

    for{
      y <- 0 until height
      x <- 0 until width
    } {
      f(x, y, rgb(x, y))
    }

  }

  def write(path: String)(implicit writer: ImageWriter): Unit = {

    val o = ImageIO.createImageOutputStream(new File(path));

    writer.setOutput(o)

    try {

      metadata match {
        case Some(metadata) => writer.write(new IIOImage(bufferedImage, null, metadata))
        case _ => writer.write(bufferedImage)
      }

    } finally {

      writer.dispose()

      o.flush()
      o.close()

    }

  }

  private def newBufferedImage(width: Int, height: Int) = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)

}

object Image {

  def apply(path: String)(implicit reader: ImageReader) = {

    val stream = ImageIO.createImageInputStream(new File(path))

    reader.setInput(stream)

    try {

      val imageMetadata = Option(reader.getImageMetadata(0))

      new Image(reader.read(0, reader.getDefaultReadParam), imageMetadata)

    } finally {

      reader.dispose
      stream.close

    }
  }

}
