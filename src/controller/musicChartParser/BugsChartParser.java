package controller.musicChartParser;

import java.awt.Component;
import java.util.HashMap;

import controller.musicChartParser.MusicChartParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author SejongUniv 오창한
 * @version 1.7
 **/

public class BugsChartParser extends MusicChartParser {

    /*
     * BugsChartParser Description (KO_KR)
     *
     **************************************************
     *
     * ** 차트 100곡을 파싱할 시에 얻을 수 있는 것들 **
     * 노래 아이디		(key : songId)
     * 노래 순위		(key : rank)
     * 노래 작은 이미지	(key : smallImageUrl)
     * 노래 제목		(key : title)
     * 가수 이름		(key : artist)
     * 앨범 이름		(key : albumName)
     *
     * ** 차트 100곡을 파싱할 시에 사용 가능한 메소드 **
     * - 메소드 이름이 같은 메소드들은 반환형이 모두 같다.
     * - [반환형] 메소드이름() 과 같이 표기했다.
     *
     * <차트 100곡 파싱 관련 메소드>
     * [void]		chartDataParsing(Component parentComponent)
     * [boolean]	isParsed()
     *
     * <차트 100곡 노래 정보 get 메소드>
     * [JSONArray]	getChartList()
     * [JSONObject]	getSongData(int rank)	getSongData(String title)
     * [int]		getRank(String title)	getRank(JSONObject jObj)
     * [String]		getTitle(int rank)		getTitle(JSONObject jObj)
     * [String]		getArtistName(int rank)	getArtistName(String title)		getArtistName(JSONObject jObj)
     * [String]		getAlbumName(int rank)	getAlbumName(String title)		getAlbumName(JSONObject jObj)
     * [String]		getImageUrl(int rank)	getImageUrl(String title)		getImageUrl(JSONObject jObj)
     * [String]		getSongId(int rank)		getSongId(String title)			getSongId(JSONObject jObj)
     *
     **************************************************
     *
     * ** 노래 1개에 대한 상세 정보를 파싱할 시에 얻을 수 있는 것들 **
     * 노래 큰 이미지		(key : imageUrl)
     * 노래 재생시간		(key : songTime)
     * 노래 좋아요 개수	(key : likeNum)
     *
     * ** 노래 1개에 대한 상세 정보를 파싱할 시에 사용 가능한 메소드 **
     * - 메소드 이름이 같은 메소드들은 반환형이 모두 같다.
     * - [반환형] 메소드이름() 과 같이 표기했다.
     *
     * <노래 1개에 대한 상세 정보 파싱 관련 메소드>
     * [void]		songDetailDataParsing(String songId, Component parentComponent)
     * [void]		songDetailDataParsing(JSONObject jObj, Component parentComponent)
     * [void]		songDetailDataParsing(int rank, JSONArray chartListData, Component parentComponent)
     * [void]		songDetailDataParsing(String title, JSONArray chartListData, Component parentComponent)
     * [boolean]	isParsed()
     *
     * <노래 1개에 대한 상세 정보 get 메소드>
     * [JSONObject]	getSongData()
     * [String]		getImageUrl()		getImageUrl(JSONObject jObj)	getImageUrl(int rank)	getImageUrl(String title)
     * [String]		getSongTime()		getSongTime(JSONObject jObj)
     * [String]		getLikeNum()		getLikeNum(JSONObject jObj)
     *
     **************************************************
     *
     */

    private String bugsChartParsingTitle = "벅스 차트 파싱중..";
    private String bugsChartParsingMessage = "벅스 차트 100곡에 대한 정보를 불러오는 중 입니다 :)";

    public BugsChartParser() { // 초기화 작업을 진행함
        _songCount = 0;                // 파싱한 노래 개수(초기값은 0)
        _chartList = null;            // 차트 100곡에 대한 정보를 담을 JSONArray
        _songDetailInfo = null;        // 노래 한 곡에 대한 상세 정보를 담을 JSONObject
        _url = null;                    // 파싱할 웹 사이트 url
        _chartThread = null;            // 차트 100곡 파싱에 사용할 Thread
        _songDetailThread = null;    // 노래 한 곡에 대한 상세 정보 파싱에 사용할 Thread
        progressMonitor = null;    // ProgressMonitor를 사용하면 Thread가 종료되지 않는 버그와 ProgressMonitor가 제대로 나오지 않는 버그가 발생하여 사용하는 부분은 주석처리 해두었음
    } // constructor

    private class ChartDataParsingThread implements Runnable { // 차트 100곡 파싱을 하는 Runnable class
        @Override
        public void run() {
            // 벅스 차트 1~100위의 노래를 파싱함
            _songCount = 0;
            _url = "https://music.bugs.co.kr/chart";

            try {
                // 벅스 차트 연결에 필요한 header 설정 및 연결
                Connection bugsConnection = Jsoup.connect(_url).header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                        .header("Sec-Fetch-User", "?1").header("Upgrade-Insecure-Requests", "1")
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                        .method(Connection.Method.GET);

                // 연결 후 웹페이지를 긁어옴
                Document bugsDocument = bugsConnection.get();

                // 1~100위에 대한 정보를 불러옴, 순위와 곡의 상세한 정보를 뽑기 위한 링크를 뽑는 용도로 사용
                Elements data1st100 = bugsDocument.select("table.list").first().select("tr[rowtype=track]");

                _chartList = new JSONArray();

                for (Element elem : data1st100) { // 1~100위에 대한 내용 파싱
                    // JSONObject에 데이터를 넣기 위한 작업
                    HashMap<String, Object> songAllInfo = new HashMap<String, Object>();

                    // key : rank, value : 순위
                    songAllInfo.put("rank", elem.select("div.ranking > strong").first().text().toString());

                    // key : smallImageUrl, value : 작은 이미지 url
                    songAllInfo.put("smallImageUrl", elem.select("img").attr("src").toString());

                    // key : songId, value : 노래 아이디
                    songAllInfo.put("songId", elem.select("tr").attr("trackid").toString());

                    // key : title, value : 제목
                    songAllInfo.put("title", elem.select("th[scope=row]").first().select("a").first().text().toString());

                    // key : artist, value : 가수 이름
                    songAllInfo.put("artist", elem.select("td.left").first().select("a").first().text().toString());

                    // key : albumName, value : 앨범 이름
                    songAllInfo.put("albumName", elem.select("td.left").get(1).select("a").first().text().toString());

                    songAllInfo.put("albumID", elem.select("td.left").get(1).select("a").attr("href").substring(31,39));

                    // 값들을 JSONObject로 변환
                    JSONObject jsonSongInfo = new JSONObject(songAllInfo);

                    // JSONArray에 값 추가, 노래 개수 증가
                    _chartList.add(jsonSongInfo);
                    _songCount++;
                    // progressMonitor.setProgress(songCount);
                }

                // 파싱 결과 출력(테스트용)
				/*
				for (Object o : chartList) {
					if (o instanceof JSONObject)
						System.out.println(((JSONObject) o));
				}
				*/

            } catch (HttpStatusException e) {
                e.printStackTrace();
                _chartList = null;
                _songDetailInfo = null;
                System.out.println("많은 요청으로 인해 불러오기에 실패하였습니다.");
                _songCount = 0;
                return;
            } catch (NullPointerException e) { // 데이터 긁어오는 데에 실패했을 때(태그나 속성이 없을 때)
                e.printStackTrace();
                _chartList = null;
                _songDetailInfo = null;
                System.out.println("Url 링크가 잘못되었거나, 웹 페이지 구조가 변경되어 파싱에 실패했습니다 :(");
                _songCount = 0;
                return;
            } catch (Exception e) { // 그 외의 모든 에러
                e.printStackTrace();
                _chartList = null;
                _songDetailInfo = null;
                System.out.println("파싱도중 에러가 발생했습니다 :(");
                _songCount = 0;
                return;
            }
        } // run()
    } // ChartDataParsingThread Runnable class

    private class SongDetailDataParsingThread implements Runnable { // 노래 한 곡에 대한 상세 파싱을 하는 Runnable class
        @Override
        public void run() {
            // 노래 한 곡에 대한 상세 정보 파싱
            _songCount = 0; // 노래 개수 초기화
            HashMap<String, Object> songAllInfo = new HashMap<String, Object>();

            try {
                // songId를 통해 곡에 대한 상세한 정보를 얻기 위한 접근
                Connection songDetailConnection = Jsoup.connect(_url).header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                        .header("Sec-Fetch-User", "?1").header("Upgrade-Insecure-Requests", "1")
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                        .method(Connection.Method.GET);

                // 곡에 대한 상세한 정보 웹 페이지를 긁어옴
                Document songDetailDocument = songDetailConnection.get();
                Element songDetailInfo = songDetailDocument.select("div.basicInfo").first();

                Element songDetailAlbumInfo = songDetailInfo.select("table.info").first().select("tbody").first();
                Element songDetailLikeInfo = songDetailDocument.select("div.etcInfo").first();

                // key : imageUrl, value : 큰 이미지 url 링크
                songAllInfo.put("imageUrl",
                        songDetailInfo.select("div.photos").first().select("ul > li > a > img").attr("src").toString());

                // key : songTime, value : 재생 시간
                songAllInfo.put("songTime",
                        songDetailAlbumInfo.select("tr").get(3).select("td > time").get(0).text().toString());

                // key : likeNum, value : 좋아요 개수
                songAllInfo.put("likeNum",
                        songDetailLikeInfo.select("span").first().select("a > span > em").first().text().toString());

            } catch (HttpStatusException e) {
                e.printStackTrace();
                _chartList = null;
                _songDetailInfo = null;
                System.out.println("많은 요청으로 인해 불러오기에 실패하였습니다.");
                _songCount = 0;
                return;
            } catch (NullPointerException e) { // 데이터 긁어오는 데에 실패했을 때(태그나 속성이 없을 때)
                e.printStackTrace();
                _chartList = null;
                _songDetailInfo = null;
                System.out.println("Url 링크가 잘못되었거나, 웹 페이지 구조가 변경되어 파싱에 실패했습니다 :(");
                _songCount = 0;
                return;
            } catch (Exception e) { // 그 외의 모든 에러
                e.printStackTrace();
                _chartList = null;
                _songDetailInfo = null;
                System.out.println("파싱도중 에러가 발생했습니다 :(");
                _songCount = 0;
                return;
            }
            _songDetailInfo = new JSONObject(songAllInfo); // HashMap을 JSONObject로 변환하여 저장
            _songCount++; // 노래 개수 증가
        } // run()
    } // SongDetailDataParsingThread Runnable class

    @Override
    public void chartDataParsing(Component parentComponent) { // 차트 100곡을 파싱하는 Thread를 시작하는 메소드
        if (_chartThread != null) { // Thread를 사용하는게 처음이 아닐 때
            if (_chartThread.isAlive()) // Thread가 살아있으면 정지
                _chartThread.stop();
        }
        _chartThread = new Thread(new ChartDataParsingThread()); // Thread는 재사용이 안되기 때문에 다시 객체를 생성함
        // progressMonitorManager(parentComponent, bugsChartParsingTitle, bugsChartParsingTitle);
        _chartThread.start(); // Thread 시작
        try {
            _chartThread.join(); // ChartDataParsingThread가 종료되기전까지 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } // chartDataParsing(Component parentComponent)
    @Override
    public void songDetailDataParsing(int rank, JSONArray chartListData, Component parentComponent) { // 노래 한 곡에 대한 상세 정보를 파싱하는 Thread를 시작하는 메소드
        if (chartListData == null) {
            System.out.println("차트 파싱된 데이터가 없어 메소드 실행을 종료합니다 :(");
            return;
        }
        JSONObject jObj = (JSONObject) chartListData.get(rank - 1);
        detailDataparsing(jObj, parentComponent);
    } // songDetailDataParsing(int rank, JSONArray chartListData, Component parentComponent)
    @Override
    public void songDetailDataParsing(JSONObject obj, Component parentComponent) { // 노래 한 곡에 대한 상세 정보를 파싱하는 Thread를 시작하는 메소드
        if (obj == null) {
            System.out.println(_plzUseRightJSONObject);
            return;
        }

        if (!obj.containsKey("songId")) { // songId key값 유효성 검사
            System.out.println(_jsonDontHaveKey);
            return;
        }
        JSONObject _jObj = obj;
        detailDataparsing(_jObj, parentComponent);

    } // songDetailDataParsing(JSONObject jObj, Component parentComponent)
    @Override
    void detailDataparsing(JSONObject jObj, Component parentComponent){
        _url = "https://music.bugs.co.kr/track/" + jObj.get("songId").toString() + "?wl_ref=list_tr_08_chart"; // 파싱할 url을 만듬
        if (_songDetailThread != null) { // Thread를 사용하는 게 처음이 아닐 때
            if (_songDetailThread.isAlive()) // Thread가 살아있으면 정지
                _songDetailThread.stop();
        }
        _songDetailThread = new Thread(new SongDetailDataParsingThread()); // Thread는 재사용이 안되기 때문에 다시 객체를 생성함
        _songDetailThread.start();
        try {
            _songDetailThread.join(); // SongDetailDataParsingThread가 종료되기 전까지 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // songDetailDataParsing 후에만 사용가능한 메소드
    public String getLikeNum() { // 노래 한 곡에 대한 상세 파싱이 이루어졌다면 그 곡의 좋아요 개수를 반환하는 메소드
        if(isSongDetailParsed("likeNum") != null){
            return isSongDetailParsed("likeNum");
        }
        return null;
    } // String getLikeNum()

	/*  벅스는 발매일(releaseDate)와 장르(genre)를
	 	웹 페이지에서 보여주지 않아 getReleaseDate 메소드와 getGenre메소드가 없음		*/

    // songDetailDataParsing 후에만 사용가능한 메소드
    public String getSongTime() { // 노래 한 곡에 대한 상세 파싱이 이루어졌다면 그 곡의 재생 시간을 반환하는 메소드
        if(isSongDetailParsed("songTime") != null){
            return isSongDetailParsed("songTime");
        }
        return null;
    } // String getSongTime()

    // songDetailDataParsing 후에만 사용가능한 메소드
    public String getSongTime(JSONObject jObj) { // 노래 한 곡에 대한 상세 파싱이 이루어졌다면 JSONObject를 이용하여 그 곡의 재생 시간을 반환하는 메소드
        if (!isParsed()) { // 파싱이 이루어지지 않았다면
            System.out.println(_isNotParsed);
            return null;
        }

        if (_songCount == 1) {// 노래 한 곡에 대한 상세 파싱이 이루어졌다면
            JSONObject _jobj = jObj;
            if(isJSONObject("songTime",_jobj) != null){
                return isJSONObject("songTime",_jobj);
            }
            return null;
        }
        return null;
    } // String getSongTime(JSONObject jObj)


}
