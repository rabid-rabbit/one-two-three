package com.sungevity.util

object Int {


  implicit class IntRichInt(i: Int) {

    def toRange(r: Range): Int = {
      math.min(math.max(r.min, i), r.max)
    }

  }
}
