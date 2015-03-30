package org.template.similaruser

import grizzled.slf4j.Logger
import io.prediction.controller.{P2LAlgorithm, Params}
import io.prediction.data.storage.BiMap
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.recommendation.{ALS, Rating => MLlibRating}

import scala.collection.mutable.PriorityQueue

case class ALSAlgorithmParams(
  rank: Int,
  numIterations: Int,
  lambda: Double,
  seed: Option[Long]) extends Params

class ALSModel(
  val userItemFeatures: Map[Int, Array[Double]],
  val userItemStringIntMap: BiMap[String, Int],
  val userItems: Map[Int, UserItem]
) extends Serializable {

  @transient lazy val userItemIntStringMap = userItemStringIntMap.inverse

  override def toString = {
    s" userItemFeatures: [${userItemFeatures.size}]" +
    s"(${userItemFeatures.take(2).toList}...)" +
    s" userItemStringIntMap: [${userItemStringIntMap.size}]" +
    s"(${userItemStringIntMap.take(2).toString}...)]" +
    s" userItems: [${userItems.size}]" +
    s"(${userItems.take(2).toString}...)]"
  }
}

/**
  * Use ALS to build userItem x feature matrix
  */
class ALSAlgorithm(val ap: ALSAlgorithmParams)
  extends P2LAlgorithm[PreparedData, ALSModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): ALSModel = {
    require(!data.viewEvents.take(1).isEmpty,
      s"viewEvents in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    require(!data.users.take(1).isEmpty,
      s"users in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    require(!data.userItems.take(1).isEmpty,
      s"userItems in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    // create User and userItem's String ID to integer index BiMap
    val userStringIntMap = BiMap.stringInt(data.users.keys)
    val userItemStringIntMap = BiMap.stringInt(data.userItems.keys)

    // collect UserItem as Map and convert ID to Int index
    val userItems: Map[Int, UserItem] = data.userItems.map { case (id, userItem) =>
      (userItemStringIntMap(id), userItem)
    }.collectAsMap.toMap

    val mllibRatings = data.viewEvents
      .map { r =>
        // Convert user and userItem String IDs to Int index for MLlib
        val uindex = userStringIntMap.getOrElse(r.user, -1)
        val iindex = userItemStringIntMap.getOrElse(r.userItem, -1)

        if (uindex == -1)
          logger.info(s"Couldn't convert nonexistent user ID ${r.user}"
            + " to Int index.")

        if (iindex == -1)
          logger.info(s"Couldn't convert nonexistent userItem ID ${r.userItem}"
            + " to Int index.")

        ((uindex, iindex), 1)
      }.filter { case ((u, i), v) =>
        // keep events with valid user and userItem index
        (u != -1) && (i != -1)
      }.reduceByKey(_ + _) // aggregate all view events of same user-userItem pair
      .map { case ((u, i), v) =>
        // MLlibRating requires integer index for user and userItem
        MLlibRating(u, i, v)
      }
      .cache()

    // MLLib ALS cannot handle empty training data.
    require(!mllibRatings.take(1).isEmpty,
      s"mllibRatings cannot be empty." +
      " Please check if your events contain valid user and userItem ID.")

    // seed for MLlib ALS
    val seed = ap.seed.getOrElse(System.nanoTime)

    val m = ALS.trainImplicit(
      ratings = mllibRatings,
      rank = ap.rank,
      iterations = ap.numIterations,
      lambda = ap.lambda,
      blocks = -1,
      alpha = 1.0,
      seed = seed)

    new ALSModel(
      userItemFeatures = m.productFeatures.collectAsMap.toMap,
      userItemStringIntMap = userItemStringIntMap,
      userItems = userItems
    )
  }

  def predict(model: ALSModel, query: Query): PredictedResult = {

    val productFeatures = model.userItemFeatures

    // convert userItems to Int index
    val queryList: Set[Int] = query.userItems.map(model.userItemStringIntMap.get(_))
      .flatten.toSet

    val queryFeatures: Vector[Array[Double]] = queryList.toVector
      // userItemFeatures may not contain the requested userItem
      .map { userItem => productFeatures.get(userItem) }
      .flatten

    val whiteList: Option[Set[Int]] = query.whiteList.map( set =>
      set.map(model.userItemStringIntMap.get(_)).flatten
    )
    val blackList: Option[Set[Int]] = query.blackList.map ( set =>
      set.map(model.userItemStringIntMap.get(_)).flatten
    )

    val ord = Ordering.by[(Int, Double), Double](_._2).reverse

    val indexScores: Array[(Int, Double)] = if (queryFeatures.isEmpty) {
      logger.info(s"No userItemFeatures vector for query userItems ${query.userItems}.")
      Array[(Int, Double)]()
    } else {
      productFeatures.par // convert to parallel collection
        .mapValues { f =>
          queryFeatures.map{ qf =>
            cosine(qf, f)
          }.reduce(_ + _)
        }
        .filter(_._2 > 0) // keep userItems with score > 0
        .seq // convert back to sequential collection
        .toArray
    }

    val filteredScore = indexScores.view.filter { case (i, v) =>
      isCandidateUserItem(
        i = i,
        userItems = model.userItems,
        queryList = queryList,
        whiteList = whiteList,
        blackList = blackList
      )
    }

    val topScores = getTopN(filteredScore, query.num)(ord).toArray

    val userItemScores = topScores.map { case (i, s) =>
      new UserItemScore(
        userItem = model.userItemIntStringMap(i),
        score = s
      )
    }

    new PredictedResult(userItemScores)
  }

  private
  def getTopN[T](s: Seq[T], n: Int)(implicit ord: Ordering[T]): Seq[T] = {

    val q = PriorityQueue()

    for (x <- s) {
      if (q.size < n)
        q.enqueue(x)
      else {
        // q is full
        if (ord.compare(x, q.head) < 0) {
          q.dequeue()
          q.enqueue(x)
        }
      }
    }

    q.dequeueAll.toSeq.reverse
  }

  private
  def cosine(v1: Array[Double], v2: Array[Double]): Double = {
    val size = v1.size
    var i = 0
    var n1: Double = 0
    var n2: Double = 0
    var d: Double = 0
    while (i < size) {
      n1 += v1(i) * v1(i)
      n2 += v2(i) * v2(i)
      d += v1(i) * v2(i)
      i += 1
    }
    val n1n2 = (math.sqrt(n1) * math.sqrt(n2))
    if (n1n2 == 0) 0 else (d / n1n2)
  }

  private
  def isCandidateUserItem(
    i: Int,
    userItems: Map[Int, UserItem],
    queryList: Set[Int],
    whiteList: Option[Set[Int]],
    blackList: Option[Set[Int]]
  ): Boolean = {
    whiteList.map(_.contains(i)).getOrElse(true) &&
    blackList.map(!_.contains(i)).getOrElse(true) &&
    // discard userItems in query as well
    (!queryList.contains(i))
  }

}
