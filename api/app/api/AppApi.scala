package api

import javax.inject.Singleton

import com.payu.shorturl.Version

@Singleton
class AppApi extends Api {

  def currentVersion = Action {
    Ok(Version.current)
  }

  def ping = Action {
    Ok
  }
}
