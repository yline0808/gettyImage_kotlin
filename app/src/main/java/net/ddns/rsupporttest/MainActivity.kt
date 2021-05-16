package net.ddns.rsupporttest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import net.ddns.rsupporttest.adapter.ImgAdapter
import net.ddns.rsupporttest.databinding.ActivityMainBinding
import net.ddns.rsupporttest.item.RowImgSrc
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val rowImgList:ArrayList<RowImgSrc?> = ArrayList<RowImgSrc?>()
    private val imgAdapter:ImgAdapter = ImgAdapter(rowImgList)
    private var isLoading:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // recyclerView 세팅
        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = imgAdapter
            addOnScrollListener(scrollListener)
        }

        // 첫번째 페이지 가져오기
        getRowItemList()
    }

    // 스크롤 리스너
    private val scrollListener = object:RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

            // 로딩중이 아니면서 마지막에 도달했을 경우 새로운 이미지 불러옴
            if(!isLoading
                && layoutManager != null
                && layoutManager.findLastCompletelyVisibleItemPosition() == rowImgList.size - 1
            ){
                isLoading = true
                getRowItemList()
            }
        }
    }

    // 로딩세팅 & 이미지 src 가져오는 함수
    private fun getRowItemList() {
        CoroutineScope(Dispatchers.Main).launch {
            val loadingIdx:Int = rowImgList.size
            rowImgList.add(null)
            imgAdapter.notifyItemInserted(loadingIdx)

            // rowImgList에 새로운 항목 추가
            val isEnd:Boolean = fetchImgSrc(rowImgList)

            rowImgList.removeAt(loadingIdx)
            imgAdapter.notifyDataSetChanged()
            isLoading = false

            // 마지막 페이지에 도달했을 경우
            if(isEnd){
                Toast.makeText(applicationContext, "last page!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// jsoup을 사용한 이미지 src 파싱 & 아이템 배열 추가
suspend fun fetchImgSrc(rowImgList:ArrayList<RowImgSrc?>):Boolean = withContext(Dispatchers.IO) {
    val WEB_URL = "https://www.gettyimages.com/photos/collaboration?page=%d&phrase=collaboration&sort=mostpopular"
    val pageNum:Int = if (rowImgList.size <= 1) 1 else (rowImgList.size - 1) / 20 + 1
    var isEnd = false

    try {
        val doc:Document = Jsoup.connect(String.format(WEB_URL, pageNum)).get()

        // html 에서 img 태그에 해당하는 부분만 파싱
        val imgElementList:Elements = doc
            .select("div[class=search-content__gallery-assets]")
            .select("img")

        // 1개의 item에 3개의 이미지 src를 받기 위해 3step씩 증가하며 src 파싱
        for (i in 0 until imgElementList.size step 3){
            rowImgList.add(
                RowImgSrc(
                    imgElementList[i].attr("src"),
                    imgElementList[i+1].attr("src"),
                    imgElementList[i+2].attr("src")
                )
            )
        }
    }catch (httpStatusException : HttpStatusException){ //마지막 페이지에서 status 404처리
        httpStatusException.printStackTrace()
        isEnd = true
    }catch (exception :Exception){
        exception.printStackTrace()
    }
    isEnd
}