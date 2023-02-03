package com.ngodesain.mondokin.ui.terdekat

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Toast
import com.ngodesain.mondokin.R
import com.ngodesain.mondokin.databinding.ActivityMapsTerdekatBinding
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

class MapsTerdekatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsTerdekatBinding
    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationComponent: LocationComponent
    private lateinit var mylocation: LatLng
    private lateinit var permissionsManager: PermissionsManager

    //rute
    private lateinit var navigationMapRoute: NavigationMapRoute
    private var currentRoute: DirectionsRoute? = null

    companion object {
        private const val ICON_ID = "ICON_ID"
    }

    private lateinit var symbolManager: SymbolManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
        binding = ActivityMapsTerdekatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                symbolManager = SymbolManager(binding.mapView, mapboxMap, style)
                symbolManager.iconAllowOverlap = true
                style.addImage(
                    MapsTerdekatActivity.ICON_ID,
                    BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default)
                )

                navigationMapRoute = NavigationMapRoute(
                    null,
                    binding.mapView,
                    mapboxMap,
                    R.style.NavigationMapRoute
                )

                showTujuan()
                showMyLocation(style)
            }
        }

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
            mylocation = LatLng(locationComponent.lastKnownLocation?.latitude as Double, locationComponent.lastKnownLocation?.longitude as Double)
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 12.0))

            val latitude = intent.getStringExtra("latitudeTerdekat")
            val longitude = intent.getStringExtra("longitudeTerdekat")
            val destination = longitude?.let { latitude?.let { it1 -> Point.fromLngLat(it.toDouble(), it1.toDouble()) } }
            val origin = Point.fromLngLat(mylocation.longitude, mylocation.latitude)
            if (destination != null) {
                requestRoute(origin, destination)
            }
            binding.btnNavigation.visibility = View.VISIBLE
            binding.btnNavigation.setOnClickListener {
                val simulateRoute = true

                val options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRoute)
                    .shouldSimulateRoute(simulateRoute)
                    .build()

                NavigationLauncher.startNavigation(this, options)
            }



        } else {
            permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
                    Toast.makeText(this@MapsTerdekatActivity, "Anda harus mengizinkan location permission untuk menggunakan aplikasi ini", Toast.LENGTH_SHORT).show()
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


    private fun showTujuan() {

        val latitude = intent.getStringExtra("latitudeTerdekat")
        val longitude = intent.getStringExtra("longitudeTerdekat")
        val nama = intent.getStringExtra("namaTerdekat")


        val tujuanPondok = latitude?.let { longitude?.let { it1 -> LatLng(it.toDouble(), it1.toDouble()) } }
        if (tujuanPondok != null) {
            symbolManager.create(
                SymbolOptions()
                    .withLatLng(LatLng(tujuanPondok.latitude, tujuanPondok.longitude))
                    .withIconImage(MapsTerdekatActivity.ICON_ID)
                    .withIconSize(1.5f)
                    .withIconOffset(arrayOf(0f, -1.5f))
                    .withTextField("$nama")
                    .withTextHaloColor("rgba(255, 255, 255, 100)")
                    .withTextHaloWidth(5.0f)
                    .withTextAnchor("top")
                    .withTextOffset(arrayOf(0f, 1.5f))
                    .withDraggable(true)
            )
        }
        tujuanPondok?.let { CameraUpdateFactory.newLatLngZoom(it, 7.0) }?.let { mapboxMap.moveCamera(it) }




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
                            this@MapsTerdekatActivity,
                            "No routes found, make sure you set the right user and access token.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    } else if (response.body()?.routes()?.size == 0) {
                        Toast.makeText(this@MapsTerdekatActivity, "No routes found.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    currentRoute = response.body()?.routes()?.get(0)

                    navigationMapRoute.addRoute(currentRoute)
                }

                override fun onFailure(call: retrofit2.Call<DirectionsResponse>, t: Throwable) {
                    Toast.makeText(this@MapsTerdekatActivity, "Error : $t", Toast.LENGTH_SHORT).show()
                }
            })


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

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}