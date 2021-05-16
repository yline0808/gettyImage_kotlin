package net.ddns.rsupporttest

import android.os.Bundle
import android.util.Log
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

        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = imgAdapter
            addOnScrollListener(scrollListener)
        }
        coroutine()
    }

    private fun coroutine() {
        CoroutineScope(Dispatchers.Main).launch {
            val loadingIdx:Int = rowImgList.size
            rowImgList.add(null)
            imgAdapter.notifyItemInserted(loadingIdx)

            Log.d("===please!!!1", "${rowImgList.size}")
            val isEnd:Boolean = withContext(Dispatchers.IO){ getImgSrc(rowImgList) }
//            val isEnd:Boolean = CoroutineScope(Dispatchers.IO).async { getImgSrc(rowImgList) }
            Log.d("===please!!!3", "${rowImgList.size}")

            rowImgList.removeAt(loadingIdx)
            imgAdapter.notifyDataSetChanged()
            isLoading = false

            if(isEnd){
                Toast.makeText(applicationContext, "last page!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getImgSrc(rowImgList:ArrayList<RowImgSrc?>):Boolean {
        Log.d("===please!!!2", "${rowImgList.size}")

        val WEB_URL = "https://www.gettyimages.com/photos/collaboration?page=%d&phrase=collaboration&sort=mostpopular"
        var isEnd = false

        try {
            val doc:Document = Jsoup.connect(
                String.format(WEB_URL, if (rowImgList.size <= 1) 1 else rowImgList.size / 20 + 1)
            ).get()

            val imgElementList:Elements = doc
                .select("div[class=search-content__gallery-assets]")
                .select("img")
            for (i in 0 until imgElementList.size step 3){
                rowImgList.add(
                    RowImgSrc(
                        imgElementList.get(i).attr("src"),
                        imgElementList.get(i+1).attr("src"),
                        imgElementList.get(i+2).attr("src")
                    )
                )
            }
        }catch (httpStatusException : HttpStatusException){
            httpStatusException.printStackTrace()
            isEnd = true
            Log.d("===please???EEE1", "${rowImgList.size}")
        }catch (exception :Exception){
            exception.printStackTrace()
            Log.d("===please???EEE2", "${rowImgList.size}")
        }
        return isEnd
    }

    private val scrollListener = object:RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            if(!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == rowImgList.size - 1){
                isLoading = true
                coroutine()
            }
        }
    }
}

//fun getImgSrc(rowImgList:ArrayList<RowImgSrc?>):Boolean {
//    Log.d("===please!!!2", "${rowImgList.size}")
//
//    val WEB_URL = "https://www.gettyimages.com/photos/collaboration?page=%d&phrase=collaboration&sort=mostpopular"
//    var isEnd = false
//
//    try {
//        val doc:Document = Jsoup.connect(
//            String.format(WEB_URL, if (rowImgList.size <= 1) 1 else rowImgList.size / 20 + 1)
//        ).get()
//
//        val imgElementList:Elements = doc
//            .select("div[class=search-content__gallery-assets]")
//            .select("img")
//        for (i in 0 until imgElementList.size step 3){
//            rowImgList.add(
//                RowImgSrc(
//                    imgElementList.get(i).attr("src"),
//                    imgElementList.get(i+1).attr("src"),
//                    imgElementList.get(i+2).attr("src")
//                )
//            )
//        }
//    }catch (httpStatusException : HttpStatusException){
//        httpStatusException.printStackTrace()
//        isEnd = true
//        Log.d("===please???EEE1", "${rowImgList.size}")
//    }catch (exception :Exception){
//        exception.printStackTrace()
//        Log.d("===please???EEE2", "${rowImgList.size}")
//    }
//    return isEnd
//}