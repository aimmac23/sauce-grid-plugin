package com.saucelabs.grid;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

public class Helper {

  private static final String encoding = "UTF-8";

  public static String extractResponse(HttpResponse repsonse) throws IOException {
    InputStream is = repsonse.getEntity().getContent();
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, encoding);
    return writer.toString();
  }
}
