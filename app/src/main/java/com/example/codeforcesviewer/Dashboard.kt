package com.example.codeforcesviewer

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.codeforcesviewer.UserData.UserContests
import com.example.codeforcesviewer.UserData.UserPublicData
import com.example.codeforcesviewer.databinding.ActivityDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


class Dashboard : AppCompatActivity() {
    lateinit var binding: ActivityDashboardBinding
    private lateinit var colors : Map<String, Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        colors = mapOf(
                resources.getString(R.string.Newbie) to R.color.Newbie,
                resources.getString(R.string.Pupil) to R.color.Pupil,
                resources.getString(R.string.Specialist) to R.color.Specialist,
                resources.getString(R.string.Expert) to R.color.Expert,
                resources.getString(R.string.CandidateMaster) to R.color.CandidateMaster,
                resources.getString(R.string.Master) to R.color.Master,
                resources.getString(R.string.InternationalMaster) to R.color.InternationalMaster,
                resources.getString(R.string.Grandmaster) to R.color.GrandMaster,
                resources.getString(R.string.InternationalGrandmaster) to R.color.InternationalGrandmaster,
                resources.getString(R.string.LegendaryGrandmaster) to R.color.LegendaryGrandmaster
        )
        val handle : String? = intent.getStringExtra("handle")
        Log.d("Dashboard", "Handle Received: $handle")
        if(handle == null){
            Log.d("Dashboard", "No Handle received here")
        }else{
            getData(handle)
        }
    }
    private fun getData(handle: String){
        val publicData : Call<UserPublicData> = FetchData.instance.getUserData(handle)
        publicData.enqueue(object : Callback<UserPublicData> {
            override fun onResponse(call: Call<UserPublicData>, response: Response<UserPublicData>) {
                Log.d("Dashboard", "Data Received: ${response.body()}")
                Log.d("Dashboard", "${response.code()}")
                val userData = response.body()
                if (userData != null) {
                    updateUI(userData)
                    showRanks()
                    getAllUsersData(handle, userData.result.get(0).country)
                    updateGraph(handle)
                } else {
                    Log.d("Dashboard", "Null received in User Data ${response.code()}")
                    Toast.makeText(applicationContext, "Null received in User Data ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserPublicData>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in API User Data ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun showRanks(){
        binding.publicDataId.WorldRank.visibility = View.VISIBLE
        binding.publicDataId.CountryRank.visibility = View.VISIBLE
        binding.publicDataId.progressBar.visibility = View.VISIBLE
        binding.publicDataId.progressBar2.visibility = View.VISIBLE
    }
    private fun updateUI(userPublicData: UserPublicData){
        val result = userPublicData.result.get(0)
        updateImage(result.titlePhoto)
        showQuestions()
        updateName(result.firstName, result.lastName)
        updateRating(result.rating, result.maxRating)
        updateTitle(result.handle, result.rank, result.maxRank)
        updateCityCountry(result.city, result.country)
        updateContribution(result.contribution)
        updateOrganization(result.organization)
        updateFriends(result.friendOfCount)
        if(result.rank != null && result.maxRank != null)
            updateColor(result.rank, result.maxRank)
        updateRegisteredOnline(result.registrationTimeSeconds, result.lastOnlineTimeSeconds)
        checkOnline(result.lastOnlineTimeSeconds)
    }
    private fun showQuestions(){
        binding.publicDataId.NameQuestion.visibility = View.VISIBLE
        binding.publicDataId.CurrentRatingQuestion.visibility = View.VISIBLE
        binding.publicDataId.OrganizationQuestion.visibility = View.VISIBLE
        binding.publicDataId.CityCountryQuestion.visibility = View.VISIBLE
        binding.publicDataId.FriendOfQuestion.visibility = View.VISIBLE
        binding.publicDataId.ContributionQuestion.visibility = View.VISIBLE
        binding.publicDataId.RegisteredQuestion.visibility = View.VISIBLE
        binding.publicDataId.MaxRankQuestion.visibility = View.VISIBLE

    }
    private fun updateOrganization(organization: String?){
        binding.publicDataId.OrganizationAnswer.text = organization?: "NA"

    }
    private fun updateName(first: String?, last: String?){
        val name: String = if(first != null) "$first ${last ?: "NA"}" else last ?: "NA"
        binding.publicDataId.NameAnswer.text = name
    }
    private fun updateRating(current: Int?, maximum: Int?){
        binding.publicDataId.CurrentRatingAnswer.text = "${current ?: 0} (max. ${maximum ?: 0})"

    }
    private fun updateTitle(handle: String, rank: String?, max_rank: String?){
        binding.publicDataId.titleTextView1.text = handle
        binding.publicDataId.titleTextView2.text = getCapitalized((rank ?: ""))
        binding.publicDataId.MaxRankAnswer.text = getCapitalized((max_rank ?: "NA"))
    }
    private fun updateCityCountry(city: String?, country: String?){
        val cityCountry: String = if(city != null) "$city, ${country ?: "NA"}" else country ?: "NA"
        binding.publicDataId.CityCountryAnswer.text = cityCountry
    }
    private fun updateContribution(contribution: Int?){
        if(contribution != null){
            binding.publicDataId.ContributionAnswer.text = "$contribution"
            if(contribution > 0){
                colors["pupil"]?.let {
                    binding.publicDataId.ContributionAnswer.setTextColor(resources.getColor(it))
                    binding.publicDataId.ContributionAnswer.text = "+$contribution"
                }
            }
        }else{
            binding.publicDataId.ContributionAnswer.text = "NA"
        }
    }
    private fun updateFriends(friends: Int?){
        if(friends != null){
            binding.publicDataId.FriendOfAnswer.text = "$friends users"
        }else{
            binding.publicDataId.FriendOfAnswer.text = "NA"
        }
    }
    private fun getDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }
    private fun getMonth(number : Int) : String?{
        val months = mapOf<Int, String>(
                0 to "January", 1 to "February", 2 to "March", 3 to "April", 4 to "May", 5 to "June",
                6 to "July", 7 to "August", 8 to "September", 9 to "October", 10 to "November", 11 to "December",
        )
        return months[number]
    }

    private fun updateRegisteredOnline(time1: Long, time2: Long){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            val days = seconds / 3600 / 24
            val date = getDaysAgo(days.toInt())
            Log.d("Dashboard", "$timeNow $timePrev $seconds $days")
            Log.d("Dashboard", "$date")
            binding.publicDataId.RegisteredAnswer.text = getMonth(date.month) + " " + date.date + ", " + (date.year + 1900)
        } else {
            binding.publicDataId.RegisteredAnswer.text = "No Info!"
        }
    }

    private fun checkOnline(time1: Long){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            Log.d("Dashboard", "Seconds passed: $seconds")
            if(seconds <= 5 * 3600){ // online in the previous
                binding.publicDataId.onlineIndicator.visibility = View.VISIBLE
            }else{
                binding.publicDataId.onlineIndicator.visibility = View.INVISIBLE
            }
            return
        }
        binding.publicDataId.onlineIndicator.visibility = View.INVISIBLE
    }
    private fun updateImage(url: String){
        val downloader = DownloadImageTask(binding.publicDataId.profilePhotoImageView)
        downloader.execute("https:$url")
    }
    private fun updateColor(rank: String, max_rank: String) {
        Log.d("Dashboard", rank)
        colors[rank]?.let {
            binding.publicDataId.titleTextView2.setTextColor(resources.getColor(it))
            binding.publicDataId.profilePhotoImageView.borderColor = resources.getColor(it)
        }
        colors[max_rank]?.let{
            binding.publicDataId.MaxRankAnswer.setTextColor(resources.getColor(it))
        }
    }
    private fun getCapitalized(s: String) : String{
        val a = s.split(" ").map { it.capitalize() }
        var string = ""
        for(i in a){
            string += "$i "
        }
        string.trim()
        return string
    }
    class DownloadImageTask(private val bmImage: ImageView) :
            AsyncTask<String?, Void?, Bitmap?>() {
        override fun doInBackground(vararg params: String?): Bitmap? {
            val urldisplay = params[0]
            var mIcon11: Bitmap? = null
            try {
                val `in`: InputStream = URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Dashboard", "Error in Image Download + ${e.message!!}")
                e.printStackTrace()
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            bmImage.setImageBitmap(result)
        }
    }

    private fun getAllUsersData(handle : String, country : String){
        val publicData : Call<UserPublicData> = FetchData.instance.getAllUsers()
        Log.d("Dashboard", "Getting All users now")
        publicData.enqueue(object : Callback<UserPublicData> {
            override fun onResponse(call: Call<UserPublicData>, response: Response<UserPublicData>) {
                Log.d("Dashboard", "${response.code()}")
                val allUsers = response.body()
                if(allUsers != null){
                    var worldRank = 1
                    var countryRank = 1
                    val totalWorld = allUsers.result.size
                    var totalInCountry = 1
                    for(result in allUsers.result){
                        if(result.handle == handle)
                            break
                        if(result.country == country)
                            countryRank++
                        worldRank++
                    }
                    if(worldRank > allUsers.result.size){
                        worldRank = -1
                    }
                    for(result in allUsers.result){
                        if(result.country == country)
                        totalInCountry++
                    }
                    updateRanks(worldRank, totalWorld, if(country != null) countryRank else -1, totalInCountry)
                }else{
                    Log.d("Dashboard", "Received null in Rank API ${response.code()}")
                    Toast.makeText(applicationContext, "Null Received in Rank API: ${response.code()}", Toast.LENGTH_LONG).show()
                    updateRanks(-1, -1, -1, -1)
                }
            }

            override fun onFailure(call: Call<UserPublicData>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Log.d("Dashboard", "Error in Rank API ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in API call Rank API: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                updateRanks(-1, -1, -1, -1)
            }
        })
    }
    private fun updateRanks(Wr : Int, totalW : Int, Cr : Int, totalC : Int){
        binding.publicDataId.progressBar.visibility = View.INVISIBLE
        binding.publicDataId.progressBar2.visibility = View.INVISIBLE
        if(Wr != -1){
            binding.publicDataId.WorldRankAnswer.text = "$Wr\n($totalW)"
        }else{
            binding.publicDataId.WorldRankAnswer.text = "NA"
        }
        if(Cr == -1){
            binding.publicDataId.CountryRankAnswer.text = "NA"
        }else{
            binding.publicDataId.CountryRankAnswer.text = "$Cr\n($totalC)"
        }
        binding.publicDataId.WorldRankAnswer.visibility = View.VISIBLE
        binding.publicDataId.CountryRankAnswer.visibility = View.VISIBLE
    }
    private fun updateGraph(handle : String){
        val ratings = ArrayList<Entry>()
        val circleColors = ArrayList<Int>()
        val contestData : Call<UserContests> = FetchData.instance.getUserRatedContests(handle)
        contestData.enqueue(object : Callback<UserContests> {
            override fun onResponse(call: Call<UserContests>, response: Response<UserContests>) {
                Log.d("Dashboard", "Contest Data ${response.code()}")
                val dataRetured = response.body()
                if(dataRetured != null){
                    var max_here = -2000000
                    var min_here = 2000000
                    var max_limit : Long = 0
                    val min_time = if (dataRetured.result.isNotEmpty()) dataRetured.result[0].ratingUpdateTimeSeconds else 0
                    for(contest in dataRetured.result){
                        Log.d("Dashboard", "Contest Next ${contest.toString()}")
                        max_here = max(max_here, contest.newRating)
                        min_here = min(min_here, contest.newRating)
                        Log.d("Dashboard", "Adding point ${contest.newRating} ${(contest.ratingUpdateTimeSeconds - min_time).toFloat() / 1000}")
                        ratings.add(Entry( (contest.ratingUpdateTimeSeconds - min_time).toFloat() / 1000, contest.newRating.toFloat()))
                    }
                    var count = 0
                    for(entry in ratings){
                        if(entry.y != max_here.toFloat() || count == 1){
                            circleColors.add(resources.getColor(R.color.ratingGraph))
                        }else{
                            circleColors.add(resources.getColor(R.color.maxRating))
                            count = 1
                        }
                    }
                    max_here = if(max_here != -2000000) max_here + 200 else 2000
                    min_here = if(min_here != 2000000) min(min_here - 200, 1200) else 1200
                    val dataSets = ArrayList<ILineDataSet>()
                    ratings.sortBy { it.x }
                    val lineDataSet = LineDataSet(ratings, handle)
                    lineDataSet.lineWidth = 2F
                    lineDataSet.setColor(resources.getColor(R.color.ratingGraph))
                    lineDataSet.setCircleColors(circleColors)
                    lineDataSet.setDrawValues(false)
                    dataSets.add(lineDataSet)
                    styleChart(max_here, min_here)
                    binding.userGraphId.RatingGraph.data = LineData(dataSets)
                    binding.userGraphId.RatingGraph.invalidate()
                }else{
                    Log.d("Dashboard", "Contest Data Received null here!!!")
                    Toast.makeText(applicationContext, "Null Received in API contests ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserContests>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Log.d("Dashboard", "Error in API contests ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in API contests ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })

    }
    private fun styleChart(max_here : Int, min_here : Int){
        Log.d("Dashboard", "$max_here $min_here YAxis constraints")
        binding.userGraphId.RatingGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.userGraphId.RatingGraph.axisLeft.setAxisMaxValue(max_here.toFloat())
        binding.userGraphId.RatingGraph.axisLeft.setAxisMinValue(min_here.toFloat())
        binding.userGraphId.RatingGraph.axisRight.setDrawLabels(false)
        binding.userGraphId.RatingGraph.description.isEnabled = false
        binding.userGraphId.RatingGraph.animateX(3000)
        binding.userGraphId.RatingGraph.axisRight.setDrawGridLines(false)
        binding.userGraphId.RatingGraph.setDrawBorders(true)
        binding.userGraphId.RatingGraph.xAxis.setDrawLabels(false)
        binding.userGraphId.RatingGraph.setBorderWidth(2f)
        binding.userGraphId.RatingGraph.visibility = View.VISIBLE
        binding.userGraphId.RatingGraph.legend.isEnabled = false
        val color = when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {ContextCompat.getColor(applicationContext, R.color.white)}
            Configuration.UI_MODE_NIGHT_NO -> {ContextCompat.getColor(applicationContext, R.color.black)}
            else -> {ContextCompat.getColor(applicationContext, R.color.black)}
        }
        binding.userGraphId.RatingGraph.axisLeft.textColor = color
        binding.userGraphId.RatingGraph.setPinchZoom(false)
        binding.userGraphId.RatingGraph.setScaleEnabled(false)
        binding.userGraphId.RatingGraph.isHighlightPerTapEnabled = false
        binding.userGraphId.RatingGraph.isHighlightPerDragEnabled = false
    }
}
