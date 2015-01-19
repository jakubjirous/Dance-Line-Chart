package cz.jakubjirous.dlc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    MediaPlayer mp = new MediaPlayer();
    ArrayList<ChartItem> chartItems = new ArrayList<ChartItem>();
    TableLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // layout aplikace TableLayout zustane pred nactenim a stazenim dat z internetu schovany
        layout = (TableLayout)findViewById(R.id.table_layout);
        layout.setVisibility(View.GONE);

        // kontrola pripojeni k internetu
        if(isNetworkAvailable()) {
            Toast.makeText(this, "DANCE LINE CHART - Beat Of Your Heart", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Loading beats...", Toast.LENGTH_LONG).show();
            // stazeni dat z HTML stranky
            new DownloadWebsite().execute();

        } else
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    // otestovani pripojeni zarizeni k internetu
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork  = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
               activeNetwork.isConnectedOrConnecting();
    }



    /***********************************************************************************************
     *** Stazeni HTML stranky
     **********************************************************************************************/
    public class DownloadWebsite extends AsyncTask<Void, Void, String> {

        // adresa HTML stranky ze ktere se parsuji data
        public String websiteURL = "http://www.danceradio.cz/cs/dance-line-chart.shtml";

        @Override
        protected String doInBackground(Void... arg0) {
            String line = "";        // radek - pro ulozeni dat pri parsovani
            String html = "";        // zdrojovy kod HTML stranky
            InputStream is = null;

            // pokus o pripojeni k HTML strance
            try {
                URL url = new URL(websiteURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                // pokud je pripojeni navazano, dojde k ulozeni zdrojoveho kodu HTML stranky
                // cte se po radcich
                while ((line = reader.readLine()) != null) {
                    html += line;
                }

                is.close();
            } catch (Exception e) {
                return null;
            }

            return html;
        }


        @Override
        protected void onPostExecute(String text) {

            // parsovani jednotlivych elementu z HTML stranky
            Document doc = Jsoup.parse(text);
            Element h2 = doc.select("h2").first();
            Element ul = doc.select("ul").get(3);
            Elements chart = ul.getElementsByTag("li");

            for(Element el : chart) {

                Element order = el.getElementsByTag("span").first();        // poradi v hitparádě
                Element coverUrl = el.getElementsByTag("img").first();      // URL obrázku
                Element artist = el.getElementsByClass("artist").first();   // interpret
                Element title = el.getElementsByClass("song").first();      // název písničky
                Element soundUrl = el.getElementsByTag("a").first();        // URL na MP3 stream

                // chartItem reprezentuje jeden řádek v tabulce
                ChartItem item = new ChartItem(
                        order.text(),
                        coverUrl.attr("src"),
                        artist.text(),
                        title.text(),
                        soundUrl.attr("href"));

                chartItems.add(item);
            }

            // pole kde jsou uložená pořadí jednotlivých skladeb
            List<TextView> orders = new ArrayList<TextView>();
            for(int i = 1; i <= 30; i++) {
                String buttonID = "order" + (i);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                orders.add((TextView) findViewById(resID));
            }

            // pole obsahující URL na obrázky alb
            List<ImageView> covers = new ArrayList<ImageView>();
            for(int i = 1; i <= 30; i++) {
                String buttonID = "cover" + (i);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                covers.add((ImageView) findViewById(resID));
            }

            // pole interpretů
            List<TextView> artists = new ArrayList<TextView>();
            for(int i = 1; i <= 30; i++) {
                String buttonID = "artist" + (i);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                artists.add((TextView) findViewById(resID));
            }

            // pole názvů skladeb
            List<TextView> titles = new ArrayList<TextView>();
            for(int i = 1; i <= 30; i++) {
                String buttonID = "title" + (i);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                titles.add((TextView) findViewById(resID));
            }

            // vložení všech získaných dat do layoutu
            // pořadí
            for(int i = 0; i < orders.size(); i++) {
                orders.get(i).setText(chartItems.get(i).getOrder());
            }

            // vytvoření BMP z URL obrazku alba
            for(int i = 0; i < covers.size(); i++) {
                new DownloadImage().execute(covers.get(i), chartItems.get(i).getCoverURL());
            }

            // interpret
            for(int i = 0; i < artists.size(); i++) {
                artists.get(i).setText(chartItems.get(i).getArtist());
            }

            // název skladby
            for(int i = 0; i < titles.size(); i++) {
                titles.get(i).setText(chartItems.get(i).getTitle());
            }

            // po dokončení načtení a vložená všech dat do layoutu se deaktivuje jeho neviditelnost
            // TableLayout je visible
            layout.setVisibility(View.VISIBLE);
        }
    }



    // funkce pro přehrání MP3 ukázky skladby po kliknutí na tlačítko PLAY
    public void play(View v) {
        final int id = v.getId();
        String stream;
        List<ImageButton> players = new ArrayList<ImageButton>();

        // zíksání všech ID tlačítek pro přehrání z layoutu activity_main.xml
        for(int i = 1; i <= 30; i++) {
            String buttonID = "player" + (i);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            players.add((ImageButton) findViewById(resID));
        }

        // přehrávání
        for(int i = 0; i < players.size(); i++) {

            // přehrání konkrétní MP3 ukázky
            if(id == players.get(i).getId()) {
                final int position = i;
                final ImageButton activePlayer = players.get(i);
                stream = chartItems.get(position).getSoundURL();

                // pole přehrávačů, odstraní se vždy dotyčný přehrávač na které bylo kliknuto
                // animace na tlačítkách (PLAY / STOP)
                players.remove(players.get(position));
                for(ImageButton player : players) {
                    player.setBackgroundResource(android.R.drawable.ic_media_play);
                }

                if(mp.isPlaying()) {
                    stopStream(activePlayer);
                } else {
                    playStream(activePlayer, stream);
                }

                // automatické zastavení přehrávače po dohrání MP3 ukázky
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        activePlayer.setBackgroundResource(android.R.drawable.ic_media_play);
                    }
                });
            }
        }
    }


    // spuštění přehrávače MP3 přehrávače
    public void playStream(ImageButton ib, String stream) {
        try {
            mp.reset();
            mp.setDataSource(stream);
            mp.prepare();
            mp.start();

            ib.setBackgroundResource(android.R.drawable.ic_media_pause);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // zastavení přehrávače
    public void stopStream(ImageButton ib) {
        ib.setBackgroundResource(android.R.drawable.ic_media_play);

        mp.stop();
        mp.reset();
    }




    // vytvoření BMP z odkazu na obrázek na HTML stránce
    private class DownloadImage extends AsyncTask<Object, Void, Bitmap> {

        private String image;
        private ImageView imgView;

        @Override
        protected Bitmap doInBackground(Object... params) {
            InputStream is = null;
            Bitmap bmp = null;

            try {
                for (Object object : params) {
                    // string
                    if (object instanceof String) {
                        this.image = (String)object;

                        // odkaz na obrázek
                        URL url = new URL(this.image);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        is = conn.getInputStream();
                        bmp = BitmapFactory.decodeStream(is); // konverze streamu do bitmapy
                        bmp = Bitmap.createScaledBitmap(bmp, 40, 40, true);

                    }
                    // image view
                    else if (object instanceof ImageView) {
                        this.imgView = (ImageView)object;
                    }
                }

            } catch(MalformedURLException e) {
                return null;
            } catch (IOException e) {
                return null;
            }

            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            // vložení vytvořeného BMP obrázku do layoutu
            this.imgView.setImageBitmap(bmp);
        }
    }
}



