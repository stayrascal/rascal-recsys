package com.stayrascal.recom.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecomServer {
    private final Logger logger = LoggerFactory.getLogger(RecomServer.class);
    private Server webServer = null;

    public void start() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        webServer = new Server(9999);
        webServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "com.stayrascal.recom.service");

        try {
            logger.info("Web Server started .......");
            webServer.start();
            webServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webServer.destroy();
        }
    }

    public void stop() throws Exception {
        if (webServer != null) {
            webServer.stop();
        }
    }

    public static void main(String[] args) {
        RecomServer recomServer = new RecomServer();
        recomServer.start();
    }
}
