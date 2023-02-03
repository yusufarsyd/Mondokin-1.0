package com.ngodesain.mondokin.ui.terdekat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.databinding.ListPondokTerdekatBinding

class PondokTerdekatAdapter : RecyclerView.Adapter<PondokTerdekatAdapter.ListViewHolder>() {

    private var listData = ArrayList<DataPondokTerdekat>()
    var onItemClick: ((DataPondokTerdekat) -> Unit)? = null

    fun setData(newListData: List<DataPondokTerdekat>?) {
        if (newListData == null) return
        listData.clear()
        listData.addAll(newListData)
        notifyDataSetChanged()
    }

    inner class ListViewHolder(view : View): RecyclerView.ViewHolder(view) {
        val binding = ListPondokTerdekatBinding.bind(view)
        fun bind(data: DataPondokTerdekat) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(data.foto)
                    .into(ivFotoPondokPesantren)
                tvNamaPondokPesantren.text = data.nama_pondok
                tvAlamat.text = data.alamat_pondok
                textView5.text = data.jarak.toString() + " Kilometer"
            }
        }

        init {
            binding.root.setOnClickListener {
                onItemClick?.invoke(listData[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder =
        ListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_pondok_terdekat, parent, false))


    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = listData[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int = listData.size


}