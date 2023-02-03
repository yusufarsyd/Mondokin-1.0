package com.ngodesain.mondokin.ui.terdekat

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.ui.home.DataPondokPesantren
import com.ngodesain.mondokin.databinding.FragmentSearchBinding
import com.ngodesain.mondokin.ui.activity.DetailFromJarakActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlin.math.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    //data pondok
    private var dataListPondokPesantren = ArrayList<DataPondokPesantren>()


    //data pondok terdekat
    private var pondokTerdekatAdapter = PondokTerdekatAdapter()

    private lateinit var locationComponent: LocationComponent
    private lateinit var permissionsManager: PermissionsManager

    private lateinit var mapboxMap: MapboxMap

    var progressDialog : Dialog ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLoading()
        with(binding.rvJarakTerdekat) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = pondokTerdekatAdapter
        }
        if (activity != null) {
            pondokTerdekatAdapter.onItemClick = { selectedData ->
                val intent = Intent(activity, DetailFromJarakActivity::class.java)
                intent.putExtra(DetailFromJarakActivity.EXTRA_DATA, selectedData)
                startActivity(intent)
            }
        }

        binding.mapView.onCreate(savedInstanceState)
        getAllPondokPesantren()

    }

    private fun getAllPondokPesantren() {

        progressDialog?.show()

        val mDatabaseTravel =
            FirebaseDatabase.getInstance().getReference("Daftar Pondok Pesantren")

        mDatabaseTravel.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataListPondokPesantren.clear()
                for (getdataSnapshot in dataSnapshot.children) {
                    val daftarPondok = getdataSnapshot
                        .getValue(DataPondokPesantren::class.java)
                    dataListPondokPesantren.add(daftarPondok!!)

                }
                getLastLocation(dataListPondokPesantren)


            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                progressDialog?.dismiss()

            }

        })

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(dataPondok: ArrayList<DataPondokPesantren>) {
        progressDialog?.show()

        binding.mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
                    val locationComponentOptions =
                        LocationComponentOptions.builder(requireContext())
                            .pulseEnabled(true)
                            .pulseColor(Color.BLUE)
                            .pulseAlpha(.4f)
                            .pulseInterpolator(BounceInterpolator())
                            .build()
                    val locationComponentActivationOptions = LocationComponentActivationOptions
                        .builder(requireContext(), style)
                        .locationComponentOptions(locationComponentOptions)
                        .build()
                    locationComponent = mapboxMap.locationComponent
                    locationComponent.activateLocationComponent(locationComponentActivationOptions)
                    locationComponent.isLocationComponentEnabled = true
                    locationComponent.cameraMode = CameraMode.TRACKING
                    locationComponent.renderMode = RenderMode.COMPASS
                    // get posisi
                    val mylocation = locationComponent.lastKnownLocation?.latitude?.let { locationComponent.lastKnownLocation?.longitude?.let { it1 -> LatLng(it, it1) } }
                    val myLatitude = mylocation?.latitude
                    val myLongitude = mylocation?.longitude


                    val resultList: ArrayList<DataPondokTerdekat> = ArrayList()

                    dataPondok.forEach { data ->

                        if (myLatitude != null && myLongitude != null){
                            val desLat = data.latitude * PI / 180
                            val desLong = data.longtitude * PI / 180
                            val myLang = myLatitude * PI / 180
                            val myLo = myLongitude * PI / 180
                            val finalLat = desLat - myLang
                            val finalLong = desLong - myLo

                            val a =
                                sin(finalLat / 2) * sin(finalLat / 2) + cos(desLat) * cos(myLatitude)* cos(myLatitude) * sin(finalLong / 2   ) * sin(finalLong / 2)

                            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
                            val d = 6371 * c

                            resultList.add(
                                DataPondokTerdekat(
                                    data.alamat_pondok.toString(),
                                    data.foto.toString(),
                                    data.latitude,
                                    data.longtitude,
                                    d,
                                    data.nama_pondok.toString(),
                                    data.profile.toString()
                                )
                            )
                            resultList.sortBy({ selector(it) })

                        }

                    }

                    pondokTerdekatAdapter.setData(resultList)
                    pondokTerdekatAdapter = PondokTerdekatAdapter()
                    progressDialog?.dismiss()


                } else {
                    progressDialog?.dismiss()
                    permissionsManager = PermissionsManager(object : PermissionsListener {
                        override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
                            Toast.makeText(
                                context,
                                "Anda harus mengizinkan location permission untuk menggunakan aplikasi ini",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onPermissionResult(granted: Boolean) {
                            if (granted) {
                                mapboxMap.getStyle { style ->
                                }
                            }
                        }
                    })
                    permissionsManager.requestLocationPermissions(requireActivity())

                }
            }
        }
    }

    private fun isLoading() {
        progressDialog = Dialog(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_loading, null)

        progressDialog?.let {
            it.setContentView(dialogLayout)
            it.setCancelable(false)
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    fun selector(p: DataPondokTerdekat): Double = p.jarak

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }


}
