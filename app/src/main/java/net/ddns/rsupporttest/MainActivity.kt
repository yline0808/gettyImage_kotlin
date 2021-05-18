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

/*
-inflate
xml에 있는 레이아웃을 객체화 하는 행동
setContentView() 함수가 xml을 객체화 하는 inflate행동

-content
-activity
화면 UI를 담당하는 컴포넌

-context
시스템을 사용하기 위한 정보(프로퍼티)와 두구(메서드)가 담겨있는 클래스
컴포넌트 실행시 함께 생성
앱에서 사용하는 기본기능이 담긴 기본 클래스

-스코프 함수 (this[run, apply, with], it[let, also])
run : this를 생략 가능
let : it를 생략 불가능 하지만 다른 이름으로 변경 가능
 */

class MainActivity : AppCompatActivity() {
    // 늦은 초기화 불리는 시점에 괄호 안에 거로 초기화, 세이프 콜 방지
    // 레이아웃 인플레이터를 바인딩된 코틀린 메인 클래스로 변환하여 초기화
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // 여기서는 rowimgsrc에 null이 들어갈 수 있기 때문에 lazy 안씀
    private val rowImgList:ArrayList<RowImgSrc?> = ArrayList<RowImgSrc?>()
    private val imgAdapter:ImgAdapter = ImgAdapter(rowImgList)
    // 이미 로딩중에 다시 로딩을 하기 않기 위함
    private var isLoading:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩 된 root뷰를 contentView에 전달
        setContentView(binding.root)

        // recyclerView 세팅 this:recyclerView
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
        // dx, dy는 위치가 아닌 얼마나 스크롤 되었는지
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
        //launch를 통해 상태관리 가능 async는 연산 결과도 받을 수 있음
        CoroutineScope(Dispatchers.Main).launch {
            //나중에 로딩의 위치를 기억해서 지우기 위함
            val loadingIdx:Int = rowImgList.size
            rowImgList.add(null)
            imgAdapter.notifyItemInserted(loadingIdx)

            // rowImgList에 새로운 항목 추가
            // 코루틴 스코프 안에서 작성하여서 백그라운드 실행처럼 동작
            // suspend 키워드가 있는 함수를 사용하여 해당 함수가 끝나야 다음 동작 실
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

// jsoup을 사용한 이미지 src 파싱 & 아이템 배열 추가행
// suspend를 사용한 곳에서 다른 Dispatchers가능, withContext로 다른 Dispatchers로 변경 가능
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