package com.example.ecomap


import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
/*import com.androdocs.weatherapp.WeatherActivity*/
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_maps.*
import com.example.ecomap.models.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val userMaps = generateSampleData()

    private lateinit var mMap: GoogleMap

    private var latitude:Double=0.toDouble()
    private var longitude:Double=0.toDouble()

    private lateinit var mLastLocation:Location
    private var mMarker: Marker?=null

    //маркер для категорий
    //private var categoryMarker: Marker? = null
    val mutableList = mutableListOf<Marker?>()

    //Location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkLocationPermission()) {
                buildLocationRequest();
                buildLocationCallBack();

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                );
            }
        }
        else{
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            );
        }

            bottom_navigation_view.setOnNavigationItemSelectedListener {item->
            when(item.itemId)
            {
                R.id.action_paper -> {
                    nearByPlace(0)
                    true
                }
                R.id.action_plastic ->
                {
                    nearByPlace(1)
                    true
                }
                R.id.action_glass -> {
                    nearByPlace(2)
                    true
                }
                R.id.action_danger ->
                {
                    nearByPlace(3)
                    true
                }
                /*R.id.action_help ->
                {
                    val intent = Intent(this,HelpActivity::class.java)

                    startActivity(intent)
                    true
                }*/
                R.id.action_weather ->
                {
                    val intent = Intent(this,WeatherActivity::class.java)

                    startActivity(intent)
                    true
                }
                else -> false
            }

        //Для нижнего меню
        //val bnv = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        }

    }

    /* onItemClick */
    private fun nearByPlace(typePlace: Int) {

        for(i in mutableList)
            i?.remove()
        mutableList.clear()

        val boundsBuilder = LatLngBounds.Builder()
        for (place in userMaps[typePlace].places)
        {
            val latLng = LatLng(place.latitude,place.longitude)
            boundsBuilder.include(latLng)
            //categoryMarker = mMap!!.addMarker(MarkerOptions().position(latLng).title(place.title).snippet(place.desription))
            mutableList.add(mMap!!.addMarker(MarkerOptions().position(latLng).title(place.title).snippet(place.desription)))
        }
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(),1000,1000,0))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
    }

    private fun generateSampleData(): List<UserMap>{
        return listOf(
            UserMap(
                "Бумага",
                listOf(
                    Place("Эко-ресурсы","Индустриальная ул., 4а, Смоленск",54.771380, 32.111411),
                    Place("Прием вторсырья","ул. Кашена, 8А, Смоленск",54.798179, 32.044829),
                    Place("Прием вторсырья","ул. Воробьева, 12, Смоленск",54.767708, 32.028210),
                    Place("ООО Технопарк-СМ","ул. Соболева, 102, Смоленск",54.799537, 32.091030),
                    Place("Прием вторсырья","ул. Шевченко, 76, Смоленск",54.775673, 32.077842),
                    Place("Прием вторсырья","ул. Рыленкова, 85, Смоленск",54.754875, 32.107142),
                    Place("Эко Лайн","ул. Соболева, 102, Смоленск",54.799457, 32.090620),
                    Place("Прием Макулатуры","ул. Свердлова, 22, Смоленск",54.800440, 32.059290),
                    Place("Пункт Приема Макулатуры","ул. Черняховского, 13, Смоленск",54.766369, 32.027321)
                )
            ),
            UserMap(
                "Пластик",
                listOf(
                    Place("Прием вторсырья","ул. Воробьева, 12, Смоленск",54.767708, 32.028210),
                    Place("ООО Технопарк-СМ","ул. Соболева, 102, Смоленск",54.799537, 32.091030),
                    Place("Прием вторсырья","ул. Багратиона, 10, Смоленск",54.780152, 32.023988),
                    Place("Прием вторсырья","ул. Рыленкова, 85, Смоленск",54.754875, 32.107142),
                    Place("Смоленский завод пластиковых изделий ZAVPLAST"," ул. Ново-Московская, д. 15, Смоленск",54.795670, 32.058521),
                    Place("Экотрейд-Смоленск","ул. Крупской, 68, Смоленск",54.759480, 32.066306)
                )
            ),
            UserMap(
                "Стекло",
                listOf(
                    Place("ООО Технопарк-СМ","ул. Соболева, 102, Смоленск",54.799537, 32.091030),
                    Place("Прием вторсырья","ул. Рыленкова, 85, Смоленск",54.754875, 32.107142),
                    Place("Прием вторсырья","ул. Кашена, 8А, Смоленск",54.798179, 32.044829),
                    Place("«Спецавтохозяйство» Ао","ул. Кирова, д. 29Г, Смоленск",54.770358, 32.040904)
                )
            ),

            UserMap(
                "Опасные отходы",
                listOf(
                    Place("Тюменские аккумуляторы","ул. Шевченко, 79, Смоленск",54.780073, 32.082624),
                    Place("Вывоз мусора","ул. Рыленкова, 23, Смоленск",54.763048, 32.097669),
                    Place("«Спецавтохозяйство» Ао","ул. Кирова, д. 29Г, Смоленск",54.770358, 32.040904),
                    Place("ЛЕДВАНС","г. Смоленск, ул. Индустриальная, 9А",54.781592, 32.106928),
                    Place("Артика","г. Смоленск, Нахимова, 21",54.779965, 32.012323),
                    Place("Экотрейд-Смоленск","ул. Крупской, 68, Смоленск",54.759480, 32.066306),
                    Place("БМЗ","г. Смоленск, Лавочкина, 104",54.813426, 31.985398)
                )
            )
        )
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                mLastLocation = p0!!.locations.get(p0!!.locations.size - 1) // Get last location

                if(mMarker != null)
                {
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title("Вы находитесь здесь")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap!!.addMarker(markerOptions)

                //Move Camera
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission():Boolean {

        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            return false
        }
        else
            return true
    }

    //Override OnRequestPermissionResult
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode)
        {
            MY_PERMISSION_CODE->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        if(checkLocationPermission()) {
                            buildLocationRequest();
                            buildLocationCallBack();

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            );
                            mMap!!.isMyLocationEnabled = true
                        }
                }
                else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        //Init Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
            }
        }
        else
            mMap!!.isMyLocationEnabled = true

        //Enable Zoom control
        mMap.uiSettings.isZoomControlsEnabled = true
    }
}