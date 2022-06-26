package org.acme;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class TestApp {
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        Log.info("Hello World triggered");
        return "Hello World";
    }

    @GET
    @Path("/uglyError")
    @Produces(MediaType.TEXT_PLAIN)
    public String uglyError() {
        Log.error("Something ugly happened");
        return "Ugly error happened";
    }

    @GET
    @Path("/manyUglyErrors")
    @Produces(MediaType.TEXT_PLAIN)
    public String manyUglyErrors() {
        for (int i = 0; i < 10; i++) {
            Log.error("Something ugly happened");
        }

        return "Many ugly errors happened";
    }

    @Scheduled(every="10s")
    void iamAlive()  {
        uglyError();
    }
}