import slick.jdbc.GetResult

import java.time.LocalDate
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import java.util.concurrent.Executors
import scala.util.{Failure, Success}

object PrivateExecutionContext{
  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)
}

object main {
  import PrivateExecutionContext._

  //Movies
  val shawshank= Movie(1L, "The Shawshank Redemption", LocalDate.of(2024, 1, 27), 154)
  val theMatrix= Movie(2L, "The Matrxi", LocalDate.of(2001, 1,1), 123)
  val phantom= Movie(10L, "Phantom", LocalDate.of(2012, 1, 17), 102)

  //Actors
  val tom= Actor(1L, "Tom")
  val jerry= Actor(1L, "Jerry")
  val mickey= Actor(3L, "Mickey")


  def demoInsertMovie(): Unit={
    val queryDescription= SlickTables.movieTable += shawshank
    val futureId: Future[Int]= Connection.db.run(queryDescription)
    futureId.onComplete{
      case Success(newMovieId)=>println(s"Query was successful, new id is $newMovieId")
      case Failure(exception)=> println(s"Query Failed, reason :- $exception")
    }
    Thread.sleep(10000)
  }


  def demoInsertActors(): Unit = {
    val queryDescription = SlickTables.actorTable ++= Seq(tom, jerry)
    val futureId = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Success(_) => println(s"Query was successful, new id is ")
      case Failure(exception) => println(s"Query Failed, reason :- $exception")
    }
  }
  def demoReadAllMovies():Unit={
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result)  // select * from tableX
    resultFuture.onComplete{
      case Success(movies)=> println(s"Fetched: ${movies.mkString(",")}")
      case Failure(ex)=> println(s"Fetching failed: $ex")
    }
    Thread.sleep(10000)
  }

  def demoReadSomeMovies(): Unit = {
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Mat%")).result) // select * from tableX
    //selecte * from tableX where name like "%MAT%"
    resultFuture.onComplete {
      case Success(movies) => println(s"Fetched: ${movies.mkString(",")}")
      case Failure(ex) => println(s"Fetching failed: $ex")
    }
    Thread.sleep(10000)
  }

  def demoUpdate():Unit={
    val queryDescriptor= SlickTables.movieTable.filter(_.id === 1L).update(shawshank.copy(lengthInMin = 10))

    val futureId: Future[Int] = Connection.db.run(queryDescriptor)
    futureId.onComplete {
      case Success(newMovieId) => println(s"Query was successful, new id is $newMovieId")
      case Failure(exception) => println(s"Query Failed, reason :- $exception")
    }
    Thread.sleep(10000)
  }

  def demoDelete():Unit={
    Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Mat%")).delete)
    Thread.sleep(5000)
  }

  def readMoviesByPlainQuery():Future[Vector[Movie]]={
    //[id, name, localDate, lengthInMin]
    implicit val getResultMovie: GetResult[Movie]=
      GetResult(positionedResult=> Movie(
        positionedResult.<<,
        positionedResult.<<,
        LocalDate.parse(positionedResult.nextString()),
        positionedResult.<<
      ))
    val query= sql"""SELECT * FROM movies."Movie";""".as[Movie]
    Connection.db.run(query)
  }

  def multipleQueriesSingleTransaction():Unit={
    val insertMovie= SlickTables.movieTable += phantom
    val insertActor= SlickTables.actorTable += mickey

    val finalQuery= DBIO.seq(insertMovie, insertActor)
    val futureRes= Connection.db.run(finalQuery)
    futureRes.onComplete {
      case Success(_)=> println("Query Successful")
      case Failure(ex)=> println("Query Failed")
    }
  }

  def findAllActorsByMovie(movieId: Long): Future[Seq[Actor]]={
    val joinQuery= SlickTables.movieActorMappingTable
      .filter(_.movieId=== movieId)
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id)  //select * from movieActorMappingTable m join actorTable a on m.actorId== a.id
      .map(_._2)

    Connection.db.run(joinQuery.result)
//    val futureJoin= Connection.db.run(joinQuery.result)
//    futureJoin.onComplete{
//      case Success(_)=>println("Query executed successful")
//      case Failure(ex)=>println(s"Query failed. Error= $ex")
//    }

  }


  def main(args: Array[String]):Unit={
    println("Hello world")
//    demoInsertMovie()
//    demoReadAllMovies()
//    demoReadSomeMovies()
//    demoUpdate()
//    demoDelete()

//    readMoviesByPlainQuery().onComplete{
//      case Success(movies)=> println(s"Query successful, movies: ${movies}")
//      case Failure(ex)=> println(s"Query Failed: $ex")
//    }

//    demoInsertActors()
//    multipleQueriesSingleTransaction()

    findAllActorsByMovie(2L).onComplete{
      case Success(actor) => println(s"Query successful,Actors from: ${actor}")
      case Failure(ex)=> println(s"Query Failed: $ex")
    }

    Thread.sleep(5000)
    PrivateExecutionContext.executor.shutdown()
  }

}
