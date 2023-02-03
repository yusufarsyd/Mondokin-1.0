package com.ngodesain.mondokin.ui.activity

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.databinding.ActivityMapsBinding
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute

class MapsActivity : AppCompatActivity() {


    //mapbox
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapboxMap: MapboxMap

    companion object {
        private const val ICON_ID = "ICON_ID"
    }

    private lateinit var symbolManager: SymbolManager
    private lateinit var locationComponent: LocationComponent
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var navigationMapRoute: NavigationMapRoute
    private var currentRoute: DirectionsRoute? = null

    //menampung variabel global
    private lateinit var latitudeTujuan: String
    private lateinit var longtitudeTujuan: String
    private lateinit var namaPondokTujuan: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get data dari detail activity
        namaPondokTujuan = intent.getStringExtra("nama_pondok_tujuan").toString()
        latitudeTujuan = intent.getStringExtra("latitude_tujuan").toString()
        longtitudeTujuan = intent.getStringExtra("longtitude_tujuan").toString()

        //peta mapbox
        binding.mapViewDetail.onCreate(savedInstanceState)
        binding.mapViewDetail.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                //menampilkan titik tujuan
                symbolManager = SymbolManager(binding.mapViewDetail, mapboxMap, style)
                symbolManager.iconAllowOverlap = true
                style.addImage(
                    ICON_ID,
                    BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default)
                )

                navigationMapRoute = NavigationMapRoute(
                    null,
                    binding.mapViewDetail,
                    mapboxMap,
                    R.style.NavigationMapRoute
                )

                tujuan()
                showMyLocation(style)
                showNavigation()
            }
        }


    }


    private fun getRute(data: LatLng) {
        val destination = Point.fromLngLat(longtitudeTujuan.toDouble(), latitudeTujuan.toDouble())
        val origin = Point.fromLngLat(data.longitude, data.latitude)
        requestRoute(origin, destination)
    }

    private fun requestRoute(origin: Point, destination: Point) {
        navigationMapRoute.updateRouteVisibilityTo(false)
        NavigationRoute.builder(this)
            .accessToken(getString(R.string.mapbox_access_token))
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object : retrofit2.Callback<DirectionsResponse> {
                override fun onResponse(
                    call: retrofit2.Call<DirectionsResponse>,
                    response: retrofit2.Response<DirectionsResponse>
                ) {
                    if (response.body() == null) {
                        Toast.makeText(
                            baseContext,
                            "No routes found, make sure you set the right user and access token.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    } else if (response.body()?.routes()?.size == 0) {
                        Toast.makeText(baseContext, "No routes found.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    currentRoute = response.body()?.routes()?.get(0)

                    navigationMapRoute.addRoute(currentRoute)

                }

                override fun onFailure(call: retrofit2.Call<DirectionsResponse>, t: Throwable) {
                    Toast.makeText(baseContext, "Error : $t", Toast.LENGTH_SHORT).show()
                }
            })

    }

    @SuppressLint("MissingPermission")
    private fun showMyLocation(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponentOptions = LocationComponentOptions.builder(this)
                .pulseEnabled(true)
                .pulseColor(Color.BLUE)
                .pulseAlpha(.4f)
                .pulseInterpolator(BounceInterpolator())
                .build()
            val locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(this, style)
                .locationComponentOptions(locationComponentOptions)
                .build()
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(locationComponentActivationOptions)
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            val mylocation = locationComponent.lastKnownLocation?.latitude?.let { locationComponent.lastKnownLocation?.longitude?.let { it1 -> LatLng(it, it1) } }
            mylocation?.let { CameraUpdateFactory.newLatLngZoom(it, 12.0) }?.let { mapboxMap.animateCamera(it) }
            if (mylocation != null) {
                getRute(mylocation)
            }

        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
                    Toast.makeText(baseContext, "Anda harus mengizinkan location permission untuk menggunakan aplikasi ini", Toast.LENGTH_SHORT).show()
                }
                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        mapboxMap.getStyle { style ->
                            showMyLocation(style)
                        }
                    } else {
                        finish()
                    }
                }
            })
            permissionsManager.requestLocationPermissions(this)
        }

    }

    private fun showNavigation() {
        binding.btnNavigation.visibility = View.VISIBLE
        binding.btnNavigation.setOnClickListener {
            val simulateRoute = true

            val options = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(simulateRoute)
                .build()

            NavigationLauncher.startNavigation(this, options)
        }
    }

    private fun tujuan() {
        val tujuanPondok = LatLng(latitudeTujuan.toDouble(), longtitudeTujuan.toDouble())
        symbolManager.create(
            SymbolOptions()
                .withLatLng(LatLng(tujuanPondok.latitude, tujuanPondok.longitude))
                .withIconImage(ICON_ID)
                .withIconSize(1.5f)
                .withIconOffset(arrayOf(0f, -1.5f))
                .withTextField(namaPondokTujuan)
                .withTextHaloColor("rgba(255, 255, 255, 100)")
                .withTextHaloWidth(5.0f)
                .withTextAnchor("top")
                .withTextOffset(arrayOf(0f, 1.5f))
                .withDraggable(true)
        )
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tujuanPondok, 8.0))
    }


    override fun onStart() {
        super.onStart()
        binding.mapViewDetail.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewDetail.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapViewDetail.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapViewDetail.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapViewDetail.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapViewDetail.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewDetail.onLowMemory()
    }


}