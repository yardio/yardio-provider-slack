package io.yard.provider.slack

import io.yard.module.core.Api

class SlackPlugin(application: play.api.Application) extends play.api.Plugin {
  override def onStart = {
    Api.registerProvider(SlackProvider)
  }
}
