package io.yard.provider.slack

import play.api.libs.concurrent.Execution.Implicits._

import io.yard.models._
import io.yard.module.core.Api
import io.yard.api.slack.SlackApi
import io.yard.provider.slack.SlackConfig

object SlackProvider extends io.yard.models.Provider {
  def name = "slack"

  def config(organization: Organization) = Api.connector.read[SlackConfig](organization, SlackProvider)

  def send(message: Message, organization: Organization) = {
    config(organization).map(_.map { config =>
      SlackApi.send(organization.name, config.incomingTokens(0), SlackMapper.from.message(message))
    })

    /*val teamName: String = organization.name.toLowerCase
    val incomingToken: String = token orElse organization.tokens.get("incoming").flatMap(_.headOption) getOrElse ""
    val url = s"https://${teamName}.slack.com/services/hooks/incoming-webhook?token=${incomingToken}"
    val jsonWebhook = Json.toJson(hook)

    debugStart(s"IncomingWebhooks.send at ${url}")
    debug(Json.prettyPrint(jsonWebhook))
    debugEnd

    WS.url(url).post(Json.stringify(jsonWebhook))*/
  }
}
