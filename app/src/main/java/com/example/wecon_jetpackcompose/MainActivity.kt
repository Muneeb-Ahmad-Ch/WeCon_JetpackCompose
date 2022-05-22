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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.google.firebase.database.*

// https://www.geeksforgeeks.org/start-a-new-activity-using-intent-in-android-using-jetpack-compose/

// colors used in the app
val GREY= Color(0xFF202124)
val YELLOW= Color(0xFFFFE715)
val LIGHT_GREY= Color(0xFF494949)
val BUTTER_YELLOW= Color(0xFFFEE227)
val LEMON_YELLOW= Color(0xFFEFFD5F)


class MainActivity : ComponentActivity() {

    // firebase auth variable
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDBReference : DatabaseReference
//    lateinit var userList: ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        var currentUser: User

        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        Log.i("** Custom Log Message **", "OnCreate of MainActivity Is called")


        val uid = auth.currentUser?.uid

        firebaseDBReference = FirebaseDatabase.getInstance().getReference("user")

        // Read from the database
        firebaseDBReference.child(uid!!).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                currentUser = snapshot.getValue(User::class.java)!!
                Log.i(
                    "** Custom Log Message **",
                    "Value is: " + currentUser.email + currentUser.uid + currentUser.name
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("** Custom Log Message **", "Failed to read value.")
            }

        })



        setContent {
            WeCon_JetpackComposeTheme {

              MainAppUI()

            }
        }

    }



    @Composable
    fun MainAppUI ()
    {
        val isDialogOpen =  remember { mutableStateOf(false) }


        Scaffold(
            backgroundColor = GREY,
            topBar = {

                TopAppBar(
                    backgroundColor = YELLOW,

                    title = {
                        Text(text = "Restaurant Record Book", color = GREY , fontSize = 20.sp,
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
//                        AllCards(Items )
                        ShowAlertDialog(isDialogOpen)
                ContactRow()
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
                            contentDescription = "add"
                        )
                    }
                }
            },
        )
    }


    //    @OptIn(ExperimentalCoilApi::class)
    @Composable
    fun ContactRow() {
    var name: String? = "muneeb" // currentUser.name
    var uid: String? = "UKSR9DwAZ2YOimoPpC4utDpcU002"    //  currentUser.uid


    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,

        modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),

        backgroundColor = GREY,
//        border = BorderStroke(5.dp, Color.Red),

        contentColor = Color.Black,

        ) {
        Column(Modifier.background(color = LIGHT_GREY)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.5.dp)
                    .padding(15.dp)

            )
            {
                Image(
                    painter = rememberImagePainter(
                        data = "https://firebasestorage.googleapis.com/v0/b/wecon-jetpackcompose.appspot.com/o/images%2F${uid}?alt=media&token=c92e2cbd-3ce9-4e82-a512-bbbaaece4058"
                    ),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                )

                Spacer(Modifier.width(20.dp))
                Text(
                    text = name!!.capitalize(),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = YELLOW,
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp)

                )

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
                                if (!email.isNullOrEmpty()) {
                                    isDialogOpen.value = false

                                    // add in output here



                                    //
                                    Toast.makeText(
                                        context,
                                        "Contact Added!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else
                                {
                                    Toast.makeText(
                                        context,
                                        "Please Complete the Fields",
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
                                .padding(10.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = YELLOW)
                        ) {
                            Text(
                                text = "Add",
                                color = GREY,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

    }









//    fun getUsersListFromFirebase ()
//    {
//        firebaseDBReference.child("user").addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                // snapshot use to get database from db
//
//                // clearing any previous list
//                userList.clear()
//                for (snap in snapshot.children) {
//                    val user = snap.getValue(User::class.java)
//                    // add we dont want to message our self xD
//                    if (auth.currentUser?.uid != user?.uid)
//                        userList.add(user!!)
//
//                }
//
//
////                    Log.i("Custom Note", "** Data Set Update **" )
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//
//    }






}
