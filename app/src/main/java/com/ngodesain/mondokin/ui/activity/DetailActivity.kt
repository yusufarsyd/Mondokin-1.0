package com.ngodesain.mondokin.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.ngodesain.mondokin.ui.home.DataPondokPesantren
import com.ngodesain.mondokin.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {


    companion object {
        const val EXTRA_DATA = "extra_data"

    }


    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val detailPondok = intent.getParcelableExtra<DataPondokPesantren>(EXTRA_DATA)!!
        showDetailTourism(detailPondok)

        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }


    }


    private fun showDetailTourism(data: DataPondokPesantren) {

        binding.tvNamaDetail.text = data.nama_pondok.toString()
        binding.tvAlamatDetail.text = data.alamat_pondok.toString()
        binding.tvProfile.text = data.profile.toString()
        Glide.with(this)
            .load(data.foto)
            .into(binding.ivFoto)

        binding.button.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("nama_pondok_tujuan", data.nama_pondok.toString())
            intent.putExtra("latitude_tujuan", data.latitude.toString())
            intent.putExtra("longtitude_tujuan", data.longtitude.toString())
            startActivity(intent)
        }


    }
}