package com.affirm.takehome

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import com.affirm.takehome.adapter.RestaurantAdapter
import com.affirm.takehome.data.Restaurant
import com.affirm.takehome.mainViewModel.MainViewModel
import com.affirm.takehome.mainViewModelFactory.MainViewModelFactory
import com.affirm.takehome.network.yelp.YelpRestaurant
import com.affirm.takehome.network.yelp.YelpRestaurantApi
import com.affirm.takehome.network.yelp.YelpRestaurantApiFactory
import com.affirm.takehome.network.yelp.YelpRestaurantService
import com.affirm.takehome.network.zomato.ZomatoRestaurant
import com.affirm.takehome.network.zomato.ZomatoRestaurantApi
import com.affirm.takehome.network.zomato.ZomatoRestaurantApiFactory
import com.affirm.takehome.network.zomato.ZomatoRestaurantDetail
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Response
import kotlin.properties.Delegates.observable

private const val LOCATION_PERMISSION_CODE = 101
private const val THUMB_UP = R.drawable.thumb_up
private const val THUMB_DOWN = R.drawable.thumb_down
private const val TAG = "MainActivity"
lateinit var viewModel: MainViewModel

class MainActivity : AppCompatActivity() {
    private var yelpApi: YelpRestaurantApi = YelpRestaurantApiFactory.create()
    private var zomatoApi: ZomatoRestaurantApi = ZomatoRestaurantApiFactory.create()
    lateinit var loc: Location
    private var isYelp = false


    private var animating = false

    private val restaurantAdapter by lazy {
        RestaurantAdapter()
    }

    private var yesCounter: Int by observable(0) { _, _, newValue ->
        yesCounterText.text = newValue.toString()
    }

    private var noCounter: Int by observable(0) { _, _, newValue ->
        noCounterText.text = newValue.toString()
    }

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager.adapter = restaurantAdapter
        // Only allow button input, swiping not allowed
        viewPager.isUserInputEnabled = false

        var mainViewModelFactory: MainViewModelFactory = MainViewModelFactory()

        //create the viewmodel that will hold the data
        viewModel = ViewModelProvider(
            this,
            mainViewModelFactory
        ).get(MainViewModel::class.java)


        yesButton.setOnClickListener {
            // Make sure the previous animation finishes
            if (!animating) {
                yesCounter++
                viewPager.currentItem = viewPager.currentItem + 1
                animateIcon(THUMB_UP)
                if((viewModel.restaurantList.value?.size?.minus(viewPager.currentItem))!! == 3) {
                    CoroutineScope(Dispatchers.Default).launch {
                        getData()
                    }
                }
            }
        }

        noButton.setOnClickListener{
            if (!animating) {
                noCounter++
                viewPager.currentItem = viewPager.currentItem + 1
                animateIcon(THUMB_DOWN)
                if((viewModel.restaurantList.value?.size?.minus(viewPager.currentItem))!! == 3) {
                    CoroutineScope(Dispatchers.Default).launch {
                        getData()
                    }
                }
            }
        }

        yesCounterText.text = yesCounter.toString()
        noCounterText.text = noCounter.toString()

        checkAndRequestPermissionsForLocation()
    }

    private fun animateIcon(drawable: Int) {
        animating = true
        icon.setImageDrawable(ContextCompat.getDrawable(this, drawable))
        icon.alpha = 0.5f
        icon.visibility = View.VISIBLE
        icon.animate()
            .alpha(1f)
            .setDuration(300)
            .scaleX(2f)
            .scaleY(2f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    icon.visibility = View.GONE
                    animating = false
                }
            })
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                loadLocation()
            } else {
                Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndRequestPermissionsForLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            loadLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                // request the location
                Log.i("Here", "Here")

                fusedLocationProviderClient.requestLocationUpdates(
                    LocationRequest.create(),
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)

                            locationResult.locations.lastOrNull().let { location ->
                                if (location == null) {
                                    Log.d(TAG, "Location load fail")
                                    false
                                } else {
                                    loc = location
                                    var yelpResponse = yelpApi.getRestaurants(location.latitude, location.longitude, 0).execute()
                                    yelpResponse.body()?.restaurants?.let { yelpToRestaurant(it) }
                                    viewPager.adapter = restaurantAdapter

                                    true
                                }
                            }
                            fusedLocationProviderClient.removeLocationUpdates(this)
                        }
                    },
                    null
                )
            } else {
                loc = location
                runBlocking{
                    runOnUiThread {
                        viewPager.adapter = restaurantAdapter
                    }
                    withContext(Dispatchers.Default) {
                            var yelpResponse =
                                yelpApi.getRestaurants(location.latitude, location.longitude, 0)
                                    .execute()
                            yelpResponse.body()?.restaurants?.let { yelpToRestaurant(it) }

                    }
                    runOnUiThread {
                        viewPager.adapter?.notifyDataSetChanged()
                    }

                }
            }
        }
    }

    /**
     * Takes a list of YelpRetsaurant objects as returned from the Yelp API
     * and places them into the viewModel as Restaurant objects
     * so that the adapter will recognize them as new elements
     *
     * @param  list the list of YelpRestaurants
     * @return no return value
     */
    fun yelpToRestaurant(list: List<YelpRestaurant>) {
        var temp: MutableList<Restaurant> = mutableListOf()

        for(restaurant in list) {
            var rest = Restaurant(
                    restaurant.id,
                    restaurant.name,
                    restaurant.image,
                    restaurant.rating
            )

            viewModel.restaurantList.value?.add(rest)
            temp.add(rest)
        }
        runOnUiThread {
            restaurantAdapter.addRestaurants(temp)
        }
    }

    /**
     * Takes a list of ZomatoRetsaurant objects as returned from the Zomato API
     * and places them into the viewModel as Restaurant objects
     * so that the adapter will recognize them as new elements
     *
     * @param  list the list of ZomatoRestaurants
     * @return no return value
     */
    fun zomatoToRestaurant(list: List<ZomatoRestaurant>) {
        var temp: MutableList<Restaurant> = mutableListOf()

        for(restaurant in list) {
                var rest = Restaurant(
                    restaurant.restaurantDetail.id,
                    restaurant.restaurantDetail.name,
                    restaurant.restaurantDetail.image,
                    restaurant.restaurantDetail.userRating.toString()
            )
            viewModel.restaurantList.value?.add(rest)
            temp.add(rest)
        }
        runOnUiThread {
            restaurantAdapter.addRestaurants(temp)
        }
    }


    /**
     * when there are less than 3 remaining restaurant left to be shown this method will
     * be called. It will first check the isYelp boolean variable to see if data needs to be
     * pulled from Yelp, while if that variable is false data will be pulled from Zomato
     * @param no params
     * @return no return value
     */
    fun getData(){
        Log.i("fuck", isYelp.toString())
        if (isYelp) {//check if we are pulling from Yelp
            runBlocking{
                withContext(Dispatchers.Default) {
                    var yelpResponse =
                        yelpApi.getRestaurants(loc.latitude, loc.longitude, 0)
                            .execute()
                    yelpResponse.body()?.restaurants?.let { yelpToRestaurant(it) }
                }
            }
            //this data was pulled from Yelp so we must change isYelp to false so the next data comes from Zomato
            runOnUiThread {
                (viewPager.adapter as RestaurantAdapter).notifyDataSetChanged()
            }
        } else {
            runBlocking{
                withContext(Dispatchers.Default) {
                    var zomatoResponse =
                        zomatoApi.getRestaurants(loc.latitude, loc.longitude, 0)
                            .execute()
                    zomatoResponse.body()?.restaurants?.let { zomatoToRestaurant(it) }
                }
            }
            //this data was pulled from Zomato so we must change isYelp to true so the next data comes from Yelp
            runOnUiThread {
                (viewPager.adapter as RestaurantAdapter).notifyDataSetChanged()
            }
        }
        isYelp = !isYelp
    }
}