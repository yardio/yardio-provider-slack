package io.yard.provider.slack

import play.api.libs.concurrent.Execution.Implicits._

import io.yard.common.models._
import io.yard.api.slack.SlackApi

object SlackProvider extends io.yard.common.models.Provider {
  def name = "slack"
  def controller = Some(SlackController)

  def send(message: Message, organization: Organization) = {
    SlackConfig.from(organization).map(_.map { config =>
      SlackApi.send(organization.name, config.incomingTokens(0), SlackMapper.from.message(message))
    })
  }
}
