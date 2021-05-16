package net.ddns.rsupporttest

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ddns.rsupporttest.adapter.ImgAdapter
import net.ddns.rsupporttest.databinding.ActivityMainBinding
import net.ddns.rsupporttest.item.RowImgSrc
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class MainActivity : AppCompatActivity() {
    private val rowImgList:ArrayList<RowImgSrc?> = ArrayList<RowImgSrc?>()
    private val imgAdapter:ImgAdapter = ImgAdapter(rowImgList)
    private var isLoading:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = imgAdapter
            binding.recyclerView.addOnScrollListener(scrollListener)
        }
        GetImgSrc().execute(rowImgList.size)
    }

    val scrollListener = object:RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager;
            if(!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == rowImgList.size - 1){
                recyclerView.scrollToPosition(rowImgList.size - 1)
                GetImgSrc().execute(rowImgList.size)
                isLoading = true
            }
        }
    }

    inner class GetImgSrc:AsyncTask<Int, Void, Int>(){
        private val WEB_URL: String = "https://www.gettyimages.com/photos/collaboration?page=%d&phrase=collaboration&sort=best"
        private var isEnd:Boolean = false

        override fun onPreExecute() {
            rowImgList.add(null)
            imgAdapter.notifyItemInserted(rowImgList.size - 1)
        }

        override fun doInBackground(vararg itemCnt: Int?): Int? {
            try {
                val doc:Document = Jsoup.connect(
                    String.format(WEB_URL, if (itemCnt[0]!! <= 1) 1 else itemCnt[0]!! / 20 + 1)
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
            }catch (exception :Exception){
                exception.printStackTrace()
            }
            return itemCnt[0]
        }

        override fun onPostExecute(result: Int) {
            isLoading = false
            rowImgList.removeAt(result)
            imgAdapter.notifyDataSetChanged()
            if(isEnd){
                Toast.makeText(applicationContext, "last page!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}