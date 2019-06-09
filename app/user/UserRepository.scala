package user

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}

@Singleton
class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def username = column[String]("username")

    def isSubscribed = column[Boolean]("is_subscribed", O.Default(false))

    def isBlacklisted = column[Boolean]("is_blacklisted", O.Default(false))

    def * = (id.?, username, isSubscribed, isBlacklisted) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]

  def all: Future[Seq[User]] = db.run {
    users.result
  }

  def delete(id: Int): Future[Int] = db.run {
    users.filter(_.id === id).delete
  }

  def create(user: User): Future[User] = db.run {
    (users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id)))) += user
  }

  def show(id: Int): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.headOption
  }
}