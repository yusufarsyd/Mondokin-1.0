package com.ngodesain.mondokin.ui.viewmaps

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.ui.home.DataPondokPesantren
import com.ngodesain.mondokin.databinding.FragmentViewMapsBinding
import com.ngodesain.mondokin.ui.activity.DetailActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions

class ViewMapsFragment : Fragment() {

    private var _binding: FragmentViewMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapboxMap: MapboxMap
    private var dataListPondokPesantren = ArrayList<DataPondokPesantren>()
    companion object {
        private const val ICON_ID = "ICON_ID"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_access_token))
        _binding = FragmentViewMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            getDataPondokMaps()
        }

    }
    //data map
    private fun getDataPondokMaps() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Daftar Pondok Pesantren")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dataListPondokPesantren.clear()
                for (getDataSnapshot in dataSnapshot.children) {

                    val daftarPondokPesantren = getDataSnapshot.getValue(DataPondokPesantren::class.java)
                    dataListPondokPesantren.add(daftarPondokPesantren!!)

                    mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                        style.addImage(ICON_ID, BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default))
                        val latLngBoundsBuilder = LatLngBounds.Builder()
                        val symbolManager = SymbolManager(binding.mapView, mapboxMap, style)
                        symbolManager.iconAllowOverlap = true
                        val options = ArrayList<SymbolOptions>()
                        dataListPondokPesantren.forEach { data ->
                            latLngBoundsBuilder.include(LatLng(data.latitude, data.longtitude))
                            options.add(
                                SymbolOptions()
                                    .withLatLng(LatLng(data.latitude, data.longtitude))
                                    .withIconImage(ICON_ID)
                                    .withTextField("${data.nama_pondok}")
                                    .withData(Gson().toJsonTree(data))
                            )
                        }
                        symbolManager.create(options)
                        val latLngBounds = latLngBoundsBuilder.build()
                        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 5000)
                        symbolManager.addClickListener { symbol ->
                            val data = Gson().fromJson(symbol.data, DataPondokPesantren::class.java)
                            val intent = Intent(context, DetailActivity::class.java)
                            intent.putExtra(DetailActivity.EXTRA_DATA, data)
                            startActivity(intent)
                        }
                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }
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

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

}
