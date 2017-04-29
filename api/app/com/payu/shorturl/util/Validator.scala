package com.payu.shorturl.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import com.payu.shorturl.exception.ShortUrlException
import com.payu.shorturl.model.Error
import com.wix.accord._

import scala.util.matching.Regex

object Validator {

  def input[T: Validator](value: T): Unit = {
    validate(value) match {
      case Success =>
      case Failure(violations) =>
        val errors = violations.map { v =>
          val code = Descriptions.render(v.description)
          Error(code, s"${v.value} ${v.constraint}")
        }
        ShortUrlException.invalidInputs(errors)
    }
  }

  def enum(enum: Enumeration, value: String, name: String): Unit = {
    enum.values.find(_.toString == value).getOrElse {
      ShortUrlException.invalidInput(name + ".enum")
    }
  }

  def nonEmpty(value: String, name: String = "param"): Unit = {
    if (value.isEmpty) {
      ShortUrlException.invalidInput(name + ".empty")
    }
  }

  def max(value: String, size: Int, name: String = "param"): Unit = {
    if (value.length > size) {
      ShortUrlException.invalidInput(name + ".max")
    }
  }

  def min(value: String, size: Int, name: String = "param"): Unit = {
    if (value.length < size) {
      ShortUrlException.invalidInput(name + ".min")
    }
  }

  def sizeBetween(value: String, min: Int, max: Int, name: String = "param"): Unit = {
    this.nonEmpty(value, name)
    this.min(value, min, name)
    this.max(value, max, name)
  }

  def size(value: String, size: Int, name: String = "param"): Unit = {
    if (value.length != size) {
      ShortUrlException.invalidInput(s"name.size")
    }
  }

  def pattern(value: String, regex: Regex, name: String = "param"): Unit = {
    if (regex.findFirstIn(value).isEmpty) {
      ShortUrlException.invalidInput(s"name.pattern")
    }
  }

  def maxDaysBetween(from: LocalDate, to: LocalDate, maxDays: Long, name: String) = {
    if (ChronoUnit.DAYS.between(from, to) > maxDays)
      ShortUrlException.invalidInput(s"$name.range")
  }

  def dateOlderThan(oldest: LocalDate, date: LocalDate, name: String) = {
    if (date.isBefore(oldest))
      ShortUrlException.invalidInput(s"$oldest.old")
  }

}