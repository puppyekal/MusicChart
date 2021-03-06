package model.commentParser

import model.ChartData
import org.openqa.selenium.WebDriver
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Thread.sleep

class MelonAlbumCommentParser(var driver: WebDriver) {
	//Properties
	private val WEB_DRIVER_ID = "webdriver.chrome.driver"
	private val WEB_DRIVER_PATH = "src/driver/chromedriver.exe"
	private var base_url: String = "https://www.melon.com/album/detail.htm?albumId="

	init {
		System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH)
	}

	public fun crawl(): MutableMap<String, List<String>> {
		val result = mutableMapOf<String, List<String>>()
		val _setAlbumID = getAlbumIDtoSet()

		try {
			var doc: Document
			var arr: MutableList<String>
			var html: String

			for (id in _setAlbumID) {
				driver.get(base_url + id)
				html = driver.pageSource
				doc = Jsoup.parseBodyFragment(html)
				arr = doc.body().select("div.cntt").text().split("����").toMutableList()
				result[id] = arr.refine()
				sleep(1000);
			}

			for (ele in result) {
				println("Melon-Key${ele.key}")
				for (str in ele.value)
					println(str)
			}

		} catch (e: Exception) {
			e.printStackTrace()
		}

		return result
	}


	private fun MutableList<String>.refine(): MutableList<String> {
		val result = mutableListOf<String>()
		var str : String
		for(i in 3 until this.size){
			str = this[i].replace(" NEW ","").filter { it in '��'..'�R' || it.toInt() in 0..127 }
			if(str.contains("������ ��� �ٿ�ε� ���"))
				result.add(str.substring(1,str.indexOf("������ ��� �ٿ�ε� ���")))
			else
				result.add(str.substring(1))
		}
		return result
	}

	private fun getAlbumIDtoSet(): Set<String> {
		val result = mutableSetOf<String>()
		for (i in 1..100) {
			if (!ChartData.getS_instance().melonChartParser.isParsed)
				ChartData.getS_instance().melonChartParser.chartDataParsing(null)
			result.add(ChartData.getS_instance().melonChartParser.getAlbumID(i).filter { it in '0'..'9' })
		}
		return result
	}
}