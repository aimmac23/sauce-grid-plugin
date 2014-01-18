package com.saucelabs.grid;

import com.google.common.io.ByteStreams;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Contains common logic for the Sauce-specific servlets.  This class (and it's subclasses) are largely
 * copied from {@link org.openqa.grid.web.servlet.beta.ConsoleServlet}, so as to retain the updated look
 * and feel, while providing a slightly different way of presenting the data.
 *
 * @author Ross Rowe
 */
public abstract class AbstractSauceOnDemandServlet extends RegistryBasedServlet {

    private static final Logger log = Logger.getLogger(AbstractSauceOnDemandServlet.class.getName());

    private static String coreVersion;
    private static String coreRevision;

    public AbstractSauceOnDemandServlet(Registry registry) {
        super(registry);
        getVersion();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    /**
     * Copied from {@}
     * @param request
     * @param response
     * @throws IOException
     */
    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int refresh = -1;

        if (request.getParameter("refresh") != null) {
            try {
                refresh = Integer.parseInt(request.getParameter("refresh"));
            } catch (NumberFormatException e) {
                // ignore wrong param
            }
        }

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);

        StringBuilder builder = new StringBuilder();

        builder.append("<html>");
        builder.append("<head>");
        builder
                .append("<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js'></script>");

        builder.append("<script src='/grid/resources/org/openqa/grid/images/console-beta.js'></script>");

        builder
                .append("<link href='/grid/resources/org/openqa/grid/images/console-beta.css' rel='stylesheet' type='text/css' />");


        if (refresh != -1) {
            builder.append(String.format("<meta http-equiv='refresh' content='%d' />", refresh));
        }
        builder.append("<title>Grid overview</title>");

        builder.append("<style>");
        builder.append(".busy {");
        builder.append(" opacity : 0.4;");
        builder.append("filter: alpha(opacity=40);");
        builder.append("}");
        builder.append("</style>");
        builder.append("</head>");

        builder.append("<body>");

        builder.append("<div id='main_content'>");

        builder.append(getHeader());
        renderBody(request, builder);
        builder.append("</div>");

        renderFooter(request, builder);
        builder.append("</body>");
        builder.append("</html>");

        InputStream in = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
        try {
            ByteStreams.copy(in, response.getOutputStream());
        } finally {
            in.close();
            response.getOutputStream().close();
        }
    }

    protected abstract void renderFooter(HttpServletRequest request, StringBuilder builder);

    protected abstract void renderBody(HttpServletRequest request, StringBuilder builder);


    protected Object getHeader() {
        StringBuilder builder = new StringBuilder();
        builder.append("<div id='header'>");
        builder.append("<h1><a href='http://code.google.com/p/selenium/wiki/Grid2' >Selenium</a></h1>");
        builder.append("<h2>Hub console - (beta) ");
        builder.append(coreVersion).append(coreRevision);
        builder.append("</h2>");
        builder.append("<div>.</div>");
        builder.append("</div>");
        builder.append("");
        return builder.toString();
    }

    private void getVersion() {
        final Properties p = new Properties();

        InputStream stream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("VERSION.txt");
        if (stream == null) {
            log.severe("Couldn't determine version number");
            return;
        }
        try {
            p.load(stream);
        } catch (IOException e) {
            log.severe("Cannot load version from VERSION.txt" + e.getMessage());
        }
        coreVersion = p.getProperty("selenium.core.version");
        coreRevision = p.getProperty("selenium.core.revision");
        if (coreVersion == null) {
            log.severe("Cannot load selenium.core.version from VERSION.txt");
        }
    }


}
