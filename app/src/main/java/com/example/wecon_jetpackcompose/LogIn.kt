package com.example.wecon_jetpackcompose

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wecon_jetpackcompose.ui.theme.WeCon_JetpackComposeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import coil.compose.rememberImagePainter

class LogIn : ComponentActivity() {

    // firebase auth variable
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()


        val user = auth.currentUser


//        if (user != null) {
//            // User is signed in
//            Log.i("CUSTOM INFO"  , "** USER IS ALREADY LOGGED IN : ${user.uid} **")
//        } else {
//            // No user is signed in
//            Log.i("CUSTOM INFO"  , "** USER NOT LOGGED IN **")
//
//        }


        setContent {
            WeCon_JetpackComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    if (user != null) {
                        // User is signed in
                        Log.i("-> CUSTOM INFO"  , "** USER IS ALREADY LOGGED IN : ${user.uid} **")

                    // redirect to main screen
                        Toast.makeText(this@LogIn , "Welcome Back :)" ,Toast.LENGTH_SHORT ).show()

                        // setting profile image
                        val intent1 = Intent(this@LogIn, MainActivity::class.java)
                        //  finishAffinity()  // to clear previous stack of the activities
                        startActivity(intent1)


                    } else {
                        // No user is signed in
                        Log.i("CUSTOM INFO"  , "** USER NOT LOGGED IN **")

                        LoginScreen()

                    }
                }
            }
        }
    }


    @Composable
    private fun LoginScreen() {

        // Fetching the Local Context
//        val mContext = LocalContext.current
//        val activity = mContext as Activity
        // data variables
        var email: String by remember { mutableStateOf("") }
        var password: String by remember { mutableStateOf("") }
        // firebase auth variable
// firebase - initialization
//    auth = FirebaseAuth.getInstance()


        // actual body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = GREY)
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            Image(
                painter = painterResource(id = R.drawable.wecon_rounded),

//                painter = rememberImagePainter(
//                    data = "https://firebasestorage.googleapis.com/v0/b/wecon-jetpackcompose.appspot.com/o/images%2F${uid}?alt=media&token=c92e2cbd-3ce9-4e82-a512-bbbaaece4058"
//                ),
                contentDescription = "App Logo",
//        contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)

            )
            Spacer(Modifier.height(100.dp))
            OutlinedTextField(
                value = email,

                onValueChange = { email = it },
                label = { androidx.compose.material3.Text(text = "Email", color = Color.White) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = { androidx.compose.material3.Text(text = "Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = LIGHT_GREY,
                    textColor = YELLOW,
                    focusedBorderColor = YELLOW,
                    cursorColor = YELLOW,
                    placeholderColor = LIGHT_GREY
                )
            )
            Spacer(Modifier.height(25.dp))
            // password box
            OutlinedTextField(
                value = password,

                onValueChange = { password = it },
                label = { androidx.compose.material3.Text(text = "Password", color = Color.White) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                placeholder = { androidx.compose.material3.Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = LIGHT_GREY,
                    textColor = YELLOW,
                    focusedBorderColor = YELLOW,
                    cursorColor = YELLOW,
                    placeholderColor = LIGHT_GREY
                )
            )

            Spacer(Modifier.height(45.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    color = YELLOW,

                    modifier = Modifier
                        .clickable {
                            var intent = Intent(this@LogIn, SignUp::class.java)
                            finishAffinity()
                            startActivity(intent)

                        },
                    text = "SignUp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold

                )

                Spacer(Modifier.width(125.dp))

                Button(
                    onClick = {


                        // function to login
                        login(email, password)


                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = YELLOW)
                ) {
                    androidx.compose.material3.Text(
                        text = "LogIn",
                        color = GREY,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }

    private fun login(email: String, password: String)
    {
        // Fetching the Local Context
        if (email.isNullOrEmpty() || password.isNullOrEmpty())
        {

            Toast.makeText(
                this@LogIn,
                "Please Fill All Fields!",
                Toast.LENGTH_SHORT
            ).show()

        }
        else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LogIn) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@LogIn, "Welcome Back :)", Toast.LENGTH_SHORT)
                            .show()

                        val intent = Intent(this@LogIn, MainActivity::class.java)
//                    finishAffinity()  // to clear previous stack of the activities
//                finish() // destroying previous activity

//                                    intent.flags =
//                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                        finish()

                        this@LogIn.startActivity(intent)
                        Log.i("Custom Note", "** Login Success **")

                    } else {
                        // If login in fails, display a message to the user.
                        Toast.makeText(
                            this@LogIn,
                            "LogIn Unsuccessful :( May be Email or password is incorrect or User not exits.",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                }

        }

    }


}

