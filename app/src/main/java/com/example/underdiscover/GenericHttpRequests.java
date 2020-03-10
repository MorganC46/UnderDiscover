package com.example.underdiscover;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

public class GenericHttpRequests {

    public static class HttpRequestGet extends AsyncTask<Void, Void, String> {

        String apiUrl;
        String accessToken;

        public HttpRequestGet(String apiUrl, String accessToken) {
            this.apiUrl = apiUrl;
            this.accessToken = accessToken;
        }

        // DEBUG FUNCTION

//        protected static boolean isUrlValid(String url) {
//            try {
//                URL obj = new URL(url);
//                obj.toURI();
//                return true;
//            } catch (MalformedURLException e) {
//                return false;
//            } catch (URISyntaxException e) {
//                return false;
//            }
//        }

        //Connection method
        protected String doInBackground(Void... params) {

            String result = "";
            HttpURLConnection urlConn;

            try {
                URL url = new URL(apiUrl);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = streamIntoString(urlConn.getInputStream());
                    return result;
                }
            }

            catch(MalformedURLException eURL) {
                eURL.printStackTrace();
                System.exit(1);
            }

            catch(IOException eIO) {
                eIO.printStackTrace();
                System.exit(2);
            }

            return null;
        }
    }

    public static class HttpRequestPost extends AsyncTask<Void, Void, String> {

        String apiUrl;
        String accessToken;
        String createQueryParams;
        JSONArray appendQueryParams;

        public HttpRequestPost(String apiUrl, String accessToken, String createQueryParams,
                               JSONArray appendQueryParams) {
            this.apiUrl = apiUrl;
            this.accessToken = accessToken;
            this.createQueryParams = createQueryParams;
            this.appendQueryParams = appendQueryParams;
        }

        //Connection method
        protected String doInBackground(Void... params) {

            String result = "";
            HttpURLConnection urlConn;
            OutputStream out;

            try {
                URL url = new URL(apiUrl);
                urlConn = (HttpURLConnection) url.openConnection();

                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Authorization", "Bearer " + accessToken);
                urlConn.setDoOutput(true);

                out = urlConn.getOutputStream();

                if (appendQueryParams == null) {
                    out.write(createQueryParams.getBytes());
                    if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK || urlConn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                        result = urlConn.getHeaderField("Location");
                        out.close();
                        return result;
                    }
                }
                else if (createQueryParams == null) {
                    out.write(appendQueryParams.toString().getBytes());
                    if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK || urlConn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                        result = streamIntoString(urlConn.getInputStream());
                        out.close();
                        return result;
                    }
                }
            }

            catch(MalformedURLException eURL) {
                eURL.printStackTrace();
                System.exit(1);
            }

            catch(IOException eIO) {
                eIO.printStackTrace();
                System.exit(2);
            }

            return null;

        }
    }

//    //PIECE OF CODE WRITTEN BY FAHIM, STACK OVERFLOW
//    //https://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post-using-namevaluepair/29561084#29561084
//
//    private static String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
//        StringBuilder result = new StringBuilder();
//        boolean first = true;
//        for(Map.Entry<String, String> entry : params.entrySet()){
//            if (first)
//                first = false;
//            else
//                result.append("&");
//
//            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
//            result.append("=");
//            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
//        }
//
//        return result.toString();
//    }

    public static class ImageRequest extends AsyncTask<Void, Void, Drawable> {

        String imageUrl;

        protected ImageRequest(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        protected Drawable doInBackground(Void... params) {
            try {
                InputStream imageInput = (InputStream) new URL(imageUrl).getContent();
                Drawable imageDrawable = Drawable.createFromStream(imageInput, "artwork");
                return imageDrawable;
            } catch (MalformedURLException eURL) {
                eURL.printStackTrace();
                System.exit(1);
            } catch (IOException eIO) {
                eIO.printStackTrace();
                System.exit(2);
            }
            return null;
        }
    }


    protected static String streamIntoString(InputStream stream) {
        //Method to process and correctly separate input streams
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String data;
            String result = "";

            while ((data = reader.readLine()) != null) {
                result += data;
            }
            if (null != stream) {
                stream.close();
            }
            return result;
        }
        catch (IOException eIO) {
            eIO.printStackTrace();
            System.exit(3);
        }
        return null;
    }
}
