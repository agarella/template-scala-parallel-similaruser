package org.template.similaruser

import io.prediction.controller.PPreparator
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    new PreparedData(
      users = trainingData.users,
      similarUsers = trainingData.similarUsers,
      viewEvents = trainingData.viewEvents)
  }
}

class PreparedData(
  val users: RDD[(String, User)],
  val similarUsers: RDD[(String, User)],
  val viewEvents: RDD[ViewEvent]
) extends Serializable
