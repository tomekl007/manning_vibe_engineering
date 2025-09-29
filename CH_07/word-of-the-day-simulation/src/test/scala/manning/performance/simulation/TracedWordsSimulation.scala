package manning.performance.simulation

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Traced version of WordsSimulation that targets the traced endpoints
 * to collect performance metrics and identify hot paths.
 * 
 * Run with:
 *   mvn gatling:test
 *
 * This simulation will help identify:
 * - Method execution times
 * - File I/O bottlenecks
 * - String operation overhead
 * - Request-level performance metrics
 */
class TracedWordsSimulation extends Simulation {

  // ---- HTTP protocol ----
  private val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  // ---- Feeder ----
  private val feeder = csv("words.csv").eager.circular

  // Expression form is bulletproof: no risk of "${word}" being treated literally
  private val wordExpr: Expression[String] = s => s("word").as[String]

  // ---- Requests ----
  object TracedWordOfTheDay {
    val get =
      http("traced-word-of-the-day")
        .get("/traced-words/word-of-the-day")
        .check(status.is(200))
  }

  object TracedValidateWord {
    val validate =
      feed(feeder)
        // optional logging to confirm what's fed
        .exec { s =>
          println(s"[traced-feeder] word = ${s("word").as[String]}")
          s
        }
        .exec(
          http("traced-word-exists")
            .get("/traced-words/word-exists")
            .queryParam("word", wordExpr) // use Expression, not a raw EL string
            .check(status.is(200))
        )
  }

  object MetricsEndpoint {
    val getMetrics =
      http("get-metrics")
        .get("/traced-words/metrics")
        .check(status.is(200))
        .check(bodyString.exists)
    
    val resetMetrics =
      http("reset-metrics")
        .get("/traced-words/reset-metrics")
        .check(status.is(200))
  }

  // ---- Scenarios ----
  private val tracedWordOfTheDayScenario =
    scenario("traced-word-of-the-day")
      .exec(TracedWordOfTheDay.get)

  private val tracedValidateScenario =
    scenario("traced-word-exists")
      .exec(TracedValidateWord.validate)

  // ---- Setup Phase ----
  private val setupScenario =
    scenario("setup-metrics")
      .exec(MetricsEndpoint.resetMetrics)

  // ---- Results Phase ----
  private val resultsScenario =
    scenario("collect-results")
      .exec(MetricsEndpoint.getMetrics)

  // ---- Injection profile ----
  setUp(
    // Setup: Reset metrics before test
    setupScenario.inject(
      atOnceUsers(1)
    ),
    
    // Main test: Same traffic pattern as original simulation
    tracedWordOfTheDayScenario.inject(
      constantUsersPerSec(1) during (1.minute)
    ),
    tracedValidateScenario.inject(
      constantUsersPerSec(20) during (1.minute)
    ),
    
    // Results: Collect metrics after test
    resultsScenario.inject(
      atOnceUsers(1)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.mean.lt(5000), // Mean response time should be less than 5 seconds
     global.responseTime.max.lt(10000), // Max response time should be less than 10 seconds
     global.successfulRequests.percent.gt(95) // 95% of requests should be successful
   )
}
