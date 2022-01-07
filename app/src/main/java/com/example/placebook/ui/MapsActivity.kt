package com.example.placebook.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.placebook.R
import com.example.placebook.adapter.BookmarkInfoWindowAdapter
import com.example.placebook.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
//    private var locationRequest: LocationRequest? = null
    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupLocationClient()
        setupPlacesClient()
    }
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION
        )
    }
    private fun getCurrentLocation() {
// 1
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
// 2
            requestLocationPermissions()
        } else {
//            if (locationRequest == null) {
//                locationRequest = LocationRequest.create()
//                locationRequest?.let { locationRequest ->
//// 1
//                    locationRequest.priority =
//                        LocationRequest.PRIORITY_HIGH_ACCURACY
//// 2
//                    locationRequest.interval = 5000
//// 3
//                    locationRequest.fastestInterval = 1000
//// 4
//                    val locationCallback = object : LocationCallback() {
//                        override fun onLocationResult(locationResult: LocationResult?) {
//                            getCurrentLocation()
//                        }
//                    }
//// 5
//                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
//                }
//            }
            map.isMyLocationEnabled=true
// 3
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
// 4
                    val latLng = LatLng(location.latitude,
                        location.longitude)
// 5
//                    map.clear()
//                    map.addMarker(MarkerOptions().position(latLng).title("You are here!"))
// 6
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
// 7
                    map.moveCamera(update)
                } else {
// 8
                    Log.e(TAG, "No location found")
                }
            }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        getCurrentLocation()
        map.setOnPoiClickListener {
            displayPoi(it)
        }
    }
    private fun setupPlacesClient() {
        Places.initialize(applicationContext,
            getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }
    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }
    private fun displayPoiGetPhotoStep(place: Place) {
// 1
        val photoMetadata = place
            .getPhotoMetadatas()?.get(0)// 2
        if (photoMetadata == null) {
// Next step here
            displayPoiDisplayStep(place,null)
            return
        }
// 3
        val photoRequest = FetchPhotoRequest
            .builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(
                R.dimen.default_image_width
            ))
            .setMaxHeight(resources.getDimensionPixelSize(
                R.dimen.default_image_height
            ))
            .build()
// 4
        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
// Next step here
                displayPoiDisplayStep(place,bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode)
                }
            }
    }
    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        // 1
        val placeId = pointOfInterest.placeId
        // 2
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        // 3
        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()
        // 4
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
    // 5
                val place = response.place
                displayPoiGetPhotoStep(place)
//                Toast.makeText(
//                    this,
//                    "${place.name}, " +
//                            "${place.phoneNumber}", Toast.LENGTH_LONG
//                ).show()
            }.addOnFailureListener { exception ->
    // 6
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode
                    )
                }
            }
    }
    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?)
    {
        val iconPhoto = if (photo == null) {
            BitmapDescriptorFactory.defaultMarker()
        } else {
            BitmapDescriptorFactory.fromBitmap(photo)
        }
        val marker=map.addMarker(
            MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber)
        )
        marker?.tag=photo
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] ==PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}