package krs.user.service

import com.twitter.util.{Await}
import com.twitter.finagle.Thrift
import com.twitter.finagle.stats.Counter
import com.twitter.server.TwitterServer

import krs.user.api.ApiModule
import krs.user.domain.DomainModule

object UserServer
    extends TwitterServer
    with ServiceModule
    with ApiModule
    with DomainModule {

  val userImpl = UserServiceImpl(userApi)

  val userService = statsReceiver.counter("userService")

  def main(): Unit = {
    val conf = com.typesafe.config.ConfigFactory.load()
    val host = conf.getString("krs.user.host")

    val server = Thrift.server
      .withStatsReceiver(statsReceiver)
      .serveIface(host, userImpl)

    onExit { server.close() }
    Await.ready(server)
  }
}
