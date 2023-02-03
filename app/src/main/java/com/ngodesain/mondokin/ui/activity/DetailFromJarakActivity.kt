package com.ngodesain.mondokin.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.ngodesain.mondokin.databinding.ActivityDetailFromJarakBinding
import com.ngodesain.mondokin.ui.terdekat.DataPondokTerdekat
import com.ngodesain.mondokin.ui.terdekat.MapsTerdekatActivity

class DetailFromJarakActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DATA = "extra_data"

    }
    private lateinit var binding : ActivityDetailFromJarakBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFromJarakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val detailPondok = intent.getParcelableExtra<DataPondokTerdekat>(EXTRA_DATA)!!
        showDetailTourism(detailPondok)
        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }

    }

    private fun showDetailTourism(data: DataPondokTerdekat) {
        binding.tvNamaDetail.text = data.nama_pondok.toString()
        binding.tvAlamatDetail.text = data.alamat_pondok.toString()
        binding.tvProfileJarak.text = data.profile.toString()
        Glide.with(this)
            .load(data.foto)
            .into(binding.ivFoto)
        binding.button.setOnClickListener {
            val intent = Intent(this, MapsTerdekatActivity::class.java)
            intent.putExtra("latitudeTerdekat", data.latitude.toString())
            intent.putExtra("longitudeTerdekat", data.longtitude.toString())
            intent.putExtra("namaTerdekat", data.nama_pondok.toString())
            startActivity(intent)
        }
    }
}