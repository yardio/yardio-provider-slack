package io.yard.provider.slack

import play.api.libs.json._
import play.api.libs.functional.syntax._

import io.yard.models.ProviderConfiguration

case class SlackConfig(
  outgoingTokens: Seq[String] = Seq.empty,
  incomingTokens: Seq[String] = Seq.empty
) extends ProviderConfiguration

object SlackConfig {
  implicit val slackConfigFormat = Json.format[SlackConfig]
}
