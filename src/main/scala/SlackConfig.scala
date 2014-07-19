package io.yard.provider.slack

import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.yard.common.models.{ProviderConfiguration, Organization}
import io.yard.module.core.Api

case class SlackConfig(
  outgoingTokens: Seq[String] = Seq.empty,
  incomingTokens: Seq[String] = Seq.empty
) extends ProviderConfiguration

object SlackConfig {
  implicit val slackConfigFormat = Json.format[SlackConfig]

  def default = Api.connector.read[SlackConfig](SlackProvider, None)
  def from(organization: Organization) = Api.connector.read[SlackConfig](SlackProvider, Some(organization))
}
