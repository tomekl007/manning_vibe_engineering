package manning.performance.simulation

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Run with:
 *   mvn gatling:test
 *
 * Files:
 *   - Simulation: src/test/scala/manning/performance/simulation/WordsSimulation.scala
 *   - Feeder CSV: src/test/resources/words.csv  (first line must be: word)
 *
 * Example words.csv:
 *   word
 *   cat
 *   house
 *   ...
 */
class WordsSimulation extends Simulation {

  // ---- HTTP protocol ----
  private val httpProtocol = http
    .baseUrl("http://localhost:8080/words")
    .acceptHeader("application/json")

  // ---- Feeder ----
  private val feeder = csv("words.csv").eager.circular

  // Expression form is bulletproof: no risk of "${word}" being treated literally
  private val wordExpr: Expression[String] = s => s("word").as[String]

  // ---- Requests ----
  object WordOfTheDay {
    val get =
      http("word-of-the-day")
        .get("/word-of-the-day")
        .check(status.is(200))
  }

  object ValidateWord {
    val validate =
      feed(feeder)
        // optional logging to confirm what's fed
        .exec { s =>
          println(s"[feeder] word = ${s("word").as[String]}")
          s
        }
        .exec(
          http("word-exists")
            .get("/word-exists")
            .queryParam("word", wordExpr) // use Expression, not a raw EL string
            .check(status.is(200))
        )
  }

  // ---- Scenarios ----
  private val wordOfTheDayScenario =
    scenario("word-of-the-day")
      .exec(WordOfTheDay.get)

  private val validateScenario =
    scenario("word-exists")
      .exec(ValidateWord.validate)

  // ---- Injection profile ----
  setUp(
    wordOfTheDayScenario.inject(
      constantUsersPerSec(1) during (1.minute)
    ),
    validateScenario.inject(
      constantUsersPerSec(20) during (1.minute)
    )
  ).protocols(httpProtocol)
}
