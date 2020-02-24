package com.example.underdiscover;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class GenericHttpRequests {

    public static class HttpRequest extends AsyncTask<Void, Void, String> {

        String apiUrl;
        String accessToken;

        public HttpRequest(String apiUrl, String accessToken) {
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
