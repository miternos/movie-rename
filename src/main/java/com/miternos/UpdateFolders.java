package com.miternos;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eilkakk on 19.09.2015.
 */
public class UpdateFolders {

    public static void main (String[] args){

        System.out.println("Started");


        File file = new File("folder");
        String[] directories = file.list(new FilenameFilter() {
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });


        for (int i = 0; i < directories.length; i++) {
            String directory = directories[i];
            String newDirectory = directory + " - "+getImdbUrlAndRateFromOmdb(directory);
            File movieFolder = new File("folder/"+directory);
            if (movieFolder.exists() && movieFolder.canWrite()){
                File newMovieFolder = new File("folder/"+newDirectory);
                boolean result = file.renameTo(newMovieFolder);
                if (!result){
                    System.out.printf("Could not rename: "+directory+" to: "+newDirectory);
                }
            }
        }

    }

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    class GoogleResults{

        private ResponseData responseData;
        public ResponseData getResponseData() { return responseData; }
        public void setResponseData(ResponseData responseData) { this.responseData = responseData; }
        public String toString() { return "ResponseData[" + responseData + "]"; }

        class ResponseData {
            private List<Result> results;
            public List<Result> getResults() { return results; }
            public void setResults(List<Result> results) { this.results = results; }
            public String toString() { return "Results[" + results + "]"; }
        }

        class Result {
            private String url;
            private String title;
            public String getUrl() { return url; }
            public String getTitle() { return title; }
            public void setUrl(String url) { this.url = url; }
            public void setTitle(String title) { this.title = title; }
            public String toString() { return "Result[url:" + url +",title:" + title + "]"; }
        }
    }

    class OmdbResult{
        private String Title ;
        private String imdbRating;
        private String Response ;


        public String getTitle() {
            return Title;
        }

        public void setTitle(String title) {
            Title = title;
        }

        public String getImdbRating() {
            return imdbRating;
        }

        public void setImdbRating(String imdbRating) {
            this.imdbRating = imdbRating;
        }

        public String getResponse() {
            return Response;
        }

        public void setResponse(String response) {
            Response = response;
        }
    }

    public static String getImdbUrlAndRate(String movieString){

        String unDottedMovieString = movieString.replace("."," ");
        String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
        String query = "\"site:imdb.com\" "+unDottedMovieString;
        String charset = "UTF-8";

        String rate = "N/A" ;
        String imdbUrl = "http://www.imdb.com/title/???????/";

        try {

            URL url = new URL(address + URLEncoder.encode(query, charset));
            Reader reader = new InputStreamReader(url.openStream(), charset);
            GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);


            int total =  results.getResponseData() == null ? 0 : results.getResponseData().getResults().size();



            if ( total > 0 ){
                imdbUrl = results.getResponseData().getResults().get(0).getUrl();
                if ( imdbUrl.contains("imdb") ){
                    String imdbResult = getHTML(imdbUrl);
                    Pattern responseCodePattern = Pattern.compile("Users rated this (\\d.\\d)");
                    Matcher responseCodeMatcher = responseCodePattern.matcher(imdbResult);

                    if (responseCodeMatcher.find()) {
                        if ( responseCodeMatcher.group(1) != null ){
                            rate = responseCodeMatcher.group(1);
                        }
                    }
                } else {
                    System.out.println("Failed to find the rate of " + query);
                }
            }


        } catch (Exception e){
            System.out.println("Exception occured Failed to find the rate of " + query);
            e.printStackTrace();
        }

        return rate+" - "+imdbUrl;
    }

    public static String getImdbUrlAndRateFromOmdb(String movieString){

        String unDottedMovieString = movieString.replace(" ",".");
        unDottedMovieString = unDottedMovieString.replace(".","+");
        unDottedMovieString = unDottedMovieString.replaceAll("[^a-zA-Z0-9]+","+");

        String[] words = unDottedMovieString.split("\\+");

        String rate = "N/A" ;
        String title = "NO_TITLE";


        for (int i = words.length; i > 0; i--) {
            String tmpMovieString = "";
            for ( int j = 0 ; j < i ; j++){
                if ( j!=0){
                    tmpMovieString = tmpMovieString + "+" ;
                }

                tmpMovieString = tmpMovieString + words[j];

            }

            String address = "http://www.omdbapi.com/?t="+tmpMovieString+"&r=json";
            String charset = "UTF-8";


            try {

                URL url = new URL(address + URLEncoder.encode(address, charset));
                Reader reader = new InputStreamReader(url.openStream(), charset);
                OmdbResult result = new Gson().fromJson(reader, OmdbResult.class);


                if ( result.getResponse().equals("True") ){
                    title = result.getTitle();
                    rate = result.getImdbRating();
                    break;
                }


            } catch (Exception e){
                System.out.println("Exception occured Failed to find the rate of " + address);
                e.printStackTrace();
            }

        }


        return title+" - "+rate;
    }

}
