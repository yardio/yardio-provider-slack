package io.yard.provider.slack

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data._
import play.api.data.Forms._

import io.yard.api.slack.models._
import io.yard.module.core.Api
import io.yard.common.utils._
import io.yard.common.models.ModuleController

object SlackController extends ModuleController with Answer with Log {

  lazy val logger = initLogger("yardio.provider.slack.SlackController")

  def hasRoute(rh: RequestHeader) = true

  def applyRoute[RH <: RequestHeader, H >: Handler](rh: RH, default: RH ⇒ H) =
    (rh.method, rh.path.drop(path.length)) match {
      case ("GET", "/debug")      ⇒ debug(rh.getQueryString("org"))
      case ("POST", "/outgoings") ⇒ handleOutgoing
      case ("POST", "/commands")  ⇒ handleCommand
      case _                      ⇒ default(rh)
    }

  // OUTGOING WEBHOOK
  val hookForm = Form(
    mapping(
      "token" -> nonEmptyText,
      "team_id" -> nonEmptyText,
      "team_domain" -> nonEmptyText,
      "channel_id" -> nonEmptyText,
      "channel_name" -> nonEmptyText,
      "timestamp" -> nonEmptyText,
      "user_id" -> nonEmptyText,
      "user_name" -> nonEmptyText,
      "text" -> optional(text),
      "service_id" -> optional(text),
      "trigger_word" -> optional(text)
    )(OutgoingWebHook.apply)(OutgoingWebHook.unapply)
  )

  def handleOutgoing = Action { implicit request ⇒
    debugStart("core.controllers.core.handleOutgoing")
    hookForm.bindFromRequest.fold(
      errors ⇒ {
        debug("ERROR parsing: " + request.body)
        debugEnd
        BadRequest(Json.stringify(Json.obj(
          "text" -> ("Server-side error: " + errors.toString)
        )))
      },
      hook ⇒ {
        debug(hook.toString)
        debugEnd
        if (!hook.acceptable) { Ok }
        else {
          Api !! SlackMapper.to.message(hook)
          SlackMapper.to.command(hook).map {
            c => Api !! c
          }
          Ok
        }
      }
    )
  }

  // COMMANDS
  val token = "FGbEeBew4N8NiCwcBcK9Qp3e"

  val commandForm = Form(
    mapping(
      "token" -> nonEmptyText,
      "team_id" -> nonEmptyText,
      "channel_id" -> nonEmptyText,
      "channel_name" -> nonEmptyText,
      "user_id" -> nonEmptyText,
      "user_name" -> nonEmptyText,
      "command" -> nonEmptyText,
      "text" -> nonEmptyText
    )(Command.apply)(Command.unapply)
  )

  def broadcastCommand(command: Command): Future[SimpleResult] = command.command match {
    case "/do" ⇒ {
      command.text.split(" ").toList.headOption match {
        case None ⇒ asyncError("/do command must have at least one argument.")
        case Some(newCommand) ⇒ {
          val newText = command.text.split(" ").toList.drop(1).mkString(" ")
          broadcastCommand(command.copy(command = "/" + newCommand, text = newText))
        }
      }
    }
    case _ ⇒ {
      debug(s"Broadcasting command ${command}...")
      Api !! SlackMapper.to.command(command)
      asyncOk
    }
  }

  def handleCommand = Action.async { implicit request ⇒
    commandForm.bindFromRequest.fold(
      errors ⇒ asyncError("Wrong command submission from Slack."),
      command ⇒ {
        if (command.token != token) asyncError("wrong token.")
        else broadcastCommand(command)
      }
    )
  }

  def debug(org: Option[String]) = Action.async {
    Api.connector.read[SlackConfig](SlackProvider, org.map(Api.organizations.from(_))).map { confOpt =>
      val attrs = confOpt.map { config =>
        Map(
          "Tokens out size" -> config.outgoingTokens.size.toString,
          "Tokens in size" -> config.incomingTokens.size.toString
        )
      }
      .getOrElse(Map.empty[String, String])

      Ok(io.yard.html.debug(attrs))
    }
  }
}
