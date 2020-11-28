package model;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.awt.*;
public class DetailData extends SiteMBG{
    private static DetailData s_instance;

    private MelonChartParser detailMelon;
    private BugsChartParser detailBugs;
    private GenieChartParser detailGenie;

    private DetailData(){
        s_instance = this;

        detailMelon = new MelonChartParser();
        detailBugs = new BugsChartParser();
        detailGenie = new GenieChartParser();
    }
    public MusicChartParser getParser() {
        switch (ChartData.getS_instance().getSite_M_B_G()) {
            case MELON:
                return detailMelon;
            case BUGS:
                return detailBugs;
            case GENIE:
                return detailGenie;
            default:
                return null;
        }

    }
    public void detailDataPassing(int rank, JSONArray chartListData, Component parentComponent){
        System.out.println("Parsing Detail Data");
        getParser().songDetailDataParsing(((JSONObject) chartListData.get(rank - 1)), parentComponent);
    }

    public MusicChartParser getDetailParser() {
        System.out.println("Parsing Detail Data");
        return getParser();
    }
    public static DetailData getS_instance() {
        if(s_instance == null) s_instance = new DetailData();
        return s_instance;
    }
}
