package io.dropwizard.jersey.setup;

import org.glassfish.jersey.servlet.ServletContainer;

public class JerseyContainerHolder {
    private ServletContainer container;

    public JerseyContainerHolder(ServletContainer container) {
        this.container = container;
    }

    public ServletContainer getContainer() {
        return container;
    }

    public void setContainer(ServletContainer container) {
        this.container = container;
    }
}
