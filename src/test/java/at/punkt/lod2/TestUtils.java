package at.punkt.lod2;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class TestUtils {

    public static int getRandomPort() {
        return (int) Math.round(Math.random() * 100) + 8000;
    }

    public static int doPost(int port, Properties properties) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:" +port);
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        httpPost.setEntity(new StringEntity(sw.toString()));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        return response.getStatusLine().getStatusCode();
    }

}
