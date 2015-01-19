package cz.jakubjirous.dlc;

/**
 * Created by James on 28. 12. 2014.
 */
public class ChartItem {

    private String order;
    private String coverURL;
    private String artist;
    private String title;
    private String soundURL;


    public ChartItem() {};

    public ChartItem(String order, String coverURL, String artist, String title, String soundURL) {
        this.order = order;
        this.coverURL = "http://www.danceradio.cz" + coverURL;
        this.artist = artist;
        this.title = title;
        this.soundURL = "http://www.danceradio.cz" + soundURL;
    }


    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrder() {
        return this.order;
    }


    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getCoverURL() {
        return this.coverURL;
    }


    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return this.artist;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setSoundURL(String soundURL) {
        this.soundURL = soundURL;
    }

    public String getSoundURL() {
        return this.soundURL;
    }

}
