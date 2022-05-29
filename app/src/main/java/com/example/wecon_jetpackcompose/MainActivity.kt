package com.example.wecon_jetpackcompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.wecon_jetpackcompose.ui.theme.WeCon_JetpackComposeTheme
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.*
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.google.firebase.database.*
import org.intellij.lang.annotations.JdkConstants

// https://www.geeksforgeeks.org/start-a-new-activity-using-intent-in-android-using-jetpack-compose/

// colors used in the app
val GREY= Color(0xFF202124)
val YELLOW= Color(0xFFFFE715)
val LIGHT_GREY= Color(0xFF494949)
val BUTTER_YELLOW= Color(0xFFFEE227)
val LEMON_YELLOW= Color(0xFFEFFD5F)


// >> things to implement
// > implement chat logic and complete system
// review whole app again for any addition
// internet check
// loading waiting animation


class MainActivity : ComponentActivity() {

    // firebase auth variable
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDBReference : DatabaseReference

    val userList =   mutableStateListOf<User>()


    val allUserEmails =   mutableStateListOf<String>()

//    val userList = remember { mutableStateListOf<User>()}


//    var currentUser: User = User()

    var currentUser  = mutableStateOf<User>(User(":)", null, null , null  , mutableListOf<String>()))

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)


        auth = FirebaseAuth.getInstance()

        Log.i("** Custom Log Message **", "OnCreate of MainActivity Is called")


        val uid = auth.currentUser?.uid

        firebaseDBReference = FirebaseDatabase.getInstance().getReference("user")

        // Read current user from the database
        firebaseDBReference.child(uid!!).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                currentUser.value  = snapshot.getValue(User::class.java)!!
                Log.i(
                    "** Custom Log Message **",
                    "Value is: " + currentUser.value.email + currentUser.value.uid + currentUser.value.name + currentUser.value.contacts
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("** Custom Log Message **", "Failed to read value.")
            }

        })

        // read list of users in the database
        firebaseDBReference = FirebaseDatabase.getInstance().getReference("user")
        firebaseDBReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot use to get database from db

                // clearing any previous list


                userList.clear()
                allUserEmails.clear()
                for (snap in snapshot.children) {
                    val user = snap.getValue(User::class.java)
                    // add we dont want to message our self xD


                    // making list of user's email that are using app (who has wecon accounts)
                    if (currentUser.value.email != user?.email)
                        allUserEmails.add(user?.email!!)

                    // contacts that are in the current user's contact list
                    if (auth.currentUser?.uid != user?.uid  &&  currentUser.value.contacts.contains(user?.email) ) {
                        Log.i("** Custom Note **", "** This is A LOOP _> $user **")

                        userList.add(user!!)
                    }
                }


                  Log.i("** Custom Note **", "** Data Set Update $userList **" )

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        setContent {
            WeCon_JetpackComposeTheme {

                MainAppUI()
                Toast.makeText(this@MainActivity , "Welcome Back :)" ,Toast.LENGTH_SHORT ).show()

            }
        }

    }



    @Composable
    fun MainAppUI ()
    {
        val isDialogOpen =  remember { mutableStateOf(false) }
        val users = remember { userList}
        val currentUser0 = remember {currentUser}
        Scaffold(
            backgroundColor = GREY,
            topBar = {

                TopAppBar(
                    backgroundColor = YELLOW,

                    title = {
                        Text(text = "WeCon ~ ${currentUser0.value.name?.capitalize()}", color = GREY , fontSize = 20.sp,
                            fontWeight = FontWeight.Bold)
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                Log.d(
                                    "ButtonClicked",
                                    "Sign Out Button Clicked"
                                )
                                // signing out current user using firebase builtin function
                                auth.signOut()

                                val intent = Intent(this@MainActivity, LogIn::class.java)

                                finish() // destroying the current menu

                                startActivity(intent) // after logout it will show the login screen

                            }) {
                            Icon(
                                painterResource(id = R.drawable.logout),
                                modifier = Modifier.size(25.dp),
                                tint = GREY,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            content = {
                        AllCards(users )
                        ShowAlertDialog(isDialogOpen)
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {}, backgroundColor = YELLOW) {
                    IconButton(onClick = {
                        Log.d(
                            "ButtonClicked",
                            "Add Button Clicked :)"
                        )
                        isDialogOpen.value = true

                    }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            tint = GREY,
                            contentDescription = "add",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
        )
    }

    @Composable
    fun AllCards (users: SnapshotStateList<User>)
    {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
//            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 100.dp),
//            verticalArrangement = Arrangement.spacedBy(.dp)
        ){
            //  Add a Multiple Items
//            items(Items.size) { i -> CustomCard(Items[i] , i ) }
            itemsIndexed(users) { index, user -> ContactRow(index , user )}

        }

    }


    //    @OptIn(ExperimentalCoilApi::class)
    @Composable
    fun ContactRow(userNum : Int , user: User)
    {
//    var name: String? = "muneeb" // currentUser.name
//    var uid: String? = "UKSR9DwAZ2YOimoPpC4utDpcU002"    //  currentUser.uid


    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable{
                val intent = Intent ( this@MainActivity , Chat::class.java)
                // have to pass chat person name / id
                intent.putExtra("uid" , user.uid) // id of receiver
                startActivity(intent)
            },

        backgroundColor = GREY,
//        border = BorderStroke(5.dp, Color.Red),

        contentColor = Color.Black,

        ) {

        Column(Modifier.background(color = LIGHT_GREY) ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.5.dp)
                    .padding(15.dp)

            )
            {
                Image(
                    painter = rememberImagePainter(
                        data = "https://firebasestorage.googleapis.com/v0/b/wecon-jetpackcompose.appspot.com/o/images%2F${user.uid}?alt=media&token=c92e2cbd-3ce9-4e82-a512-bbbaaece4058"
                    ),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)

                )

                Spacer(Modifier.width(20.dp))
                Text(
                    text = user.name!!.capitalize(),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = YELLOW,
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp)

                )
                Spacer(Modifier.width(60.dp))
//                IconButton(  onClick = {
//                    Log.d(
//                        "ButtonClicked",
//                        "Delete Button Clicked"
//                    )
////                    Items.removeAt(cardNum)
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Item Removed!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                ) {
//                    Icon(Icons.Filled.Delete, tint = Color.Red, contentDescription = "Delete")
//                }
            }
        }

    }

}


    @Composable
    fun ShowAlertDialog(isDialogOpen: MutableState<Boolean>) {

        var email by remember { mutableStateOf("") }


        val context = LocalContext.current


        if (isDialogOpen.value) {
            Dialog(onDismissRequest = { isDialogOpen.value = false }) {
                Surface(
                    modifier = Modifier
                        .width(350.dp)
                        .height(250.dp)
                        .padding(5.dp),
                    shape = RoundedCornerShape(5.dp), color = GREY
                ) {
                    Column(
                        modifier = Modifier.padding(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.padding(5.dp))

                        Text(
                            text = "Add Contact",
                            color = YELLOW,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp
                        )
                        // email

                        Spacer(modifier = Modifier.padding(10.dp))

                        OutlinedTextField(
                            value = email,

                            onValueChange = { email = it },
                            label = { androidx.compose.material3.Text(text = "Email", color = Color.White) },
                            placeholder = { Text(text = "Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = LIGHT_GREY, textColor = YELLOW , focusedBorderColor = YELLOW , cursorColor = YELLOW , placeholderColor = LIGHT_GREY
                            )
                        )

                        Spacer(modifier = Modifier.padding(25.dp))
                        Button(
                            onClick = {

                                if (email.isNullOrEmpty())
                                {
                                    Toast.makeText(
                                        context,
                                        "Please Complete the Field!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else if (!allUserEmails.contains(email))
                                {
                                    Toast.makeText(
                                        context,
                                        "Current Email Is Not A User of WeCon",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else{
                                    isDialogOpen.value = false

                                    // add in output here
                                    var updatedContacts = mutableListOf<String>()
                                    Log.i("** CUSTOM LOG **" , "Old Contact List: 0 -> ${currentUser.value.contacts}")

                                    updatedContacts= updatedContacts.plus(currentUser.value.contacts) as MutableList<String>
                                    Log.i("** CUSTOM LOG **" , "New Contact List: 1 -> $updatedContacts")

                                    updatedContacts.add(email)
                                    Log.i("** CUSTOM LOG **" , "New Contact List: 2 -> $updatedContacts")

//                                    var newContacts: ArrayList<String>? =  currentUser.contacts?  +  ArrayList<String>?(email)
                                    Log.i("** CUSTOM LOG **" , "New Contact List: $updatedContacts")

                                    firebaseDBReference = FirebaseDatabase.getInstance().getReference()

                                    // creating of node for current user in the firebase database
                                    // this will add user to the database
//                                    firebaseDBReference.child("user").child(currentUser.uid!!).setValue(User(currentUser.name ,currentUser.email,currentUser.uid , currentUser.profile_pic  , updatedContacts))
                                    firebaseDBReference.child("user").child(currentUser.value.uid!!).child("contacts").setValue(updatedContacts)

                                    currentUser.value.contacts.add(email)
                                    //

                                    Toast.makeText(
                                        context,
                                        "Contact Added!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                Log.d(
                                    "ButtonClicked",
                                    "Add Contact Button Clicked :)" +
                                            email
                                )
                                email = ""




                            },
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(60.dp)
                                .padding(5.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = YELLOW)
                        ) {
                            Text(
                                text = "Add",
                                color = GREY,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

    }









    fun getUsersListFromFirebase ()
    {

    }






}
