package io.yard.provider.slack

import io.yard.common.models.{Command => CommonCommand}
import io.yard.api.slack.models._

object SlackMapper {
    object from {
      def message(m: io.yard.common.models.Message) = IncomingWebHook(m.text)
    }

    object to {
      def message(o: OutgoingWebHook) = io.yard.common.models.Message(o.content)

      def command(c: Command) = CommonCommand(c.command, c.text.split(" "))

      def command(o: OutgoingWebHook): Option[CommonCommand] = o.trigger_word.flatMap { tword =>
        if (tword.startsWith("!")) { o.text map { t =>
          val words = t.drop(1).trim.split(" ") // Here, we are dropping the starting bang
          CommonCommand(words.head, words.drop(1))
        }}
        else {
          None
        }
      }
    }
}
