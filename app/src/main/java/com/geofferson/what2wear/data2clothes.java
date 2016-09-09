package com.geofferson.what2wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Solid Choice on 18/11/2014.
 *
 * float f = Float.parseFloat("25");
 String s = Float.toString(25.0f);
 *
 */
public class data2clothes {

    private static final String TAG = data2clothes.class.getSimpleName();
    protected String units = "metric"; //pull this value
    private float tempDay; //choose which temp to use based on time, morning and night also available
    private float tempEve;
    private float windSpeed;
    private float windChill;
    private float humidity;
    private float humidex;
    private float feelsLike;
    private String condition;
    private String clothes;
    private int weatherId;
    private String unitSymbol;
    private String city;
    private String country;
    private String[] image;
    private long sunrise;
    private long sunset;

    //2 button. one for right now, one for later in the day

    private long timeNow = System.currentTimeMillis()/1000;
    //Log.d(TAG, String.valueOf(timeNow));

    //text colours
    private String grey = "#999999";
    private String black = "#222222";
    private String darkBlack = "#000000";
    private String white = "#ffffff";

    private String greyId = "grey";
    private String blackId = "black";
    private String darkBlackId = "darkBlack";
    private String whiteId = "white";

    //images and image info. {"nameOfImageFile","imageTitle","imageAuthor","imageURL"}
    String[] sunInfo = new String[]{"sun","The Sun","Arun Kulshreshtha","commons.wikimedia.org/wiki/File:The_Sun.jpg",black,blackId};
    String[] heatInfo = new String[]{"sun","The Sun","Arun Kulshreshtha","commons.wikimedia.org/wiki/File:The_Sun.jpg",black,blackId};
    String[] rainInfo = new String[]{"rain2","Rain in Budalla, Sri Lanka","Bleuchoi from Sussex, UK","commons.wikimedia.org/wiki/File:Rain_in_Budalla,_Sri_Lanka.jpg",white,whiteId};
    String[] stormInfo = new String[]{"storm","lightning-wallpapers, Sri Lanka","أسامة الطيب","http://commons.wikimedia.org/wiki/File:-lightning-wallpapers.jpg",grey,greyId};
    String[] snowInfo = new String[]{"snow","Snow in Leith","Ann","",darkBlack,darkBlackId};
    String[] overcastInfo = new String[]{"overcast","A little overcast over Aberdeen harbour","Douglas Cumming","commons.wikimedia.org/wiki/File:A_little_overcast_over_Aberdeen_harbour_-_geograph.org.uk_-_1468944.jpg",white,whiteId};//
    String[] cloudDayInfo = new String[]{"cloudyday","Clouds","","commons.wikimedia.org/wiki/File:Clouds.JPG",black,blackId};
    String[] cloudNightInfo = new String[]{"cloudynight3","CUMULUS a la pleine lune YLEDUC","Yvan leduc","commons.wikimedia.org/wiki/File:CUMULUS_a_la_pleine_lune_YLEDUC.jpg",darkBlack,darkBlackId};
    String[] clearNightInfo = new String[]{"clearnight","Forest-night-sky-spruce-trees-stars - West Virginia"," http://www.ForestWander.com","commons.wikimedia.org/wiki/File:Forest-night-sky-spruce-trees-stars_-_West_Virginia_-_ForestWander.jpg",white,whiteId};
    String[] coldInfo = new String[]{"cold","Icicles Partnachklamm","Richard Bartz","commons.wikimedia.org/wiki/File:Icicles_Partnachklamm_rb.jpg",darkBlack,darkBlackId};
    String[] defaultInfo = new String[] {"sun2","Muir Woods","","wikipedia.org/wiki/File:Trees_and_sunshine.JPG",black,blackId};

    String[][] allInfo = new String[][]{sunInfo,heatInfo,rainInfo,stormInfo,snowInfo,overcastInfo,cloudDayInfo,cloudNightInfo,clearNightInfo,coldInfo,defaultInfo};

    protected String[] initializer (Context context,String data) {

        getUnits(context);
        getSpecs(data);
        dataAnalyseBranch();
        getClothes();
        feelsLike = c2f(feelsLike);
        getImage();
        String[] toPass = new String[]{ clothes, String.format("%.0f", feelsLike), unitSymbol, condition, city, country, image[0],image[1],image[2],image[3],image[4],image[5]};//Float.toString(feelsLike)

        //Log.d(TAG, clothes);
        //Log.d(TAG, Float.toString(feelsLike));
        //System.gc();
        return toPass;

    }

    protected void getSpecs(String data){
        //Log.v(TAG, data);

        try {
            JSONObject dataObj = new JSONObject(data);

            windSpeed = Float.parseFloat(dataObj.getJSONObject("wind").getString("speed"));
            humidity = Float.parseFloat(dataObj.getJSONObject("main").getString("humidity"));
            condition = dataObj.getJSONArray("weather").getJSONObject(0).getString("description");//can get string "main" and "description" if you wanna go more in depth with it
            weatherId = Integer.parseInt(dataObj.getJSONArray("weather").getJSONObject(0).getString("id"));
            tempDay = Float.parseFloat(dataObj.getJSONObject("main").getString("temp"));
            country = dataObj.getJSONObject("sys").getString("country");
            city = dataObj.getString("name");
            sunrise = Long.valueOf(dataObj.getJSONObject("sys").getString("sunrise")).longValue();
            sunset = Long.valueOf(dataObj.getJSONObject("sys").getString("sunset")).longValue();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    protected void dataAnalyseBranch () {

            //insert time branch here
            if (tempDay < 10){
                dataAnalyses(tempDay, "windChill");
            }else if(tempDay > 25){

                dataAnalyses(tempDay, "humidex");
            }
            else{
                //0 isn't the correct number, there will be a range where neither should be applied, when temp is in that range this branch will be chosen
                feelsLike = tempDay;
            }

    }

    protected void dataAnalyses(double workingTemp, String theFeels){
        if (theFeels=="windChill"){

            windChill = (float) (13.12 + 0.6215 * workingTemp - 11.37 * Math.pow(windSpeed, 0.16) + 0.3965 * workingTemp * Math.pow(windSpeed, 0.16));

            //Log.d(TAG, Float.toString(windChill)); //Float.toString(windChill)
            feelsLike = windChill;

        }
        else if(theFeels == "humidex"){

            humidex = (float) (workingTemp+0.5555*(6.11*Math.pow(Math.E,(5417.750*((1/273.16)-(1/((humidity/100*workingTemp)+273.15)))))-10));

            //Log.d(TAG, Float.toString(humidex));
            feelsLike = humidex;

        }

    }

    protected String getClothes(){

        int[] umbrellaArray = {200,201,231,232,301,302,310,311,313,321,500,501,520,521,511,615,616};
        int[] rainCoat = {202,312,314,502,503,504,522,531};
        //int[] waterWinter = {511,615,616};

        if (feelsLike > 20){
            clothes = "T shirt & Shorts";//"It's a nice day; t-shirt and shorts
        }
        else if( feelsLike <= 20 && feelsLike > 10 ){
            clothes = "Long sleeves";
        }
        else if(feelsLike <= 10 && feelsLike > 3){
            clothes = "Light jacket";
        }
        else if(feelsLike <= 3 && feelsLike > -1){
            clothes = "Winter jacket";
        }
        else if(feelsLike <= -1 && feelsLike > -10){
            clothes = "Winter jacket-Hat and Mittens-Scarf";
        }
        else if(feelsLike <= -10 && feelsLike > -18){
            clothes = "Winter jacket-Sweater-Hat and Mittens-Scarf";
        }
        else if(feelsLike <= -18 && feelsLike > -24){
            clothes = "Winter jacket-Sweater-Hat and Mittens-Scarf-Snow pants";
        }
        else if(feelsLike <= -24 && feelsLike > -30){
            clothes = "Winter jacket-Sweater-Hat and Mittens-Scarf-Snow pants-Onesie";
        }
        else if(feelsLike <= -30){
            clothes = "Stay inside-Onesie";
        }

        if (Arrays.asList(umbrellaArray).contains(weatherId)){
            clothes = clothes + "-Umbrella";
        }
        else if(Arrays.asList(rainCoat).contains(weatherId)){
            clothes = clothes + "-Rain coat-Rubber boots";
        }
        else if (weatherId == 800 && timeNow > sunrise && timeNow < sunset){
            clothes = clothes +"-Sunglasses";
        }
        else if (weatherId == 800 && feelsLike > 24 && timeNow > sunrise && timeNow < sunset){
            clothes = clothes +"-Sun hat-Sunglasses";
        } else if (weatherId == 801 || weatherId == 802 || weatherId == 803 && timeNow > sunrise && timeNow < sunset){ //********************************check here if error
            clothes = clothes +"-Sunglasses";
        }
        else if (weatherId == 906){
            clothes = "HAIL WARNING-Stay inside"; //hail
        }

        return clothes;
    }

    private float c2f(float temp){

        if (units.equals("imperial")){
            temp = temp*1.8f+32;
            unitSymbol = "F";
        }
        else {
            unitSymbol = "C";
            //Log.d(TAG, "comparator failed in c2f");
        }

        return temp;
    }

    protected void getUnits (Context context){

        SharedPreferences prefs = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        units = prefs.getString("units","metric");

        //Log.d(TAG, units+" getUnits func");

    }

    protected void getImage(){
        if (weatherId >= 200 && weatherId < 300){
            image = allInfo[3];//thunderstorm
        }
        else if (weatherId == 960 || weatherId == 961){
            image = allInfo[3];//thunderstorm
        }
        else if (weatherId >= 300 && weatherId < 600){
            image = allInfo[2];//rain
        }
        else if (weatherId >= 600 && weatherId < 700){
            image = allInfo[4];//snow
            if (timeNow < sunrise || timeNow > sunset){
                image = allInfo[7]; //cloudy night
            }
        }
       // else if (weatherId == 771 || weatherId == 902 || weatherId == 905 || weatherId == 956 || weatherId == 957 || weatherId == 958 || weatherId == 959 || weatherId == 962 || weatherId == 952 || weatherId == 953){
        //    image = "windy";//wind
        //}
        else if (weatherId == 804){

            image = allInfo[5];//overcast
            if (timeNow < sunrise || timeNow > sunset){
                image = allInfo[7];
            }

        }
       // else if (weatherId == 781 || weatherId == 900){
        //    image = "tornado";//tornado
       // }
        else if (weatherId == 903){
            image = allInfo[9];//extreme cold
        }
        else if (weatherId == 904){
            image = allInfo[1];//extreme heat
        }
        else if (weatherId == 801 || weatherId == 802 || weatherId == 803){

            image = allInfo[6];//cloudsday

            if (timeNow < sunrise || timeNow > sunset){
                image = allInfo[7]; //clouds night
            }

        }
        else if (weatherId == 800 || weatherId == 804){

            image = allInfo[0];//clearday

            if (timeNow < sunrise || timeNow > sunset){
                image = allInfo[8]; //clear night
            }
        }
        else if (feelsLike < -30){
            image = allInfo[4];
        }
        else {
            image = allInfo[10];//everything else
            if (timeNow < sunrise || timeNow > sunset){
                image = allInfo[8]; //clear night
            }
        }
        //Log.d(TAG, image);

    }
}
