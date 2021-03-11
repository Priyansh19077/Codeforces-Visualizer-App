package com.example.codeforcesviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codeforcesviewer.databinding.ActivityDashboardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.util.*


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
                resources.getString(R.string.InternationalGrandmaster) to R.color.InternationGrandmaster,
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
        val data : Call<UserData> = FetchData.instance.getUserData(handle)
        data.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                Log.d("Dashboard", "Data Received: ${response.body()}")
                Log.d("Dashboard", "${response.code()}")
                val userData = response.body()
                if (userData != null) {
                    updateUI(userData)
                    showRanks()
                    getAllUsersData(handle, userData.result.get(0).country)
                } else {
                    Toast.makeText(applicationContext, "${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Toast.makeText(applicationContext, t.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }
    private fun showRanks(){
        binding.WorldRank.visibility = View.VISIBLE
        binding.CountryRank.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar2.visibility = View.VISIBLE
    }
    private fun updateUI(userData: UserData){
        val result = userData.result.get(0)
        updateImage(result.titlePhoto)
        showQuestions()
        updateName(result.firstName, result.lastName)
        updateRating(result.rating, result.maxRating)
        updateTitle(result.handle, result.rank, result.maxRank)
        updateCityCountry(result.city, result.country)
        updateContribution(result.contribution)
        updateOrganization(result.organization)
        updateFriends(result.friendOfCount)
        updateColor(result.rank, result.maxRank)
        updateRegisteredOnline(result.registrationTimeSeconds, result.lastOnlineTimeSeconds)
        checkOnline(result.lastOnlineTimeSeconds)
    }
    private fun showQuestions(){
        binding.NameQuestion.visibility = View.VISIBLE
        binding.CurrentRatingQuestion.visibility = View.VISIBLE
        binding.OrganizationQuestion.visibility = View.VISIBLE
        binding.CityCountryQuestion.visibility = View.VISIBLE
        binding.FriendOfQuestion.visibility = View.VISIBLE
        binding.ContributionQuestion.visibility = View.VISIBLE
        binding.RegisteredQuestion.visibility = View.VISIBLE
        binding.MaxRankQuestion.visibility = View.VISIBLE

    }
    private fun updateOrganization(organization: String?){
        binding.OrganizationAnswer.text = organization?:"NA"

    }
    private fun updateName(first: String?, last: String?){
        val name: String = if(first != null) "$first ${last ?: "NA"}" else last ?: "NA"
        binding.NameAnswer.text = name
    }
    private fun updateRating(current: Int?, maximum: Int?){
        binding.CurrentRatingAnswer.text = "${current ?: 0} (max. ${maximum ?: 0})"

    }
    private fun updateTitle(handle: String, rank: String?, max_rank: String?){
        binding.titleTextView1.text = handle
        binding.titleTextView2.text = getCapitalized((rank ?: ""))
        binding.MaxRankAnswer.text = getCapitalized((max_rank ?: ""))
    }
    private fun updateCityCountry(city: String?, country: String?){
        val cityCountry: String = if(city != null) "$city, ${country ?: "NA"}" else country ?: "NA"
        binding.CityCountryAnswer.text = cityCountry
    }
    private fun updateContribution(contribution: Int?){
        if(contribution != null){
            binding.ContributionAnswer.text = "$contribution"
            if(contribution > 0){
                colors["pupil"]?.let {
                    binding.ContributionAnswer.setTextColor(resources.getColor(it))
                    binding.ContributionAnswer.text = "+$contribution"
                }
            }
        }else{
            binding.ContributionAnswer.text = "NA"
        }
    }
    private fun updateFriends(friends: Int?){
        if(friends != null){
            binding.FriendOfAnswer.text = "$friends users"
        }else{
            binding.FriendOfAnswer.text = "NA"
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
            val timePrev: Long = time1.toLong()
            val seconds = timeNow - timePrev
            val days = seconds / 3600 / 24
            val date = getDaysAgo(days.toInt())
            Log.d("Dashboard", "$timeNow $timePrev $seconds $days")
            Log.d("Dashboard", "$date")
            binding.RegisteredAnswer.text = getMonth(date.month) + " " + date.date + ", " + (date.year + 1900)
        } else {
            binding.RegisteredAnswer.text = "No Info!"
        }
    }

    private fun checkOnline(time1: Long){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            Log.d("Dashboard", "Seconds passed: $seconds")
            if(seconds <= 5 * 3600){ // online in the previous
                binding.onlineIndicator.visibility = View.VISIBLE
            }else{
                binding.onlineIndicator.visibility = View.INVISIBLE
            }
            return
        }
        binding.onlineIndicator.visibility = View.INVISIBLE
    }
    private fun updateImage(url: String){
        val downloader = DownloadImageTask(binding.profilePhotoImageView)
        downloader.execute("https:$url")
    }
    private fun updateColor(rank: String, max_rank: String) {
        Log.d("Dashboard", rank)
        colors[rank]?.let {
            binding.titleTextView2.setTextColor(resources.getColor(it))
            binding.profilePhotoImageView.borderColor = resources.getColor(it)
        }
        colors[max_rank]?.let{
            binding.MaxRankAnswer.setTextColor(resources.getColor(it))
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

    private fun getAllUsersData(handle : String, country : String?){
        val data : Call<UserData> = FetchData.instance.getAllUsers()
        Log.d("Dashboard", "Getting All users now")
        data.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                Log.d("Dashboard", "${response.code()}")
                val allUsers = response.body()
                if(allUsers != null){
                    var worldRank : Int = 1
                    var countryRank : Int = 1
                    var totalWorld = allUsers.result.size
                    var totalInCountry = 1
                    for(result in allUsers.result){
                        if(result.handle == handle)
                            break
                        if(result.country == country)
                            countryRank++
                        worldRank++
                    }
                    for(result in allUsers.result){
                        if(result.country == country)
                        totalInCountry++
                    }
                    updateRanks(worldRank, totalWorld, if(country != null) countryRank else -1, totalInCountry)
                }else{
                    Log.d("Dashboard", "Received null here!!!")
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Log.d("Dashboard", "Error in API call ${t.localizedMessage}")
            }
        })
    }
    private fun updateRanks(Wr : Int, totalW : Int, Cr : Int, totalC : Int){
        binding.progressBar.visibility = View.INVISIBLE
        binding.progressBar2.visibility = View.INVISIBLE
        if(Wr != -1){
            binding.WorldRankAnswer.text = "$Wr\n($totalW)"
        }else{
            binding.WorldRankAnswer.text = "NA"
        }
        if(Cr == -1){
            binding.CountryRankAnswer.text = "NA"
        }else{
            binding.CountryRankAnswer.text = "$Cr\n($totalC)"
        }
        binding.WorldRankAnswer.visibility = View.VISIBLE
        binding.CountryRankAnswer.visibility = View.VISIBLE
    }
}
