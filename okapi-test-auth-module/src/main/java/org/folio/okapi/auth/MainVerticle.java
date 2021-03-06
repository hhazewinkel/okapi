package org.folio.okapi.auth;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.folio.okapi.common.OkapiLogger;

/**
 * The auth module provides two services: login and filter. URI "/authn/login"
 * takes username, password, and other parameters, and returns a token. URI
 * "/filter" takes the token, and verifies that everything is all right. This is
 * a very trivial dummy module, that provides simple hard-coded authentication
 * for any user who can append '-password' to his username to make a fake
 * password.
 *
 * This module can also be used for testing other filter phases, like 'pre' and
 * 'post'.
 *
 * @author heikki
 *
 * ...
 */
@java.lang.SuppressWarnings({"squid:S1192"})
public class MainVerticle extends AbstractVerticle {

  private final Logger logger = OkapiLogger.get();

  @Override
  public void start(Future<Void> fut) throws IOException {
    Router router = Router.router(vertx);
    Auth auth = new Auth();

    final int port = Integer.parseInt(System.getProperty("port", "9020"));

    logger.info("Starting auth " + ManagementFactory.getRuntimeMXBean().getName() + " on port " + port);

    router.post("/authn/login").handler(BodyHandler.create());
    router.post("/authn/login").handler(auth::login);
    router.route("/authn/login").handler(auth::accept);
    router.route("/*").handler(auth::filter);

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(
                    port,
                    result -> {
                      if (result.succeeded()) {
                        fut.complete();
                      } else {
                        logger.fatal("okapi-test-auth-module failed: " + result.cause());
                        fut.fail(result.cause());
                      }
                    }
            );
  }

}
