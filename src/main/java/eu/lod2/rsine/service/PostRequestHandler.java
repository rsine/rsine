package eu.lod2.rsine.service;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;

public abstract class PostRequestHandler implements HttpRequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
        throws HttpException, IOException
    {
        if (request.getRequestLine().getMethod().equalsIgnoreCase("POST") &&
            request instanceof BasicHttpEntityEnclosingRequest)
        {
            handlePost((BasicHttpEntityEnclosingRequest) request, response);
        }
    }

    protected abstract void handlePost(BasicHttpEntityEnclosingRequest request, HttpResponse response);

}
