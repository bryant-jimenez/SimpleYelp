package edu.stanford.bryantjimenez.simpleyelp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.net.NetworkInfo

import android.net.ConnectivityManager
import android.os.Build
import android.widget.Toast


private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "50MC4LViWS6lrAn3X-ojoH0YxE-D52u2QijJGduno3Ph8nfo3TrSp5Cuv_EZri55wv91zzw1YMdwHfrQz-QSF6HS6cMfUEzFX8CuXDhcR3r_FaGupiz9v1FSsYeUYXYx"


class MainActivity : AppCompatActivity() {
    private lateinit var rvRestaurants: RecyclerView

    private fun isNetworkAvailable(): Boolean? {
        val connectivityManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            } else {
                return false
            }
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNetworkAvailable() != true) {
            Toast.makeText(this,"Error: not connected to a network. Please connect and try again.",Toast.LENGTH_LONG).show();
        }

            val restaurants = mutableListOf<YelpRestaurant>()
            val adapter = RestaurantsAdapter(this, restaurants)
            rvRestaurants = findViewById(R.id.rvRestaurants)
            rvRestaurants.adapter = adapter
            rvRestaurants.layoutManager = LinearLayoutManager(this)

            val retrofit =
                Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).build()
            val yelpService = retrofit.create(YelpService::class.java)
            yelpService.searchRestaurants("Bearer $API_KEY", "Avocado Toast", "New York")
                .enqueue(object : Callback<YelpSearchResult> {
                    override fun onResponse(
                        call: Call<YelpSearchResult>,
                        response: Response<YelpSearchResult>
                    ) {
                        Log.i(TAG, "onResponse $response")
                        val body = response.body()
                        if (body == null) {
                            Log.w(TAG, "Did not receive valid response body from Yelp API")
                            return
                        }
                        restaurants.addAll(body.restaurants)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                        Log.i(TAG, "onFailure $t")
                    }

                })
            //
        }
    }