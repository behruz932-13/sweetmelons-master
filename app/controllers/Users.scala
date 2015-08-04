package controllers

import models._
import play.api.Logger
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._
import play.api.libs.json.Json._
import play.api.mvc._

import scala.slick.lifted.TableQuery

class Users extends Controller with TestingAuth {

  import models._
  import models.JsonFormats._
  val users = TableQuery[UsersTable]

  def showAddForm = DBAction { implicit rs =>
    Ok(views.html.addMembers())
  }

  def findUsers = DBAction { implicit rs =>
    Logger.info(s"Hiiiii im here =${users.list}")
    Ok(toJson(users.list))
  }

  def signUp = DBAction(parse.json) { implicit rs =>
    rs.request.body.validate[Account].map { user =>
      val userId = (users returning users.map(_.id)) += user
      val addedUser = users.findBy(_.id).applied(userId.toInt).first
      Ok(toJson(addedUser))
    }.recoverTotal { errors =>
      BadRequest(errors.toString)
    }
  }

  def deleteUser(id: Int) = AuthAction(AuthorityKey -> hasRole(UserRoleEnum.ADMIN)) { implicit rs =>
    DBAction { implicit req =>
      findById(id) match {
        case Some(entity) => {
          users.filter(_.id === id).delete;
          Ok("User deleted")
        }
        case None => Ok("")
      }
    }
  }

  def findById(id: Int)(implicit session: play.api.db.slick.Config.driver.simple.Session): Option[Account] = {
    val byId = users.findBy(_.id)
    byId(id).list.headOption
  }

  def updateUser(id: Int) = DBAction(parse.json) { implicit rs =>
    rs.request.body.validate[Account].map { user =>
      users.filter(_.id === id).update(user)
      Ok("User successfully updated")
    }.recoverTotal { errors =>
      BadRequest(errors.toString)
    }
  }

}