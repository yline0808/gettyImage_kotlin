package net.ddns.rsupporttest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import net.ddns.rsupporttest.R
import net.ddns.rsupporttest.databinding.ItemImgBinding
import net.ddns.rsupporttest.databinding.ItemLoadingBinding
import net.ddns.rsupporttest.item.RowImgSrc

class ImgAdapter(val rowSrcList: ArrayList<RowImgSrc?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 0;
        private const val VIEW_TYPE_LOADING = 1;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            VIEW_TYPE_ITEM -> ItemViewHolder(ItemImgBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> LoadingViewHolder(ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            VIEW_TYPE_ITEM -> (holder as ItemViewHolder).bind(rowSrcList[position]!!)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(rowSrcList[position]){
            null -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return rowSrcList.size
    }

    inner class ItemViewHolder(private val binding: ItemImgBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(imgSrc: RowImgSrc){
            setGlide(binding.imgLeft.context, imgSrc.leftImgSrc, binding.imgLeft)
            setGlide(binding.imgMiddle.context, imgSrc.middleImgSrc, binding.imgMiddle)
            setGlide(binding.imgRight.context, imgSrc.rightImgSrc, binding.imgRight)
        }
    }

    inner class LoadingViewHolder(private val binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root){
    }

    private fun setGlide(context: Context, url:String, imageView: ImageView){
        Glide.with(context)
            .load(url)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .centerCrop()
            .error(R.drawable.ic_launcher_background)
            .into(imageView)
    }
}