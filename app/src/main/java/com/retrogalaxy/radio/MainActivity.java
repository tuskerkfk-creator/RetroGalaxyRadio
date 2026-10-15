package com.retrogalaxy.radio;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
  private WebView web;
  private MediaPlayer player;
  private float volume = 0.6f;
  private boolean muted = false;
  private int playbackGeneration = 0;
  private final Object playerLock = new Object();
  @Override public void onCreate(Bundle b) { super.onCreate(b);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    web = new WebView(this);
    web.setBackgroundColor(0xff141918);
    WebSettings s = web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true);
    web.addJavascriptInterface(new Bridge(), "RadioBridge");
    web.setWebViewClient(new WebViewClient());
    setContentView(web);
    web.loadUrl("file:///android_asset/index.html");
  }
  private void js(final String call) {runOnUiThread(new Runnable(){public void run(){if(web!=null)web.evaluateJavascript(call,null);}});}
  private void emit(String state) {js("window.radioState("+JSONObject.quote(state)+")");}
  private void applyVolume(){if(player!=null){float level=muted?0f:volume;try{player.setVolume(level,level);}catch(Exception ignored){}}}
  private void stopPlayer() { synchronized(playerLock) { playbackGeneration++; if(player!=null){try{player.reset();player.release();}catch(Exception ignored){}player=null;} } }
  private class Bridge {
    @JavascriptInterface public void stop(){runOnUiThread(new Runnable(){public void run(){stopPlayer();emit("stopped");}});}
    @JavascriptInterface public void volume(int v){volume=Math.max(0,Math.min(100,v))/100f;runOnUiThread(new Runnable(){public void run(){applyVolume();}});}
    @JavascriptInterface public void mute(final boolean value){runOnUiThread(new Runnable(){public void run(){muted=value;applyVolume();}});}
    @JavascriptInterface public void play(final String url){runOnUiThread(new Runnable(){public void run(){
      if(!url.startsWith("https://")&&!url.startsWith("http://")){emit("error:Invalid URL");return;}
      stopPlayer(); final int generation=playbackGeneration; emit("buffering");
      try { final MediaPlayer mp=new MediaPlayer(); player=mp; mp.setAudioStreamType(AudioManager.STREAM_MUSIC); mp.setVolume(muted?0f:volume,muted?0f:volume);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){public void onPrepared(MediaPlayer m){if(generation!=playbackGeneration)return;try{m.start();emit("playing");}catch(Exception e){emit("error:Cannot start audio");}}});
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener(){public boolean onError(MediaPlayer m,int what,int extra){if(generation==playbackGeneration){emit("error:Network or unsupported stream");stopPlayer();}return true;}});
        mp.setDataSource(url);mp.prepareAsync();
      } catch(Exception e){Log.w("RetroRadio","Audio error",e);emit("error:Failed to open stream");stopPlayer();}
    }});}
    @JavascriptInterface public void weather(final String city){new Thread(new Runnable(){public void run(){try{
      String needle=city.split(",")[0].trim(); if(needle.length()==0)throw new Exception("City empty");
      String geo=get("https://geocoding-api.open-meteo.com/v1/search?name="+java.net.URLEncoder.encode(needle,"UTF-8")+"&count=1&language=en&format=json");
      JSONObject geoObj=new JSONObject(geo); JSONArray res=geoObj.optJSONArray("results"); if(res==null||res.length()==0)throw new Exception("City not found: "+needle); JSONObject place=res.getJSONObject(0);
      String weather=get("https://api.open-meteo.com/v1/forecast?latitude="+place.getDouble("latitude")+"&longitude="+place.getDouble("longitude")+"&current=temperature_2m,weather_code&timezone=auto");
      JSONObject current=new JSONObject(weather).getJSONObject("current");int code=current.getInt("weather_code");String desc="SUNNY",icon="☀";
      if(code>=1&&code<=3){desc="PARTLY CLOUDY";icon="⛅";} else if(code<=48&&code>=4){desc="FOGGY";icon="☁";}else if(code<=67&&code>=49){desc="RAIN";icon="☂";}else if(code<=77&&code>=68){desc="SNOW";icon="❄";}else if(code<=82&&code>=78){desc="SHOWERS";icon="☂";}else if(code<=86&&code>=83){desc="SNOW";icon="❄";}else if(code>86){desc="STORM";icon="⚡";}
      JSONObject out=new JSONObject();out.put("temp",current.getDouble("temperature_2m"));out.put("condition",desc);out.put("icon",icon);out.put("city",place.getString("name"));js("window.weatherResult("+JSONObject.quote(out.toString())+")");
    }catch(Exception e){Log.w("RetroRadio","Weather error",e); try{JSONObject out=new JSONObject();out.put("error", e.getMessage()==null?"Network error":e.getMessage());js("window.weatherResult("+JSONObject.quote(out.toString())+")");}catch(Exception ignored){}}}}).start();}
  }
  private static String get(String u) throws Exception {HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(9000);c.setReadTimeout(9000);c.setRequestProperty("User-Agent","RetroGalaxyRadio/1.0");try{InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>300000)throw new Exception("Response too long");}in.close();return new String(out.toByteArray(),"UTF-8");}finally{c.disconnect();}}
  @Override protected void onPause(){super.onPause();}
  @Override protected void onDestroy(){stopPlayer();if(web!=null)web.destroy();super.onDestroy();}
  @Override public void onBackPressed(){if(web!=null){web.evaluateJavascript("hideSettings()",null);}else super.onBackPressed();}
}
