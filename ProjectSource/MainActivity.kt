package com.e.a306coursework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.koushikdutta.ion.Ion
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

/**
 * -This is the Main Source File of the application
 *  and it handles all of the processes of the application
 *  @author George Cook (984336)
 *  @date 09/12/2020
 *  @version 1.2
 *  @versionHistory 1.0, 1.1
 */
class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var user: String? = null
    private var stories = Array<String?>(4) { null }
    private var indexes = arrayOf<Int>(4)
    private var channelId = "NewsChannel"

    /**
     * -This method is called to start the application it tells the user that there are new stories.
     *  via the a notification.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        getNews()
        mAuth = FirebaseAuth.getInstance()
        createNotificationChannel()
        addNotification()

        //onStart()
    }

    /**
     * -This method opens the text file that stores the current logged in users preferences.
     * -It then displays the preferences to the user so that they can edit and review them.
     */
    private fun loadPreferences() {
        var myData = ""
        val myExternalFile = File(getExternalFilesDir(null), "$user preferences.txt")
        try {
            val fis = FileInputStream(myExternalFile)
            Log.d("PREFERENCES_LOAD", myExternalFile.toString())
            val `in` = DataInputStream(fis)
            val br = BufferedReader(InputStreamReader(`in`))
            var strLine: String? = null
            while ({ strLine = br.readLine(); strLine }() != null) {
                myData = """
                $myData$strLine
                
                """.trimIndent()
            }
            br.close()
            `in`.close()
            fis.close()
            Log.d("PREFERENCES_LOAD", myData)
            if (myData != "") {
                var editText = findViewById<EditText>(R.id.preferencesBox)
                editText.setText(myData)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * -This method logs out the currently logged in user.
     */
    fun logoutButtonOnClick(view: View) {
        user = null
        newsButtonOnClick(view)
    }

    /**
     * -This method checks that the user data is a valid login and then logs the user in.
     * -Calling the method loadPreferences()
     */
    fun loginButtonOnClick(view: View) {

        val email = findViewById<EditText>(R.id.loginUsername)
        val password = findViewById<EditText>(R.id.loginPassword)

        var usernameText = email.text.toString()
        var passwordText = password.text.toString()


        Log.d("LOGIN_ACTIVITY", usernameText)
        Log.d("LOGIN_ACTIVITY", passwordText)

        if (usernameText != "" && passwordText != "") {

            mAuth.signInWithEmailAndPassword(usernameText, passwordText)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login", "signInWithEmail:success")
                        user = usernameText
                        setContentView(R.layout.activity_preferences)
                        loadPreferences()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Fail", "signInWithEmail:failure", task.exception)
                        val snackBar = Snackbar.make(view, "Failed", Snackbar.LENGTH_LONG)
                        snackBar.show()

                    }
                }
        } else {
            val snackBar = Snackbar.make(view, "Failed", Snackbar.LENGTH_LONG)
            snackBar.show()
        }
    }

    /**
     * -This button takes the user data and then creates a new account for the user, if the account
     *  doesn't already exist.
     * -It then takes the user to the page where they can input their preferences.
     */
    fun signUpButtonOnClick(view: View) {
        val email = findViewById<EditText>(R.id.loginUsername)
        val password = findViewById<EditText>(R.id.loginPassword)
        var usernameText = email.text.toString()
        var passwordText = password.text.toString()

        Log.d("LOGIN_ACTIVITY", usernameText)
        Log.d("LOGIN_ACTIVITY", passwordText)

        if (usernameText != "" && passwordText != "") {

            mAuth.createUserWithEmailAndPassword(usernameText, passwordText)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login", "signInWithEmail:success")
                        setContentView(R.layout.activity_preferences)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Fail", "signInWithEmail:failure", task.exception)
                        val snackBar = Snackbar.make(view, "Failed", Snackbar.LENGTH_LONG)
                        snackBar.show()
                    }
                }
        } else {
            val snackBar = Snackbar.make(view, "Failed", Snackbar.LENGTH_LONG)
            snackBar.show()
        }

    }

    /**
     * -This method takes the user to the News Activity screen.
     */
    fun newsButtonOnClick(view: View) {
        setContentView(R.layout.activity_news)
        getNews()
    }

    /**
     * -This method takes the user to the Stocks Activity screen.
     */
    fun stockButtonOnClick(view: View) {
        setContentView(R.layout.activity_stocks)
    }

    /**
     * -This method takes the user to the Quotes Activity screen.
     */
    fun quotesButtonOnClick(view: View) {
        setContentView(R.layout.activity_quotes)
        getQuoteOfTheDay()
        getRandomQuote(view)
    }

    /**
     * -This method takes the user to the Login Activity screen.
     * -If a user is already logged in then it takes them straight to their preferences page.
     */
    fun preferencesButtonOnClick(view: View) {
        if (user != null) {
            setContentView(R.layout.activity_preferences)
            loadPreferences()
        } else if (user == null) {
            setContentView(R.layout.activity_login_screen)
        }

    }

    /**
     * -This method is used to refresh the News Activity when the user is on the news Screen.
     */
    fun refresh(view: View) {
        addNotification()
        getNews()
    }

    /**
     * -This method returns the user's news preferences in the properly formatted way,
     *  so that they can be then parsed into the url for the news api.
     */
    private fun preferenceString(): String {
        var myData = ""
        val myExternalFile = File(getExternalFilesDir(null), "$user preferences.txt")
        try {
            val fis = FileInputStream(myExternalFile)
            Log.d("PREFERENCES_LOAD", myExternalFile.toString())
            val `in` = DataInputStream(fis)
            val br = BufferedReader(InputStreamReader(`in`))
            var strLine: String? = null
            while ({ strLine = br.readLine(); strLine }() != null) {
                myData = """
                $myData$strLine
                
                """.trimIndent()
            }
            br.close()
            `in`.close()
            fis.close()
            Log.d("PREFERENCES_LOAD", myData)
        } catch (e: IOException) {
            e.printStackTrace()
        }


        myData = myData.toLowerCase()
        myData = myData.replace("\n", "")
        myData = myData.replace(" ", "")
        myData = myData.replace(",", "%20OR%20")

        return myData
    }

    /**
     * -This method works out which news article the user has clicked on and then loads it in the
     *  phones web browser.
     */
    fun newsArticleOnClick(view: View) {
        println(view.getId())
        var cardNumber = 0

        when (view.getId()) {
            2131230809 -> cardNumber = 0
            2131230810 -> cardNumber = 1
            2131230811 -> cardNumber = 2
            2131230812 -> cardNumber = 3
            else -> { // Note the block
                println("Id's don't match.")
            }
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(stories[cardNumber]))
        startActivity(browserIntent)
    }

    /**
     * -This method uses the news api to get the JSON that holds all the required news articles.
     * -It then parses it on to another method for processing where they get displayed to the user.
     */
    private fun getNews() {

        if (user != null) {
            var preferences = preferenceString()
            println(preferences)
            var url =
                "http://newsapi.org/v2/everything?q=($preferences)&apiKey=e9d729b9ada6453f87f90b6259994856"
            println(url)
            Ion.with(this)
                .load(url)
                .setHeader("user-agent", "insomnia/2020.4.1")
                .asString()
                .setCallback { ex, result ->
                    println(result)
                    processNewsArticles(result)
                }
        } else {
            Ion.with(this)
                .load("http://newsapi.org/v2/top-headlines?country=gb&apiKey=e9d729b9ada6453f87f90b6259994856")
                .setHeader("user-agent", "insomnia/2020.4.1")
                .asString()
                .setCallback { ex, result ->
                    processNewsArticles(result)
                }
        }
    }

    /**
     * -This method takes the users preferences and saves then to a text file that is unique to
     *  to that user.
     */
    fun saveButtonOnClick(view: View) {

        var editView = findViewById<TextView>(R.id.preferencesBox)

        val text: String = editView.text.toString()

        val state: String = Environment.getExternalStorageState()
        if (!Environment.MEDIA_MOUNTED.equals(state)) {

            //If it isn't mounted - we can't write into it.
            return
        }
        // do what you want with these
        // do what you want with these
        //val outputStream: FileOutputStream
        val file = File(getExternalFilesDir(null), "$user preferences.txt")
        lateinit var outputStream: FileOutputStream
        try {
            file.createNewFile()
            outputStream = FileOutputStream(file, false)
            Log.d("SAVE_ACTIVITY", "Made file")
            outputStream.write(text.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * -This method processes the photos for the news articles.
     * -If there is no image then it displays a no image available icon.
     * @param myPhoto This is the URL of the photo so then Picasso can get the image and display it
     * @param myImageView This is the Image View that the image needs to be displayed in.
     */
    private fun processPhoto(myPhoto: String, myImageView: ImageView) {
        if (myPhoto == "null") {
            myImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_baseline_image_not_supported_24
                )
            )

        } else {
            Picasso
                .get()
                .load(myPhoto)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(myImageView)
        }
    }

    /**
     * -This method processes the descriptions for the news articles.
     * -If there is no description then it displays a no description available message.
     * @param myDescription This is the description that needs to be displayed to the user.
     * @param myTextView This is the Text View that the description needs to be displayed in.
     */
    private fun processDescription(myDescription: String, myTextView: TextView) {
        if (myDescription == "null" || myDescription == "") {
            myTextView.text = "No Description Available"
        } else {
            myTextView.text = myDescription
        }
    }

    /**
     * -This method processes the authors for the news articles.
     * -If there is no author then it displays a no author available message
     * @param myAuthor This is the author that needs to be displayed to the user.
     * @param myTextView This is the Text View that the description needs to be displayed in.
     */
    private fun processAuthor(myAuthor: String, myTextView: TextView) {
        if (myAuthor == "null" || myAuthor == "") {
            myTextView.text = "No Authors Available"
        } else {
            myTextView.text = "By: $myAuthor"
        }
    }

    /**
     * -This method makes sure that there are no news articles that will be displayed twice.
     * @param indexes This is the Array of article numbers to be displayed.
     * @param article This is the new article number that it is checking isn't already in the Array
     */
    fun NotInArray(indexes: Array<Int>, article: Int): Boolean {
        var returnValue: Boolean = false

        for (element in indexes) {
            if (element == article) {
                returnValue = true
            }
        }
        return returnValue
    }

    /**
     * -This method processes the JSON from the news api, displaying the news articles
     *  to the user in the correct place.
     *  @param newsData The JSON String that is return by the news api.
     */
    private fun processNewsArticles(newsData: String) {
        indexes = arrayOf<Int>(-1, -1, -1, -1)

        val myJSON = JSONObject(newsData)
        val myArticles = myJSON.getJSONArray("articles")
        var range = myArticles.length() - 1
        var index = (0..range).random()
        indexes[0] = index
        var myTitle = myArticles.getJSONObject(index).getString("title")
        var myDescription = myArticles.getJSONObject(index).getString("description")
        var myAuthor = myArticles.getJSONObject(index).getString("author")
        var myPhoto = myArticles.getJSONObject(index).getString("urlToImage")
        var myUrl = myArticles.getJSONObject(index).getString("url")

        var myTxtView = findViewById<TextView>(R.id.newsTitleOne)
        myTxtView.text = myTitle
        myTxtView = findViewById<TextView>(R.id.newsDescriptionOne)
        processDescription(myDescription, myTxtView)
        myTxtView = findViewById<TextView>(R.id.newsAuthorOne)
        processAuthor(myAuthor, myTxtView)
        stories[0] = myUrl
        var myImageView = findViewById<ImageView>(R.id.newsImageOne)
        processPhoto(myPhoto, myImageView)



        while (NotInArray(indexes, index)) {
            index = (0..range).random()
        }
        indexes[1] = index
        myTitle = myArticles.getJSONObject(index).getString("title")
        myDescription = myArticles.getJSONObject(index).getString("description")
        myAuthor = myArticles.getJSONObject(index).getString("author")
        myPhoto = myArticles.getJSONObject(index).getString("urlToImage")
        myUrl = myArticles.getJSONObject(index).getString("url")

        myTxtView = findViewById<TextView>(R.id.newsTitleTwo)
        myTxtView.text = myTitle
        myTxtView = findViewById<TextView>(R.id.newsDescriptionTwo)
        processDescription(myDescription, myTxtView)
        myTxtView = findViewById<TextView>(R.id.newsAuthorTwo)
        processAuthor(myAuthor, myTxtView)
        stories[1] = myUrl
        myImageView = findViewById<ImageView>(R.id.newsImageTwo)
        processPhoto(myPhoto, myImageView)


        while (NotInArray(indexes, index)) {
            index = (0..range).random()
        }
        indexes[2] = index
        myTitle = myArticles.getJSONObject(index).getString("title")
        myDescription = myArticles.getJSONObject(index).getString("description")
        myAuthor = myArticles.getJSONObject(index).getString("author")
        myPhoto = myArticles.getJSONObject(index).getString("urlToImage")
        myUrl = myArticles.getJSONObject(index).getString("url")

        myTxtView = findViewById<TextView>(R.id.newsTitleThree)
        myTxtView.text = myTitle
        myTxtView = findViewById<TextView>(R.id.newsDescriptionThree)
        processDescription(myDescription, myTxtView)
        myTxtView = findViewById<TextView>(R.id.newsAuthorThree)
        processAuthor(myAuthor, myTxtView)
        stories[2] = myUrl
        myImageView = findViewById<ImageView>(R.id.newsImageThree)
        processPhoto(myPhoto, myImageView)


        while (NotInArray(indexes, index)) {
            index = (0..range).random()
        }
        indexes[3] = index
        myTitle = myArticles.getJSONObject(index).getString("title")
        myDescription = myArticles.getJSONObject(index).getString("description")
        myAuthor = myArticles.getJSONObject(index).getString("author")
        myPhoto = myArticles.getJSONObject(index).getString("urlToImage")
        myUrl = myArticles.getJSONObject(index).getString("url")

        myTxtView = findViewById<TextView>(R.id.newsTitleFour)
        myTxtView.text = myTitle
        myTxtView = findViewById<TextView>(R.id.newsDescriptionFour)
        processDescription(myDescription, myTxtView)
        myTxtView = findViewById<TextView>(R.id.newsAuthorFour)
        processAuthor(myAuthor, myTxtView)
        stories[3] = myUrl
        myImageView = findViewById<ImageView>(R.id.newsImageFour)
        processPhoto(myPhoto, myImageView)

    }

    /**
     * -This method searches the stock api for the correct stock, based on the users input.
     */
    fun searchForStockOnClick(view: View) {

        val searchBar = findViewById<EditText>(R.id.stockSearchBar)
        val stockToSearch = searchBar.text
        Ion.with(this)
            .load("https://alpha-vantage.p.rapidapi.com/query?function=GLOBAL_QUOTE&symbol=$stockToSearch")
            .setHeader("x-rapidapi-key", "c99f7f826bmsh06870449c42c88bp161799jsn0785cba64c71")
            .setHeader("x-rapidapi-host", "alpha-vantage.p.rapidapi.com")
            .asString()
            .setCallback { ex, result ->
                println("result")
                println(result)
                processStocks(result, view)

            }


    }

    /**
     * -This method checks that the searched for stock is valid
     *  and doesn't return an empty JSON Object
     *  @param stocks The JSON Object that it checks whether it is empty or not.
     */
    private fun checkNotEmpty(view: View, stocks: JSONObject) {
        if (stocks != {}) {
            displayStocks(stocks)
        } else {
            val snackBar =
                Snackbar.make(view, "Failed, Stock Not Available", Snackbar.LENGTH_LONG)
            snackBar.show()
        }
    }

    /**
     * -This method processes the JSON String that is returned from the stock api.
     * @param stockData The JSON String that is return my the stock api.
     */
    private fun processStocks(stockData: String, view: View) {
        println("stockData");
        println(stockData);
        val myJSON = JSONObject(stockData)
        println("myJSON");
        println(myJSON);
        val myStocks = myJSON.getJSONObject("Global Quote")
        println("myStocks");
        println(myStocks);
        checkNotEmpty(view, myStocks)
    }

    /**
     * -This method displays the stocks to the user in the correct way and place on the screen.
     * @param myStocks The JSON Object that needs to be processed and then displayed to the user.
     */
    private fun displayStocks(myStocks: JSONObject) {

        var nameOfStockFound = myStocks.getString("01. symbol")
        var openPrice = myStocks.getString("02. open")
        var highPrice = myStocks.getString("03. high")
        var lowPrice = myStocks.getString("04. low")
        var price = myStocks.getString("05. price")
        var date = myStocks.getString("07. latest trading day")
        var closePrice = myStocks.getString("08. previous close")
        var change = myStocks.getString("09. change")
        var changePercentage = myStocks.getString("10. change percent")
        println("nameOfStockFound");
        println(nameOfStockFound);


        val stockText = findViewById<TextView>(R.id.nameOfStock)
        val stockTitle = "Stock Information for: $nameOfStockFound"
        println(stockTitle)
        stockText.text = stockTitle

        val stockInformation = "Date: $date \n" +
                "Current Price: $ $price \n" +
                "    Change over the Day: $ $change \n" +
                "    Percentage Change: $changePercentage \n" +
                "    High/Low Price: $ $highPrice/$ $lowPrice \n" +
                "    Open/Previous Close Price: $ $openPrice/$ $closePrice"

        val stockInformationText = findViewById<TextView>(R.id.stockInformation)
        println(stockInformation)
        stockInformationText.text = stockInformation
    }

    /**
     * -This method gets the quote of the day from the quote api.
     */
    private fun getQuoteOfTheDay() {
        Ion.with(this)
            .load("https://quotes.rest/qod?language=en")
            .asString()
            .setCallback { ex, result ->
                processQuoteOfTheDay(result)
            }
    }

    /**
     * -This method displays the quote to the user in the correct way and place on the screen.
     * @param quoteOfTheDay The JSON String that needs to be processed
     *                      and then displayed to the user.
     */
    private fun processQuoteOfTheDay(quoteOfTheDay: String) {
        val myJSON = JSONObject(quoteOfTheDay)
        println("myJSON")
        println(myJSON)
        val myContents = myJSON.getJSONObject("contents")
        println("myContents")
        println(myContents)
        val myQuotes = myContents.getJSONArray("quotes")
        println("myQuotes")
        println(myQuotes)
        val quoteOfTheDayQuote = myQuotes.getJSONObject(0).getString("quote")
        println(quoteOfTheDayQuote)
        val quoteOfTheDayAuthor = myQuotes.getJSONObject(0).getString("author")

        val myTextView = findViewById<TextView>(R.id.quoteOfTheDay)
        myTextView.text = quoteOfTheDayQuote

        val myTextViewAuthor = findViewById<TextView>(R.id.quoteOfTheDayAuthor)
        myTextViewAuthor.text = quoteOfTheDayAuthor
    }

    /**
     * -This method gets a JSON String, which holds loads of quotes from a database.
     */
    fun getRandomQuote(view: View) {
        Ion.with(this)
            .load("https://type.fit/api/quotes")
            .setHeader("user-agent", "insomnia/2020.4.1")
            .asString()
            .setCallback { ex, result ->
                processRandomQuote(result)
            }

    }

    /**
     * -This method takes in the JSON String and then processes it, by picking a random quote
     *  and then displaying it to the user in the correct way.
     *  @param quoteData The JSON String that needs to be processed and then displayed.
     */
    private fun processRandomQuote(quoteData: String) {
        val myArray = JSONArray(quoteData)
        println("myArray")
        println(myArray)
        println(myArray.length())
        var length = myArray.length() - 1
        var index = (0..length).random()
        val randomQuoteText = myArray.getJSONObject(index).getString("text")
        println(randomQuoteText)
        val randomQuoteAuthor = myArray.getJSONObject(index).getString("author")
        println(randomQuoteAuthor)

        val myTextView = findViewById<TextView>(R.id.randomQuoteText)
        myTextView.text = randomQuoteText

        val myTextViewAuthor = findViewById<TextView>(R.id.randomQuoteAuthor)
        if (randomQuoteAuthor == "null") {
            myTextViewAuthor.text = "No Author Given"
        } else {
            myTextViewAuthor.text = randomQuoteAuthor
        }

    }

    /**
     * -This method creates the notification that gets displayed to the user,
     *  to let them know that there are new News Articles.
     */
    private fun addNotification() {

        var builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.newStoryTitle))
            .setContentText(getString(R.string.newStoryContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)


        val notificationId = 0
        println("Displaying Notification")
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }


    }

    /**
     * -This method creates a notification channel for the application,
     *  so that it can display notifications to the user.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channelName)
            val descriptionText = getString(R.string.channelDescription)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}