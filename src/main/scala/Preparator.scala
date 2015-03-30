package org.template.similaruser

import io.prediction.controller.PPreparator
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

class Preparator
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    new PreparedData(
      users = trainingData.users,
      userItems = trainingData.userItems,
      viewEvents = trainingData.viewEvents)
  }
}

class PreparedData(
  val users: RDD[(String, User)],
  val userItems: RDD[(String, UserItem)],
  val viewEvents: RDD[ViewEvent]
) extends Serializable
