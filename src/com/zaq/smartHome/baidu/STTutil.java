package com.zaq.smartHome.baidu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

/**
 * 语音转文字
 * @author zaqzaq
 * 2015年12月5日
 *
 */
public class STTutil {

    private static final String serverURL = "http://vop.baidu.com/server_api";
    private static String token = "";
    private static final String testFileName = "test.pcm";
    //put your own params here
    private static final String apiKey = "fCGGgMX5oWLr7TnQUhENyw8Z";
    private static final String secretKey = "b38OysECB9Pe5kR07Q5VRY8L62A0QXwI";
    private static final String cuid = "B8-88-E3-F1-E4-46";

    public static void main(String[] args) throws Exception {
        getToken(); 
//        method1();
//        method2();
        method3();
    }

    private static void getToken() throws Exception {
        String getTokenURL = "http://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" + 
            "&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
        token = new JSONObject(printResponse(conn)).getString("access_token");
    }
    
    private static void method3() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://tsn.baidu.com/text2audio").openConnection();

        StringBuilder params=new StringBuilder("tex=xxxx");
        params.append("&cuid=").append(cuid);
        params.append("&lan=zh");
        params.append("&ctp=1");
        params.append("&tok=").append(token);
        // add request header
        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        System.out.println(params.toString());
//        wr.writeBytes(URLEncoder.encode(params.toString(), "utf-8"));
        wr.writeBytes(params.toString());
        wr.flush();
        wr.close();

        printResponse(conn);
    }

    private static void method1() throws Exception {
        File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();

        // construct params
        JSONObject params = new JSONObject();
        params.put("format", "pcm");
        params.put("rate", 8000);
        params.put("channel", "1");
        params.put("token", token);
        params.put("cuid", cuid);
        params.put("len", pcmFile.length());
        params.put("speech", DatatypeConverter.printBase64Binary(loadFile(pcmFile)));

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params.toString());
        wr.flush();
        wr.close();

        printResponse(conn);
    }

    private static void method2() throws Exception {
        File pcmFile = new File(testFileName);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL
                + "?cuid=" + cuid + "&token=" + token).openConnection();

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "audio/pcm; rate=8000");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(loadFile(pcmFile));
        wr.flush();
        wr.close();

        printResponse(conn);
    }

    private static String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            // request error
            return "";
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        System.out.println(new JSONObject(response.toString()).toString(4));
        return response.toString();
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }
}