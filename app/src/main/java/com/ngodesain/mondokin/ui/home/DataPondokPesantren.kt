package com.ngodesain.mondokin.ui.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataPondokPesantren(
    var alamat_pondok: String?="",
    var foto: String? = "",
    var latitude: Double = 0.0,
    var longtitude: Double= 0.0,
    var nama_pondok: String? ="",
    var profile: String? =""
) : Parcelable
