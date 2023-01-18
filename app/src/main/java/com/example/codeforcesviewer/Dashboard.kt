@file:Suppress("DEPRECATION")

package com.example.codeforcesviewer

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforcesviewer.UserData.ContestData.ContestAdapter
import com.example.codeforcesviewer.UserData.ContestData.ContestDataToShow
import com.example.codeforcesviewer.UserData.ContestData.UserContests
import com.example.codeforcesviewer.UserData.Styling.BarChartStyling
import com.example.codeforcesviewer.UserData.Styling.LineChartStyling
import com.example.codeforcesviewer.UserData.SubmissionData.Problem
import com.example.codeforcesviewer.UserData.SubmissionData.UserSubmissions
import com.example.codeforcesviewer.UserData.UserInfo.UserPublicData
import com.example.codeforcesviewer.databinding.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
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
import kotlin.math.roundToInt


class Dashboard : Activity() {
    lateinit var binding: ActivityDashboardBinding
    lateinit var publicDataBinding: UserPublicDataBinding
    lateinit var userGraphBinding: UserGraphBinding
    lateinit var userSolvedRatingsBinding: UserSolvedRatingsBinding
    lateinit var userSolvedIndexBinding: UserSolvedIndexBinding
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
            resources.getString(R.string.LegendaryGrandmaster) to R.color.LegendaryGrandmaster,
        )
        publicDataBinding = binding.publicDataId
        userGraphBinding = binding.userGraphId
        userSolvedRatingsBinding = binding.userSolvedRatingId
        userSolvedIndexBinding = binding.userSolvedIndexId
        val handle: String? = intent.getStringExtra("handle")
        Log.d("Dashboard", "Handle Received: $handle")
        if (handle == null) {
            Log.d("Dashboard", "No Handle received")
            Toast.makeText(applicationContext, "No Handle received!!!", Toast.LENGTH_LONG).show()
        } else {
            getData(handle)
            updateGraph(handle)
            updateRatingSolved(handle)
        }
    }

    private fun getData(handle: String) {
        Log.d("Dashboard", "Getting User Info now")
        val publicData: Call<UserPublicData> = FetchData.instance.getUserData(handle)
        publicData.enqueue(object : Callback<UserPublicData> {
            override fun onResponse(call: Call<UserPublicData>, response: Response<UserPublicData>) {
                Log.d("Dashboard", "User Info Response Code${response.code()}")
                val userData = response.body()
                if (userData != null) {
                    updateUI(userData)
                    val sharedPreference =  getSharedPreferences("com.example.codeforcesviewer", Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putString("username", handle)
                    editor.commit()
                    showRanks()
                    getAllUsersData(handle, userData.result.get(0).country)
                } else {
                    Log.d("Dashboard", "Null received in User Info : Response Code ${response.code()}")
                    Toast.makeText(applicationContext, "Null received in User Info : Response Code ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserPublicData>, t: Throwable) {
                Log.d("DashBoard", "Failure: ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in API User Info : Message ${t.localizedMessage}", Toast.LENGTH_LONG).show()
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
        val result = userPublicData.result[0]
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
        updateRegisteredOnline(result.registrationTimeSeconds)
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
                publicDataBinding.ContributionAnswer.setTextColor(resources.getColor(R.color.positiveChange))
                publicDataBinding.ContributionAnswer.text = "+$contribution"
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

    private fun updateRegisteredOnline(time1: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            val days = seconds / 3600 / 24
            val date = getDaysAgo(days.toInt())
            Log.d("Dashboard", "Date and Time or Registration: $timeNow $timePrev $seconds $days")
            Log.d("Dashboard", "Date of Registration : $date")
            publicDataBinding.RegisteredAnswer.text = getMonth(date.month) + " " + date.date + ", " + (date.year + 1900)
        } else
            publicDataBinding.RegisteredAnswer.text = "No Info!"
    }

    private fun checkOnline(time1: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val timeNow: Long = Instant.now().toEpochMilli() / 1000
            val timePrev: Long = time1
            val seconds = timeNow - timePrev
            Log.d("Dashboard", "Seconds passed since last online: $seconds")
            if (seconds <= 2 * 3600) // online in the previous 2 hours
                publicDataBinding.onlineIndicator.visibility = VISIBLE
            else
                publicDataBinding.onlineIndicator.visibility = INVISIBLE
            return
        }
        publicDataBinding.onlineIndicator.visibility = INVISIBLE
    }

    private fun updateImage(url: String) {
        val downloader = DownloadImageTask(publicDataBinding.profilePhotoImageView)
        downloader.execute("https:$url")
    }

    private fun updateColor(rank: String, max_rank: String) {
        Log.d("Dashboard", "Rank of User: $rank")
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
            Log.i("URLForImage", urldisplay.toString())
            var mIcon11: Bitmap? = null
            try {
                Log.i("FinalURL", URL(urldisplay).toString())
                val `in`: InputStream = URL(urldisplay).openStream()
                Log.i("ReachedHere", "CreatedInputStream")
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Dashboard", "Error in Image Download + ${e.message!!}")
                e.printStackTrace()
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            if(result == null) {
                bmImage.setImageResource(R.drawable.unfounduser)
                return
            }
            bmImage.setImageBitmap(result)
        }
    }

    private fun getAllUsersData(handle: String, country: String?) {
        val publicData: Call<UserPublicData> = FetchData.instance.getAllUsers()
        Log.d("Dashboard", "Getting All users now")
        Log.d("Handle of User", handle)
        publicData.enqueue(object : Callback<UserPublicData> {
            override fun onResponse(call: Call<UserPublicData>, response: Response<UserPublicData>) {
                Log.d("Dashboard", "All Users : Response Code ${response.code()}")
                val allUsers = response.body()
                if (allUsers != null) {
                    var worldRank = 1
                    var countryRank = 1
                    val totalWorld = allUsers.result.size
                    var totalInCountry = 1
                    for (result in allUsers.result) {
                        if (result.handle == handle) {
                            Log.i("All Users", "Handle Matched")
                            break
                        }
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
                    Log.d("Dashboard", "Received null in All users API : Response Code ${response.code()}")
                    Toast.makeText(applicationContext, "Null Received in Fetching All Users : Response Code ${response.code()}", Toast.LENGTH_LONG).show()
                    updateRanks(-1, -1, -1, -1)
                }
            }

            override fun onFailure(call: Call<UserPublicData>, t: Throwable) {
                Log.d("DashBoard", "Error in All Users API : Message ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in Fetching All Users : Message ${t.localizedMessage}", Toast.LENGTH_LONG).show()
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
                val dataReturned = response.body()
                if (dataReturned != null) {
                    var maxHere = -2000000
                    var minHere = 2000000
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
                    val minTime = if (dataReturned.result.isNotEmpty()) dataReturned.result[0].ratingUpdateTimeSeconds else 0
                    for (contest in dataReturned.result) {
                        Log.d("Dashboard", "Contest Item ${contest.toString()}")
                        maxHere = max(maxHere, contest.newRating)
                        minHere = min(minHere, contest.newRating)
                        Log.d("Dashboard", "Adding point on Rating Graph : ${contest.newRating} ${(contest.ratingUpdateTimeSeconds - minTime).toFloat() / 1000}")
                        ratings.add(Entry((contest.ratingUpdateTimeSeconds - minTime).toFloat() / 1000, contest.newRating.toFloat()))
                        position++
                        val newContest = ContestDataToShow(
                            (position).toString(),
                            contest.contestName ?: "Unknown Contest",
                            if (contest.rank != null) contest.rank.toString() else "NA",
                            (if (contest.newRating - contest.oldRating > 0) "+" else "") + (contest.newRating - contest.oldRating).toString(),
                            contest.newRating.toString(),
                            if (contest.newRating - contest.oldRating > 0) R.color.positiveChange else R.color.negativeChange,
                            getRatingColor(contest.newRating))
                        contestToShow.add(newContest)
                    }
                    var count = 0
                    for (entry in ratings) {
                        if (entry.y != maxHere.toFloat() || count == 1) {
                            circleColors.add(resources.getColor(R.color.ratingGraph))
                        } else {
                            circleColors.add(resources.getColor(R.color.maxRating))
                            count = 1
                        }
                    }
                    maxHere = if (maxHere != -2000000) maxHere + 200 else 2000
                    minHere = if (minHere != 2000000) min(minHere - 200, 1200) else 1200
                    val dataSets = ArrayList<ILineDataSet>()
                    ratings.sortBy { it.x }
                    val lineDataSet = LineDataSet(ratings, handle)
                    lineDataSet.lineWidth = 2F
                    lineDataSet.color = resources.getColor(R.color.ratingGraph)
                    lineDataSet.circleColors = circleColors
                    lineDataSet.circleHoleRadius = 1.6f
                    lineDataSet.circleHoleColor = resources.getColor(R.color.maxRating)
                    lineDataSet.setDrawValues(false)
                    dataSets.add(lineDataSet)
                    styleRatingGraph(maxHere, minHere)
                    userGraphBinding.RatingGraph.data = LineData(dataSets)
                    userGraphBinding.RatingGraph.invalidate()
                    userGraphBinding.contestDropDown.setOnClickListener {
                        val builder = AlertDialog.Builder(this@Dashboard)
                        val titleView = layoutInflater.inflate(R.layout.contest_heading, null)
                        builder.setCustomTitle(titleView)
                        val recyclerView = layoutInflater.inflate(R.layout.contest_recycler_view, null)
                        val recyclerViewView = recyclerView.findViewById<RecyclerView>(R.id.contest_recycler_view_view)
                        recyclerViewView.adapter = ContestAdapter(this@Dashboard, contestToShow)
                        recyclerViewView.setHasFixedSize(false)
                        builder.setCancelable(true)
                        builder.setView(recyclerView)
                        val dialog = builder.create()
                        val closeButton = titleView.findViewById<Button>(R.id.closeRecyclerView)
                        closeButton?.setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                } else {
                    Log.d("Dashboard", "Null Received in Fetching Contests : Response Code ${response.code()}")
                    Toast.makeText(applicationContext, "Null Received in Fetching Contests : Response Code ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserContests>, t: Throwable) {
                Log.d("DashBoard", "Error in Fetching Contests : Message ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in Fetching Contests : Message ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun styleRatingGraph(max_here: Int, min_here: Int) {
        Log.d("Dashboard", "YAxis constraints : Max = $max_here,  Min = $min_here")
        userGraphBinding.RatingGraph.axisLeft.setAxisMaxValue(max_here.toFloat())
        userGraphBinding.RatingGraph.axisLeft.setAxisMinValue(min_here.toFloat())

        val color = when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> R.color.white
            else -> R.color.black
        }

        LineChartStyling(userGraphBinding.RatingGraph, applicationContext).styleIt(color)
        binding.UserGraph.visibility = VISIBLE
        userGraphBinding.RatingGraph.visibility = VISIBLE
    }

    private fun getRatingColor(rating: Int): Int {
        if (rating < 1200) return R.color.Newbie
        if (rating < 1400) return R.color.Pupil
        if (rating < 1600) return R.color.Specialist
        if (rating < 1900) return R.color.Expert
        if (rating < 2100) return R.color.CandidateMaster
        if (rating < 2300) return R.color.Master
        if (rating < 2400) return R.color.InternationalMaster
        if (rating < 2700) return R.color.GrandMaster
        if (rating < 3000) return R.color.InternationalGrandmaster
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
                    for (submission in userSubmissions.result) {
                        if (submission.verdict == resources.getString(R.string.submission_accepted)) {
                            val problem = submission.problem.contestId.toString() + submission.problem.index
                            mapDifficulty[problem] = submission.problem
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
                    Log.d("Dashboard", "Total ACs (includes repeated ACs) $countAc")
                    Log.d("Dashboard", "Total ACs (unique) ${mapDifficulty.size}")
                    Log.d("Dashboard", "Total ACs with difficulty ${numberOfProblemsWithDifficulty.toString()}")
                    Log.d("Dashboard", "Total ACs with index ${numberOfProblemsWithIndex.toString()}")
                    updateProblemDifficultyGraph(numberOfProblemsWithDifficulty)
                    updateProblemIndexGraph(numberOfProblemsWithIndex)
                } else {
                    Log.d("Dashboard", "Null Received in Fetching Submissions : Response Code ${response.code()}")
                    Toast.makeText(applicationContext, "Null Received in Fetching Submissions : Response Code ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<UserSubmissions>, t: Throwable) {
                Log.d("DashBoard", "Error in Fetching Submissions: Message ${t.localizedMessage}")
                Toast.makeText(applicationContext, "Error in Fetching Submissions: Message ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                updateRanks(-1, -1, -1, -1)
            }
        })
    }

    private fun updateProblemDifficultyGraph(difficulty: MutableMap<Int, Int>) {
        val barEntries = ArrayList<BarEntry>()
        var minRating = 1000000
        var maxRating = -1000000
        var maxNumberOfProblems = 0
        for (element in difficulty) {
            barEntries.add(BarEntry(element.key.toFloat(), element.value.toFloat()))
            minRating = min(minRating, element.key)
            maxRating = max(maxRating, element.key)
            maxNumberOfProblems = max(maxNumberOfProblems, element.value)
        }

        barEntries.sortedBy { it.x }
        val barDataSet = BarDataSet(barEntries, "Difficulty")
        barDataSet.color = resources.getColor(R.color.barGraphColor)
        barDataSet.setDrawValues(true)
        val barData = BarData()

        //formatter
        class MyValueFormatter : ValueFormatter() {
            override fun getFormattedValue(value: Float): String? {
                return value.roundToInt().toString() + ""
            }
        }
        barDataSet.valueFormatter = MyValueFormatter()
        barData.addDataSet(barDataSet)

        Log.d("Dashboard", "BarDataDifficulty Width ${barData.barWidth}")
        val color = when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> R.color.white
            else -> R.color.black
        }
        BarChartStyling(userSolvedRatingsBinding.ProblemRatingGraph, applicationContext).styleIt(color)
        userSolvedRatingsBinding.ProblemRatingGraph.axisLeft.labelCount = min(userSolvedRatingsBinding.ProblemRatingGraph.axisLeft.labelCount, maxNumberOfProblems)
        barData.setValueTextColor(ContextCompat.getColor(applicationContext, color))
        barData.setValueTextSize(9f)
        if (difficulty.isNotEmpty()) {
            userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMinimum = (minRating - 100).toFloat()
            userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMaximum = (maxRating + 100).toFloat()
//            val x = userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMaximum
//            val y = userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMinimum

            barData.barWidth = 0.75f * 100
            userSolvedRatingsBinding.ProblemRatingGraph.xAxis.labelCount = min(difficulty.size, userSolvedRatingsBinding.ProblemRatingGraph.xAxis.labelCount)
        }

        class LabelFormatter : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val x = value
                if (x == userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMaximum || x == userSolvedRatingsBinding.ProblemRatingGraph.xAxis.axisMinimum)
                    return ""
                val y = value.toInt()
                return y.toString()
            }
        }

        val formatter = LabelFormatter()
        userSolvedRatingsBinding.ProblemRatingGraph.xAxis.valueFormatter = formatter
        userSolvedRatingsBinding.ProblemRatingGraph.data = barData
        userSolvedRatingsBinding.ProblemRatingGraph.invalidate()
        userSolvedRatingsBinding.ProblemRatingGraph.visibility = VISIBLE
        binding.UserRatingSolved.visibility = VISIBLE
        userSolvedRatingsBinding.ProblemRatingGraph.xAxis.setDrawGridLines(false)
    }

    private fun updateProblemIndexGraph(index: MutableMap<String, Int>) {
        val barEntries = ArrayList<BarEntry>()
        var minRating = 1000000
        var maxRating = -1000000
        var maxNumberOfProblems = 0
        for (element in index) {
            val index1 = (element.key.toCharArray()[0] - 'A' + 1)
            barEntries.add(BarEntry(index1.toFloat(), element.value.toFloat()))
            minRating = min(minRating, index1)
            maxRating = max(maxRating, index1)
            maxNumberOfProblems = max(maxNumberOfProblems, element.value)
        }

        barEntries.sortedBy { it.x }
        val barDataSet = BarDataSet(barEntries, "Index")
        barDataSet.color = resources.getColor(R.color.barGraphColor)
        barDataSet.setDrawValues(true)
        val barData = BarData()

        //formatter
        class MyValueFormatter : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.roundToInt().toString() + ""
            }
        }

        val color = when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> R.color.white
            else -> R.color.black
        }
        BarChartStyling(userSolvedIndexBinding.ProblemIndexGraph, applicationContext).styleIt(color)
        barDataSet.valueFormatter = MyValueFormatter()
        userSolvedIndexBinding.ProblemIndexGraph.axisLeft.labelCount = min(userSolvedIndexBinding.ProblemIndexGraph.axisLeft.labelCount, maxNumberOfProblems)
        barData.addDataSet(barDataSet)
        if (index.isNotEmpty()) {
            userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMinimum = (max(0, minRating - 1)).toFloat()
            userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMaximum = (min(27, maxRating + 1)).toFloat()
//            val x = userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMaximum
//            val y = userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMinimum
            barData.barWidth = 0.75f * 1
            Log.d("Dashboard", "${index.size.toFloat()}")
            userSolvedIndexBinding.ProblemIndexGraph.xAxis.labelCount = min(index.size, userSolvedIndexBinding.ProblemIndexGraph.xAxis.labelCount)
        }
        barData.setValueTextColor(ContextCompat.getColor(applicationContext, color))
        barData.setValueTextSize(9f)
        Log.d("Dashboard", "BarDataIndex Width ${barData.barWidth}")
        userSolvedIndexBinding.ProblemIndexGraph.data = barData
        userSolvedIndexBinding.ProblemIndexGraph.xAxis.labelCount = userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMaximum.toInt()

        class LabelFormatter : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val x = value.toInt()
                if (value == userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMaximum || value == userSolvedIndexBinding.ProblemIndexGraph.xAxis.axisMinimum)
                    return ""
                val y = x - 1
                val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                return chars[y].toString()
            }
        }

        val formatter = LabelFormatter()
        userSolvedIndexBinding.ProblemIndexGraph.xAxis.setDrawGridLines(false)
        userSolvedIndexBinding.ProblemIndexGraph.xAxis.valueFormatter = formatter
        userSolvedIndexBinding.ProblemIndexGraph.invalidate()
        userSolvedIndexBinding.ProblemIndexGraph.visibility = VISIBLE
        binding.UserIndexSolved.visibility = VISIBLE
    }
}
