package io.yard.provider.slack

import io.yard.api.slack.models._

object SlackMapper {
    object from {
      def message(m: io.yard.models.Message) = IncomingWebHook(m.text)
    }

    object to {
      def message(o: OutgoingWebHook) = io.yard.models.Message(o.content)

      def command(c: Command) = io.yard.models.Command(c.command, c.text.split(" "))
    }
}
