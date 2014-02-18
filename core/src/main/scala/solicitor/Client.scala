package solicitor

import scala.concurrent.duration._
import scala.concurrent.{Await,Future}
import scala.util.Try

class Client(backend: Backend, timeout: Duration = Duration(1, SECONDS)) {

  /**
   * Returns true if the name supplied returns a true value. Merely a flattened
   * wrapper around getValueAsBoolean.
   * 
   * @param name The name to check
   * @param default A default value in the event of a failure to retrieve.
   */
  def isEnabled(name: String, default: Boolean = false): Boolean =
    getBoolean(name, Some(default)).getOrElse(false)

  /**
   * Returns false if the name supplied returns a true value.
   * 
   * @param name The name to check
   * @param default A default value in the event of a failure to retrieve.
   */
  def isDisabled(name: String): Boolean = !isEnabled(name)

  /**
   * Return a value for the given name.
   * 
   * @param name The name to fetch.
   * @param default A default value in the event of a failure to retrieve.
   */
  def getString(name: String, default: Option[String] = None): Option[String] =
    Try(Await.result(backend.getString(name), timeout)).getOrElse(default)

  /**
   * Return a value, converted to Boolean, for the given name.
   * 
   * @param name The name to fetch
   * @param default A default value in the event of a failure to retrieve.
   */
  def getBoolean(name: String, default: Option[Boolean] = None): Option[Boolean] =
    Try(Await.result(backend.getBoolean(name), timeout)).getOrElse(default)

  /**
   * Return a value, converted to Double, for the given name.
   * 
   * @param name The name to fetch
   * @param default A default value in the event of a failure to retrieve.
   */
  def getDouble(name: String, default: Option[Double] = None): Option[Double] =
    Try(Await.result(backend.getDouble(name), timeout)).getOrElse(default)

  /**
   * Closes any resources allocated by Solicitor and it's backends.
   */
  def shutdown {
    backend.shutdown
  }
}