package com.common;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextPath implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext application = sce.getServletContext();
        String contextPath = application.getContextPath();

        application.setAttribute("cp", contextPath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
