package com.example.codeforcesviewer

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforcesviewer.UserData.ContestData.ContestAdapter
import com.example.codeforcesviewer.UserData.ContestData.ContestDataToShow
import com.example.codeforcesviewer.UserData.ContestData.UserContests
import com.example.codeforcesviewer.UserData.SubmissionData.Problem
import com.example.codeforcesviewer.UserData.SubmissionData.UserSubmissions
import com.example.codeforcesviewer.UserData.UserInfo.UserPublicData
import com.example.codeforcesviewer.databinding.ActivityDashboardBinding
import com.example.codeforcesviewer.databinding.UserGraphBinding
import com.example.codeforcesviewer.databinding.UserPublicDataBinding
import com.example.codeforcesviewer.databinding.UserSolvedRatingsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import java.time.Instant
import java.util.*
import kotlin.collections.*
import kotlin.math.max
import kotlin.math.min


class Dashboard : Activity() {
    lateinit var binding: ActivityDashboardBinding
    lateinit var publicDataBinding: UserPublicDataBinding
    lateinit var userGraphBinding: UserGraphBinding
    lateinit var userSolvedRatingsBinding: UserSolvedRatingsBinding
    private lateinit var colors: Map<String, Int>
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
        publicDataBinding = binding.publicDataId
        userGraphBinding = binding.userGraphId
        userSolvedRatingsBinding = binding.userSolvedRatingId
        val handle: String? = intent.getStringExtra("handle")
        Log.d("Dashboard", "Handle Received: $handle")
        if (handle == null) {
            Log.d("Dashboard", "No Handle received here")
        } else {
            getData(handle)
            updateGraph(handle)
            updateRatingSolved(handle)
        }
    }

    private fun getData(handle: String) {
        val publicData: Call<UserPublicData> = FetchData.instance.getUserData(handle)
        publicData.enqueue(object : Callback<UserPublicData> {
            override fun onResponse(call: Call<UserPublicData>, response: Response<UserPublicData>) {
                Log.d("Dashboard", "Data Received: ${response.body()}")
                Log.d("Dashboard", "${response.code()}")
                val userData = response.body()
                if (userData != null) {
                    updateUI(userData)
                    showRanks()
                    getAllUsersData(handle, userData.result.get(0).country)
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

    private fun showRanks() {
        publicDataBinding.WorldRank.visibility = VISIBLE
        publicDataBinding.CountryRank.visibility = VISIBLE
        publicDataBinding.progressBar.visibility = VISIBLE
        publicDataBinding.progressBar2.visibility = VISIBLE
    }

    private fun updateUI(userPublicData: UserPublicData) {
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
        if (result.rank != null && result.maxRank != null)
            updateColor(result.rank, result.maxRank)
        updateRegisteredOnline(result.registrationTimeSeconds, result.lastOnlineTimeSeconds)
        checkOnline(result.lastOnlineTimeSeconds)
    }

    private fun showQuestions() {
        binding.PublicData.visibility = VISIBLE
        publicDataBinding.NameQuestion.visibility = VISIBLE
        publicDataBinding.CurrentRatingQuestion.visibility = VISIBLE
        publicDataBinding.OrganizationQuestion.visibility = VISIBLE
        publicDataBinding.CityCountryQuestion.visibility = VISIBLE
        publicDataBinding.FriendOfQuestion.visibility = VISIBLE
        publicDataBinding.ContributionQuestion.visibility = VISIBLE
        publicDataBinding.RegisteredQuestion.visibility = VISIBLE
        publicDataBinding.MaxRankQuestion.visibility = VISIBLE

    }

    private fun updateOrganization(organization: String?) {
        publicDataBinding.OrganizationAnswer.text = organization ?: "NA"

    }

    private fun updateName(first: String?, last: String?) {
        val name: String = if (first != null) "$first ${last ?: "NA"}" else last ?: "NA"
        publicDataBinding.NameAnswer.text = name
    }

    private fun updateRating(current: Int?, maximum: Int?) {
        publicDataBinding.CurrentRatingAnswer.text = "${current ?: 0} (max. ${maximum ?: 0})"

    }

    private fun updateTitle(handle: String, rank: String?, max_rank: String?) {
        publicDataBinding.titleTextView1.text = handle
        publicDataBinding.titleTextView2.text = getCapitalized((rank ?: ""))
        publicDataBinding.MaxRankAnswer.text = getCapitalized((max_rank ?: "NA"))
    }

    private fun updateCityCountry(city: String?, country: String?) {
        val cityCountry: String = if (city != null) "$city, ${country ?: "NA"}" else country ?: "NA"
        publicDataBinding.CityCountryAnswer.text = cityCountry
    }

    private fun updateContribution(contribution: Int?) {
        if (contribution != null) {
            publicDataBinding.ContributionAnswer.text = "$contribution"
            if (contribution > 0) {
                colors["pupil"]?.let {
                    publicDataBinding.ContributionAnswer.setTextColor(resources.getColor(it))
                    publicDataBinding.ContributionAnswer.text = "+$contribution"
                }
            }
        } else {
            publicDataBinding.ContributionAnswer.text = "NA"
        }
    }

    private fun updateFriends(friends: Int?) {
        if (friends != null) {
            publicDataBinding.FriendOfAnswer.text = "$friends users"
        } else {
            publicDataBinding.FriendOfAnswer.text = "NA"
        }
    }

    private fun getDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.time
    }

    private fun getMonth(number: Int): String? {
        val months = mapOf<Int, String>(
                0 to "January", 1 to "February", 2 to "March", 3 to "April", 4 to "May", 5 to "June",
                6 to "July", 7 to "August", 8 to "September", 9 to "October", 10 to "November", 11 to "December",
        )
        return months[number]
    }

    private fun updateRegisteredOnline(time1: Long, time2: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            val days = seconds / 3600 / 24
            val date = getDaysAgo(days.toInt())
            Log.d("Dashboard", "$timeNow $timePrev $seconds $days")
            Log.d("Dashboard", "$date")
            publicDataBinding.RegisteredAnswer.text = getMonth(date.month) + " " + date.date + ", " + (date.year + 1900)
        } else {
            publicDataBinding.RegisteredAnswer.text = "No Info!"
        }
    }

    private fun checkOnline(time1: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            Log.d("Dashboard", "Seconds passed: $seconds")
            if (seconds <= 5 * 3600) { // online in the previous
                publicDataBinding.onlineIndicator.visibility = VISIBLE
            } else {
                publicDataBinding.onlineIndicator.visibility = INVISIBLE
            }
            return
        }
        publicDataBinding.onlineIndicator.visibility = INVISIBLE
    }

    private fun updateImage(url: String) {
        val downloader = DownloadImageTask(publicDataBinding.profilePhotoImageView)
        downloader.execute("https:$url")
    }

    private fun updateColor(rank: String, max_rank: String) {
        Log.d("Dashboard", rank)
        colors[rank]?.let {
            publicDataBinding.titleTextView2.setTextColor(resources.getColor(it))
            publicDataBinding.profilePhotoImageView.borderColor = resources.getColor(it)
        }
        colors[max_rank]?.let {
            publicDataBinding.MaxRankAnswer.setTextColor(resources.getColor(it))
        }
    }

    private fun getCapitalized(s: String): String {
        val a = s.split(" ").map { it.capitalize() }
        var string = ""
        for (i in a) {
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

    private fun getAllUsersData(handle: String, country: String) {
        val publicData: Call<UserPublicData> = FetchData.instance.getAllUsers()
        Log.d("Dashboard", "Getting All users now")
        publicData.enqueue(object : Callback<UserPublicData> {
            override fun onResponse(call: Call<UserPublicData>, response: Response<UserPublicData>) {
                Log.d("Dashboard", "${response.code()}")
                val allUsers = response.body()
                if (allUsers != null) {
                    var worldRank = 1
                    var countryRank = 1
                    val totalWorld = allUsers.result.size
                    var totalInCountry = 1
                    for (result in allUsers.result) {
                        if (result.handle == handle)
                            break
                        if (result.country == country)
                            countryRank++
                        worldRank++
                    }
                    if (worldRank > allUsers.result.size) {
                        worldRank = -1
                    }
                    for (result in allUsers.result) {
                        if (result.country == country)
                            totalInCountry++
                    }
                    updateRanks(worldRank, totalWorld, if (country != null) countryRank else -1, totalInCountry)
                } else {
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

    private fun updateRanks(Wr: Int, totalW: Int, Cr: Int, totalC: Int) {
        publicDataBinding.progressBar.visibility = INVISIBLE
        publicDataBinding.progressBar2.visibility = INVISIBLE
        if (Wr != -1) {
            publicDataBinding.WorldRankAnswer.text = "$Wr\n($totalW)"
        } else {
            publicDataBinding.WorldRankAnswer.text = "NA"
        }
        if (Cr == -1) {
            publicDataBinding.CountryRankAnswer.text = "NA"
        } else {
            publicDataBinding.CountryRankAnswer.text = "$Cr\n($totalC)"
        }
        publicDataBinding.WorldRankAnswer.visibility = VISIBLE
        publicDataBinding.CountryRankAnswer.visibility = VISIBLE
    }

    private fun updateGraph(handle: String) {
        val ratings = ArrayList<Entry>()
        val circleColors = ArrayList<Int>()
        val contestData: Call<UserContests> = FetchData.instance.getUserRatedContests(handle)
        contestData.enqueue(object : Callback<UserContests> {
            override fun onResponse(call: Call<UserContests>, response: Response<UserContests>) {
                Log.d("Dashboard", "Contest Data ${response.code()}")
                val dataRetured = response.body()
                if (dataRetured != null) {
                    var max_here = -2000000
                    var min_here = 2000000
                    var max_limit: Long = 0
                    var position = 0
                    val newContestTitle = ContestDataToShow("S.No",
                            "Contest Name",
                            "Rank",
                            "Rating Change",
                            "New Rating",
                            -1,
                            -1)
                    val contestToShow = mutableListOf<ContestDataToShow>()
                    contestToShow.add(newContestTitle)
                    val min_time = if (dataRetured.result.isNotEmpty()) dataRetured.result[0].ratingUpdateTimeSeconds else 0
                    for (contest in dataRetured.result) {
                        Log.d("Dashboard", "Contest Next ${contest.toString()}")
                        max_here = max(max_here, contest.newRating)
                        min_here = min(min_here, contest.newRating)
                        Log.d("Dashboard", "Adding point ${contest.newRating} ${(contest.ratingUpdateTimeSeconds - min_time).toFloat() / 1000}")
                        ratings.add(Entry((contest.ratingUpdateTimeSeconds - min_time).toFloat() / 1000, contest.newRating.toFloat()))
                        val item = contest
                        position++
                        val newContest = ContestDataToShow(
                                (position).toString(),
                                item.contestName ?: "Unknown Contest",
                                if (item.rank != null) item.rank.toString() else "NA",
                                (if (item.newRating - item.oldRating > 0) "+" else "") + (item.newRating - item.oldRating).toString(),
                                item.newRating.toString(),
                                if (item.newRating - item.oldRating > 0) R.color.positiveChange else R.color.negativeChange,
                                getRatingColor(item.newRating))
                        contestToShow.add(newContest)
                    }
                    var count = 0
                    for (entry in ratings) {
                        if (entry.y != max_here.toFloat() || count == 1) {
                            circleColors.add(resources.getColor(R.color.ratingGraph))
                        } else {
                            circleColors.add(resources.getColor(R.color.maxRating))
                            count = 1
                        }
                    }
                    max_here = if (max_here != -2000000) max_here + 200 else 2000
                    min_here = if (min_here != 2000000) min(min_here - 200, 1200) else 1200
                    val dataSets = ArrayList<ILineDataSet>()
                    ratings.sortBy { it.x }
                    val lineDataSet = LineDataSet(ratings, handle)
                    lineDataSet.lineWidth = 2F
                    lineDataSet.setColor(resources.getColor(R.color.ratingGraph))
                    lineDataSet.setCircleColors(circleColors)
                    lineDataSet.circleHoleRadius = 1.6f
                    lineDataSet.circleHoleColor = resources.getColor(R.color.maxRating)
                    lineDataSet.setDrawValues(false)
                    dataSets.add(lineDataSet)
                    StyleRatingGraph(max_here, min_here)
                    userGraphBinding.RatingGraph.data = LineData(dataSets)
                    userGraphBinding.RatingGraph.invalidate()
                    userGraphBinding.contestDropDown.setOnClickListener {
                        val builder = AlertDialog.Builder(this@Dashboard)
                        val titleView = layoutInflater.inflate(R.layout.contest_heading, null)
                        builder.setCustomTitle(titleView)
                        val myDataset = dataRetured.result
                        val recyclerView = layoutInflater.inflate(R.layout.contest_recycler_view, null)
                        val recyclerViewView = recyclerView.findViewById<RecyclerView>(R.id.contest_recycler_view_view)
                        recyclerViewView.adapter = ContestAdapter(this@Dashboard, contestToShow)
                        recyclerViewView.setHasFixedSize(false)
                        builder.setCancelable(true)
                        builder.setView(recyclerView)
                        val dialog = builder.create()
                        dialog.show()
                    }
                } else {
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

    private fun StyleRatingGraph(max_here: Int, min_here: Int) {
        Log.d("Dashboard", "$max_here $min_here YAxis constraints")
        userGraphBinding.RatingGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        userGraphBinding.RatingGraph.axisLeft.setAxisMaxValue(max_here.toFloat())
        userGraphBinding.RatingGraph.axisLeft.setAxisMinValue(min_here.toFloat())
        userGraphBinding.RatingGraph.axisRight.setDrawLabels(false)
        userGraphBinding.RatingGraph.description.isEnabled = false
        userGraphBinding.RatingGraph.animateX(2000)
        userGraphBinding.RatingGraph.axisRight.setDrawGridLines(false)
        userGraphBinding.RatingGraph.setDrawBorders(true)
        userGraphBinding.RatingGraph.xAxis.setDrawLabels(false)
        userGraphBinding.RatingGraph.setBorderWidth(1.5f)
        binding.UserGraph.visibility = VISIBLE
        userGraphBinding.RatingGraph.visibility = VISIBLE
        userGraphBinding.RatingGraph.legend.isEnabled = false
        val color = when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ContextCompat.getColor(applicationContext, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ContextCompat.getColor(applicationContext, R.color.black)
            }
            else -> {
                ContextCompat.getColor(applicationContext, R.color.black)
            }
        }
        userGraphBinding.RatingGraph.axisLeft.textColor = color
        userGraphBinding.RatingGraph.setPinchZoom(false)
        userGraphBinding.RatingGraph.elevation = 10f
        userGraphBinding.RatingGraph.setScaleEnabled(false)
        userGraphBinding.RatingGraph.isHighlightPerTapEnabled = false
        userGraphBinding.RatingGraph.isHighlightPerDragEnabled = false
    }

    private fun getRatingColor(rating: Int): Int {
        if (rating < 1200)
            return R.color.Newbie
        if (rating < 1400)
            return R.color.Pupil
        if (rating < 1600)
            return R.color.Specialist
        if (rating < 1900)
            return R.color.Expert
        if (rating < 2100)
            return R.color.CandidateMaster
        if (rating < 2300)
            return R.color.Master
        if (rating < 2400)
            return R.color.InternationalMaster
        if (rating < 2700)
            return R.color.GrandMaster
        if (rating < 3000)
            return R.color.InternationalGrandmaster
        return R.color.LegendaryGrandmaster
    }

    private fun updateRatingSolved(handle: String) {
        val submissionData: Call<UserSubmissions> = FetchData.instance.getSubmissions(handle)
        Log.d("Dashboard", "Getting user submissions now")
        submissionData.enqueue(object : Callback<UserSubmissions> {
            override fun onResponse(call: Call<UserSubmissions>, response: Response<UserSubmissions>) {
                Log.d("Dashboard", "${response.code()}")
                val userSubmissions = response.body()
                if (userSubmissions != null) {
                    var countAc = 0
                    val mapDifficulty = mutableMapOf<String, Problem>()
                    val numberOfProblemsWithDifficulty = mutableMapOf<Int, Int>()
                    val numberOfProblemsWithIndex = mutableMapOf<String, Int>()
                    val total = userSubmissions.result.size
                    for (submission in userSubmissions.result) {
                        if (submission.verdict == resources.getString(R.string.submission_accepted)) {
                            var problem = submission.problem.contestId.toString() + submission.problem.index
                            mapDifficulty.put(problem, submission.problem)
                            countAc++
                        }
                    }
                    for (problem in mapDifficulty) {
                        if (problem.value.rating != null) {
                            numberOfProblemsWithDifficulty[problem.value.rating!!] = 1 + (numberOfProblemsWithDifficulty[problem.value.rating!!]
                                    ?: 0)
                        }
                        val problemIndex = problem.value.index[0].toString()
                        if (numberOfProblemsWithIndex.containsKey(problemIndex)) {
                            numberOfProblemsWithIndex[problemIndex] = 1 + (numberOfProblemsWithIndex[problemIndex]
                                    ?: 0)
                        } else {
                            numberOfProblemsWithIndex[problemIndex] = 1
                        }
                    }
                    Log.d("Dashboard", "Total ACs $countAc")
                    Log.d("Dashboard", "Total ACs here ${mapDifficulty.size}")
                    Log.d("Dashboard", "Total ACs with difficulty ${numberOfProblemsWithDifficulty.toString()}")
                    Log.d("Dashboard", "Total ACs with index ${numberOfProblemsWithIndex.toString()}")
                    updateProblemGraphs(numberOfProblemsWithDifficulty, numberOfProblemsWithIndex)
                } else {
                    Log.d("Dashboard", "Received null in Submissions API ${response.code()}")
                    Toast.makeText(applicationContext, "Null Received in Submissions API: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserSubmissions>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Log.d("Dashboard", "Error in Submissions API ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in API call Submissions API: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                updateRanks(-1, -1, -1, -1)
            }
        })
    }

    private fun updateProblemGraphs(difficulty: MutableMap<Int, Int>, index: MutableMap<String, Int>) {
//        userSolvedRatingsBinding.ProblemRatingGraph.setDrawBarShadow(true)
        userSolvedRatingsBinding.ProblemRatingGraph.setDrawGridBackground(true)
        val barentries = ArrayList<BarEntry>()
        var min_rating = 1000000
        var max_rating = -1000000
        for (element in difficulty) {
            barentries.add(BarEntry(element.key.toFloat(), element.value.toFloat()))
            min_rating = min(min_rating, element.key)
            max_rating = max(max_rating, element.key)
        }
        barentries.sortedBy { it.x }
        val barDataSet = BarDataSet(barentries, "Difficulty")
        barDataSet.setColor(resources.getColor(R.color.barGraphColor))
        val barData = BarData()
        barData.addDataSet(barDataSet)
        barData.barWidth = 50f
        userSolvedRatingsBinding.ProblemRatingGraph.data = barData


        if (difficulty.size > 0) {
            userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMinimum = (min_rating - 100).toFloat()
            userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMaximum = (max_rating + 100).toFloat()
        }
        userSolvedRatingsBinding.ProblemRatingGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        userSolvedRatingsBinding.ProblemRatingGraph.axisRight.setDrawLabels(false)
        userSolvedRatingsBinding.ProblemRatingGraph.description.isEnabled = false
        userSolvedRatingsBinding.ProblemRatingGraph.animateY(2000)
        userSolvedRatingsBinding.ProblemRatingGraph.axisRight.setDrawGridLines(false)
        userSolvedRatingsBinding.ProblemRatingGraph.setDrawBorders(true)
        userSolvedRatingsBinding.ProblemRatingGraph.setBorderWidth(1.5f)
        userSolvedRatingsBinding.ProblemRatingGraph.visibility = VISIBLE
        userSolvedRatingsBinding.ProblemRatingGraph.legend.isEnabled = false
        binding.UserRatingSolved.visibility = VISIBLE

        val color = when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ContextCompat.getColor(applicationContext, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ContextCompat.getColor(applicationContext, R.color.black)
            }
            else -> {
                ContextCompat.getColor(applicationContext, R.color.black)
            }
        }

        userSolvedRatingsBinding.ProblemRatingGraph.xAxis.labelCount = 5
        userSolvedRatingsBinding.ProblemRatingGraph.axisLeft.labelCount = 4
        userSolvedRatingsBinding.ProblemRatingGraph.axisLeft.textColor = color
        userSolvedRatingsBinding.ProblemRatingGraph.setPinchZoom(false)
        userSolvedRatingsBinding.ProblemRatingGraph.elevation = 10f
        userSolvedRatingsBinding.ProblemRatingGraph.setScaleEnabled(false)
        userSolvedRatingsBinding.ProblemRatingGraph.isHighlightPerTapEnabled = false
        userSolvedRatingsBinding.ProblemRatingGraph.isHighlightPerDragEnabled = false
        userSolvedRatingsBinding.ProblemRatingGraph.invalidate()
    }
}
