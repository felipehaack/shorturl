package com.payu.shorturl.util

import java.security.MessageDigest

import scala.annotation.tailrec
import scala.language.{implicitConversions, postfixOps}

class ShortUrl {

  private val ALPHABET = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ"
  private val BASE = ALPHABET.length
  private val MD5 = MessageDigest.getInstance("MD5")

  private implicit def doubleToInt(n: Double): Int = n.toInt

  private implicit def doubleToLong(n: Double): Long = n.toLong

  private implicit def LongToInt(n: Long): Int = n.toInt

  def encode(input: Long): String = {

    @tailrec
    def encode(input: Long, result: List[Char]): List[Char] = {
      input match {
        case 0 => result
        case _ =>
          val module = input % BASE
          val position = Math.floor(input / BASE)
          encode(position, ALPHABET(module) +: result)
      }
    }

    encode(input, List()) mkString
  }

  def decode(input: String): Int = {

    @tailrec
    def decode(input: String, result: Int): Int = {

      input.length match {
        case 0 => result
        case _ =>
          val letter = ALPHABET.indexOf(input(0))
          val newLength = input.length - 1
          decode(input.substring(1), result + letter * Math.pow(BASE, newLength))
      }
    }

    decode(input, 0)
  }

  def toMD5(input: String): String = {

    MD5.digest(input.getBytes).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
}
