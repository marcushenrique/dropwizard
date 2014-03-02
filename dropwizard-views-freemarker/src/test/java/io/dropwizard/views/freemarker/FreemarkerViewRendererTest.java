package io.dropwizard.views.freemarker;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;

import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderer;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;

public class FreemarkerViewRendererTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_HTML)
    public static class ExampleResource {
        @GET
        @Path("/absolute")
        public AbsoluteView showAbsolute() {
            return new AbsoluteView("yay");
        }

        @GET
        @Path("/relative")
        public RelativeView showRelative() {
            return new RelativeView();
        }

        @GET
        @Path("/bad")
        public BadView showBad() {
            return new BadView();
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();
        final ViewRenderer renderer = new FreemarkerViewRenderer();
        config.register(new ViewMessageBodyWriter(new MetricRegistry(), ImmutableList.of(renderer)));
        config.register(new ExampleResource());
        return config;
    }

    @Test
    public void rendersViewsWithAbsoluteTemplatePaths() throws Exception {
        final String response = target("/test/absolute")
                .request().get(String.class);
        assertThat(response)
                .isEqualToIgnoringCase("Woop woop. yay\n");
    }

    @Test
    public void rendersViewsWithRelativeTemplatePaths() throws Exception {
        final String response = target("/test/relative")
                .request().get(String.class);
        assertThat(response)
                .isEqualToIgnoringCase("Ok.\n");
    }

    @Test
    public void returnsA500ForViewsWithBadTemplatePaths() throws Exception {
        try {
            target("/test/bad")
                    .request().get(String.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);
            
            ByteArrayInputStream entity = (ByteArrayInputStream) e.getResponse().getEntity();
            InputStreamReader reader = new InputStreamReader(entity);
            CharBuffer chars = CharBuffer.allocate(1024);
            String responseStr = "";
            while (reader.read(chars) != -1)
            {
                chars.limit(chars.position());
                chars.rewind();
                responseStr += chars.toString();
                chars.clear();
            }

            assertThat(responseStr)
                .isEqualTo("<html><head><title>Missing Template</title></head><body><h1>Missing Template</h1><p>Template \"/woo-oo-ahh.txt.ftl\" not found.</p></body></html>");                    
        }
    }
}
