package com.ngodesain.mondokin.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.databinding.ListPondokPesantrenBinding

class PondokPesantrenAdapter : RecyclerView.Adapter<PondokPesantrenAdapter.ListViewHolder>() {

    private var listData = ArrayList<DataPondokPesantren>()
    var onItemClick: ((DataPondokPesantren) -> Unit)? = null

    fun setData(newListData: List<DataPondokPesantren>?) {
        if (newListData == null) return
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_pondok_pesantren, parent, false))

    override fun getItemCount() = listData.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = listData[position]
        holder.bind(data)
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ListPondokPesantrenBinding.bind(itemView)
        fun bind(data: DataPondokPesantren) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(data.foto)
                    .into(ivFotoPondokPesantren)
                tvNamaPondokPesantren.text = data.nama_pondok
                tvAlamat.text = data.alamat_pondok
            }
        }

        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(listData[adapterPosition])
            }
        }
    }
}