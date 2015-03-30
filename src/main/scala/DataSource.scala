package org.template.similaruser

import grizzled.slf4j.Logger
import io.prediction.controller.{EmptyActualResult, EmptyEvaluationInfo, PDataSource, Params}
import io.prediction.data.storage.Storage
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

case class DataSourceParams(appId: Int) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val eventsDb = Storage.getPEvents()

    // create a RDD of (entityID, User)
    val usersRDD: RDD[(String, User)] = eventsDb.aggregateProperties(
      appId = dsp.appId,
      entityType = "user"
    )(sc).map { case (entityId, properties) =>
      val user = try {
        User()
      } catch {
        case e: Exception => {
          logger.error(s"Failed to get properties $properties of" +
            s" user $entityId. Exception: $e.")
          throw e
        }
      }
      (entityId, user)
    }.cache()

    // create a RDD of (entityID, SimilarUser)
    val similarUsersRDD: RDD[(String, User)] = eventsDb.aggregateProperties(
      appId = dsp.appId,
      entityType = "similarUser"
    )(sc).map { case (entityId, properties) =>
      val similarUser = try {
        User()
      } catch {
        case e: Exception => {
          logger.error(s"Failed to get properties $properties of" +
            s" similarUser $entityId. Exception: $e.")
          throw e
        }
      }
      (entityId, similarUser)
    }.cache()

    // get all "user" "view" "similarUser" events
    val viewEventsRDD: RDD[ViewEvent] = eventsDb.find(
      appId = dsp.appId,
      entityType = Some("user"),
      eventNames = Some(List("view")),
      // targetEntityType is optional field of an event.
      targetEntityType = Some(Some("similarUser")))(sc)
      // eventsDb.find() returns RDD[Event]
      .map { event =>
        val viewEvent = try {
          event.event match {
            case "view" => ViewEvent(
              user = event.entityId,
              similarUser = event.targetEntityId.get,
              t = event.eventTime.getMillis)
            case _ => throw new Exception(s"Unexpected event $event is read.")
          }
        } catch {
          case e: Exception => {
            logger.error(s"Cannot convert $event to ViewEvent." +
              s" Exception: $e.")
            throw e
          }
        }
        viewEvent
      }.cache()

    new TrainingData(
      users = usersRDD,
      similarUsers = similarUsersRDD,
      viewEvents = viewEventsRDD
    )
  }
}

case class User()


case class ViewEvent(user: String, similarUser: String, t: Long)

class TrainingData(
  val users: RDD[(String, User)],
  val similarUsers: RDD[(String, User)],
  val viewEvents: RDD[ViewEvent]
) extends Serializable {
  override def toString = {
    s"users: [${users.count()} (${users.take(2).toList}...)]" +
    s"similarUsers: [${similarUsers.count()} (${similarUsers.take(2).toList}...)]" +
    s"viewEvents: [${viewEvents.count()}] (${viewEvents.take(2).toList}...)"
  }
}
