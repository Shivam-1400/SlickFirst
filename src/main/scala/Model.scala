import slick.lifted.ProvenShape

import java.time.LocalDate

case class Movie(id:Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
case class Actor(id:Long, name: String)
case class MovieActorMapping(id:Long, movieId: Long, actorId: Long)

object SlickTables{
  import slick.jdbc.PostgresProfile.api._

  //MOVIE
  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies")/*<-schema name*/, "Movie"){
    def id= column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name= column[String]("name")
    def releaseDate= column[LocalDate]("release_date")
    def lengthInMin= column[Int]("length_in_min")

    //mapping function to the case class
    override def * =(id, name, releaseDate, lengthInMin)<>(Movie.tupled, Movie.unapply)
  }
  //API entry point
  lazy val movieTable= TableQuery[MovieTable]

  //ACTORS
  class ActorTable(tag:Tag) extends Table[Actor](tag, Some("movies"), "Actor"){  // ___ into movies.Actor
    def id= column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name= column[String]("name")

    //mapping function to the case class
    override def * : ProvenShape[Actor] = (id, name)<>(Actor.tupled, Actor.unapply)
  }

  lazy val actorTable= TableQuery[ActorTable]

//  ACTOR -MOVIE mapping
  class MovieActorMappingTable(tag:Tag) extends Table[MovieActorMappingTable](tag, Some("movies"), "MovieActorMapping"){
    def id= column[Long]("movie_actor_id", O.PrimaryKey, O.AutoInc)
    def movieId= column[Long]("movie_id")
    def actorId=  column[Long]("actor_id")


    //mapping function to case class ProvenShape[MovieActorMapping]
//    override def *  = (id, movieId, actorId)<>(MovieActorMapping.tupled, MovieActorMapping.unapply)

  override def * =(id, movieId, actorId)<>(MovieActorMapping.tupled, MovieActorMapping.unapply)
}

  lazy val movieActorMappingTable= TableQuery[MovieActorMappingTable]


}
