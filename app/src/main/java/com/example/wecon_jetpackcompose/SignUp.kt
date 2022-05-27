package com.example.wecon_jetpackcompose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.wecon_jetpackcompose.ui.theme.WeCon_JetpackComposeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : ComponentActivity() {
    // firebase auth variable
    private lateinit var auth : FirebaseAuth
    // firebase database
    private lateinit var firebaseDBReference : DatabaseReference
    
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // firebase - initialization
        auth = FirebaseAuth.getInstance()

        setContent {
            WeCon_JetpackComposeTheme {
                SignupScreen()
            }
        }
    }
    @Composable
    private fun SignupScreen() {

        // data variables
        var email :String by remember { mutableStateOf("") }
        var name :String by remember { mutableStateOf("") }

        var password :String by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = GREY)
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally)
        {

            Image(
                painter = painterResource(id = R.drawable.wecon_rounded),
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
                    unfocusedBorderColor = LIGHT_GREY, textColor = YELLOW , focusedBorderColor = YELLOW , cursorColor = YELLOW , placeholderColor = LIGHT_GREY
                )
            )
            Spacer(Modifier.height(25.dp))
            OutlinedTextField(
                value = name,

                onValueChange = { name = it },
                label = { androidx.compose.material3.Text(text = "Name", color = Color.White) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                placeholder = { androidx.compose.material3.Text(text = "Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = LIGHT_GREY, textColor = YELLOW , focusedBorderColor = YELLOW , cursorColor = YELLOW , placeholderColor = LIGHT_GREY
                )
            )
            // password box
            Spacer(Modifier.height(25.dp))
            OutlinedTextField(
                value = password,

                onValueChange = { password = it },
                label = { androidx.compose.material3.Text(text = "Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                placeholder = { androidx.compose.material3.Text(text = "Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = LIGHT_GREY, textColor = YELLOW , focusedBorderColor = YELLOW, cursorColor = YELLOW , placeholderColor = LIGHT_GREY
                )
            )

            Spacer(Modifier.height(45.dp))


            Row(  modifier = Modifier
                .fillMaxWidth()) {
                androidx.compose.material3.Text(
                    color = YELLOW,

                    modifier = Modifier
                        .clickable {
                            // Fetching the Local Context
                            var intent = Intent(this@SignUp, LogIn::class.java)
                            finishAffinity()
                            startActivity(intent)
                        },
                    text = "Log In",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold

                )

                Spacer(Modifier.width(105.dp))

                Button(onClick = {


                    signup  (name,  email , password )


                },
                    colors = ButtonDefaults.buttonColors(backgroundColor = YELLOW)
                ) {
                    androidx.compose.material3.Text(
                        text = "Sign Up",
                        color = GREY,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }


    // take care of creating new account stuff in the  backend
    private fun signup  (  username: String , email: String ,  password : String )
    {




        if (email.isNullOrEmpty() || password.isNullOrEmpty() || username.isNullOrEmpty())
        {
            Toast.makeText(
                this@SignUp,
                "Please Fill All Fields!",
                Toast.LENGTH_SHORT
            ).show()

            return
        }



        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // after a successful creation of account we need add the user
                    // to database too so that we can access it info



                    addUserToDB (username , email , auth.currentUser?.uid!!)


//                    // redirect to main screen
                    Toast.makeText(this@SignUp , "Account Created :) Now Its time to add your Profile!" ,Toast.LENGTH_LONG ).show()
//
//                    val intent2 = Intent(this, LogIn::class.java)
//                    //  finishAffinity()  // to clear previous stack of the activities
//                    startActivity(intent2)


                    // setting profile image
                    val intent1 = Intent(this, AddProfileImage::class.java)
                    //  finishAffinity()  // to clear previous stack of the activities
//                    val tempUid : String  = auth.currentUser?.uid!!
//                    intent.putExtra("uid" , tempUid)
                    startActivity(intent1)


                } else {
                    // show an error message that creating account is not successful

                    Toast.makeText(this@SignUp , "SignUp Unsuccessful :(" ,Toast.LENGTH_SHORT ).show()

                }
            }

    }


    // to add user to database
    private fun addUserToDB(name: String , email:String , userid: String){
        // passing url because not using default server of firebase (us) instead using singapore server aka asia server
        firebaseDBReference = FirebaseDatabase.getInstance().getReference()

        // creating of node for current user in the firebase database
        // this will add user to the database
        firebaseDBReference.child("user").child(userid).setValue(User(name ,email,userid , null ))

        Log.i("Custom Note", "** The User Has been added to DB **" )

    }
}



