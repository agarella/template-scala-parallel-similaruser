package org.template.similaruser

import io.prediction.controller.IEngineFactory
import io.prediction.controller.Engine

case class Query(
  similarUsers: List[String],
  num: Int,
  whiteList: Option[Set[String]],
  blackList: Option[Set[String]]
) extends Serializable

case class PredictedResult(
  similarUserScores: Array[similarUserScore]
) extends Serializable

case class similarUserScore(
  similarUser: String,
  score: Double
) extends Serializable

object SimilarUserEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("als" -> classOf[ALSAlgorithm]),
      classOf[Serving])
  }
}
