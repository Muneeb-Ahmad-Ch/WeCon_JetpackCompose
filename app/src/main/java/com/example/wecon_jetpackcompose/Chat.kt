package com.example.wecon_jetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wecon_jetpackcompose.ui.theme.WeCon_JetpackComposeTheme
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.*
import com.google.firebase.auth.FirebaseAuth
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.database.*
import java.time.format.DateTimeFormatter
import androidx.compose.material.IconButton
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime

fun getCurrentDate (): String? {
    // for higher api
//    val current = LocalDateTime.now()
//    val formatter = DateTimeFormatter.ofPattern("hh:mm a dd/MM/yy")
//    val formatted = current.format(formatter)
//    println("Current Date and Time is: $formatted")
    val sdf = SimpleDateFormat("hh:mm a dd/MM/yy")
    val currentDate = sdf.format(Date())
    Log.e("OUTPUT INFO" , "Current Date: ${currentDate.toString()}")

    return currentDate
}

fun getUserByUid (uid : String ) : MutableState<User>
{
    val firebaseDBReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("user")
    var currentUser  = mutableStateOf<User>(User(":)", null, null , null  , mutableListOf<String>()))
    // Read current user from the database
    firebaseDBReference.child(uid!!).addValueEventListener(object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.

            currentUser.value  = snapshot.getValue(User::class.java)!!
            Log.i(
                "** Custom Log Message ~ getUserUid($uid) **",
                "Value is: " + currentUser.value.email + currentUser.value.uid + currentUser.value.name + currentUser.value.contacts
            )
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("** Custom Log Message ~ getUserUid($uid)  **", "Failed to read value.")
        }

    })
    return currentUser
}

class Chat : ComponentActivity() {

    // firebase auth variable
    private lateinit var auth: FirebaseAuth
    // sender is our current user
    var senderUser  = mutableStateOf<User>(User("Sender", null, null , null  , mutableListOf<String>()))
    var receiverUser  = mutableStateOf<User>(User("Receiver", null, null , null  , mutableListOf<String>()))
    private lateinit var firebaseDBReference : DatabaseReference
//    private lateinit var receiverUid : String
//
//    private lateinit var senderUid : String
var receiverRoom : String?  =null
    var senderRoom : String? = null
    val messages =   mutableStateListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var receiverUid = intent.getStringExtra("uid")!!
        Log.d(
            "* Custom Log *",
            " --> Receiver Uid:  $receiverUid "
        )
        auth = FirebaseAuth.getInstance()

        var senderUid = auth.currentUser?.uid!!

        senderUser = getUserByUid(senderUid!!)
        receiverUser = getUserByUid(receiverUid!!)

        // we will create unique room for chat purpose

        Log.i("** Custom Log Message **", "OnCreate of Chat() Is called [senderUid: $senderUid -  receiverUid: $receiverUid]")

        //setting up room

        senderRoom = receiverUid + senderUid
        receiverRoom =   senderUid + receiverUid

        firebaseDBReference = FirebaseDatabase.getInstance().getReference()
        // showing messages to the recyclerview from the firebase
        firebaseDBReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear() // to safe list from duplication
                    for (snap in snapshot.children)
                    {
                        val message = snap.getValue(Message::class.java)
                        messages.add(message!!)
                    }
//                    messages.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        // ------------------------------------------------

        //-------------------------------------------------
        setContent {
            WeCon_JetpackComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatUI()
                }
            }
        }
    }


    @Composable
    fun ChatUI ()
    {
        val messages = remember { messages}
        Scaffold(
            backgroundColor = GREY,
            topBar = {

                TopAppBar(
                    backgroundColor = YELLOW,

                    title = {
                        Text(text = receiverUser.value.name!!, color = GREY , fontSize = 20.sp,
                            fontWeight = FontWeight.Bold)
                    },

                )
            },
            content = {
                AllCards(messages )
            },
        )
    }


    @Composable
    fun AllCards (msgs: SnapshotStateList<Message>)
    {
//        var sender = User("muneeb", "muneeb@gmail.com", "", "")
//        var msg = Message("My Name is Muneeb and I am the student of this university.", sender, "12:44am 12-3-21")
//        var msg2 = Message("Start - My Name is Muneeb and I am the student of this university.", sender, "12:44am 12-3-21")
//
//        var msgs  = mutableListOf<Message>(msg2,msg,msg,msg,msg  , msg , msg,msg,msg,msg,msg,msg  , msg , msg,msg,msg,msg,msg,msg  , msg , msg,msg,msg,msg,msg,msg  , msg , msg)



        var chat_text by remember { mutableStateOf("") }

        Column (
            Modifier.fillMaxSize()
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

//        horizontalAlignment = Alignment.,
//            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 100.dp),
//            verticalArrangement = Arrangement.spacedBy(.dp)
            ) {
                //  Add a Multiple Items


                itemsIndexed(msgs.asReversed()) { index, msg ->
                    if (msg.sender?.uid == senderUser.value.uid )
                        SentMessageCard(msg)
                    else
                        ReceivedMessageCard(msg)

                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)

            ) {
                Spacer(modifier = Modifier.width(4.dp))

                TextField(
                    value = chat_text,
                    onValueChange = {
                        chat_text = it
                    },

                    modifier = Modifier.border(
                        BorderStroke(width = 1.5.dp, color = YELLOW),
                        shape = RoundedCornerShape(40)
                    ).weight(9.5f),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = YELLOW,
                        textColor = YELLOW
                    ),
//           textStyle = inputTextStyle
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(onClick = { /*TODO*/ },
                    modifier= Modifier.size(50.dp),  //avoid the oval shape
//               shape = CircleShape,
//               border= BorderStroke(1.dp, YELLOW),
//               contentPadding = PaddingValues(0.dp),  //avoid the little icon
//               colors = ButtonDefaults.outlinedButtonColors(contentColor =  GREY)
                    backgroundColor = YELLOW
                ) {
                    IconButton(  onClick = {
                        var sUser  = User(senderUser.value.name , senderUser.value.email ,senderUser.value.uid , senderUser.value.profile_pic )
                        val msg_obj = Message(chat_text, sUser , getCurrentDate() )
                        firebaseDBReference = FirebaseDatabase.getInstance().getReference()
                        firebaseDBReference.child("chats").child(senderRoom!!).child("messages").push().setValue(msg_obj).addOnSuccessListener {
                            firebaseDBReference.child("chats").child(receiverRoom!!)
                                .child("messages").push().setValue(msg_obj)
                            Log.i("* Custom Msg : $chat_text *", "Message Send Btn Clicked")
                            chat_text = "" // clearing box
                        }
                    }
                        ) {
                        Icon(
                            painterResource(R.drawable.send),
                            modifier = Modifier.size(25.dp),
                            tint = GREY,
                            contentDescription = "content description"
                        )
                    }
                    }
                            Spacer(modifier = Modifier.width(8.dp))

                }

            }



        }



        @Composable
        fun SentMessageCard(msg : Message) {
//    var sender = User("muneeb", "muneeb@gmail.com", "", "")
//    var msg = Message("This is a book", sender, "12:44am 12-3-21")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalAlignment = Alignment.End,

                ) {

                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = 2.dp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
//        backgroundColor = GREY,
                    border = BorderStroke(1.dp, YELLOW),


                    contentColor = Color.Black,
                    backgroundColor = YELLOW
                ) {

//        Row(modifier = Modifier.padding(all = 8.dp)) {
// Set pictures
//            Image(
//                painter = painterResource(R.drawable.chat),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(25.dp)
//                    .clip(CircleShape)
//                    .border(1.5.dp, GREY)
//            )// Set the spacing
//            Spacer(modifier = Modifier.width(8.dp))

                    // We keep track if the message is expanded or not in this
                    // variable  Whether the message is animated
//            var isExpanded by remember { mutableStateOf(false) }

                    // We toggle the isExpanded variable when we click on this Column
//        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        // Set text properties
                        Text(
                            text = msg.message!!,
                            color = GREY,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(4.dp))

//                Surface(
////                shape = MaterialTheme.shapes.medium,
//                    elevation = 1.dp,
//                    color = GREY
//                ) {
                        Text(
                            text = msg.datetime!!,
                            modifier = Modifier
                                .padding(top = 1.dp, start = 20.dp)
                                .align(Alignment.End),
                            // If the message is expanded, we display all its content
                            // otherwise we only display the first line
//                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            color = LIGHT_GREY,
                            fontSize = 8.sp

                        )
//                }
                    }
                }
//    }
            }

        }


        @Composable
        fun ReceivedMessageCard(msg : Message) {
//    var sender = User("muneeb", "muneeb@gmail.com", "", "")
//    var msg = Message("This is a book", sender, "12:44am 12-3-21")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp),
                horizontalAlignment = Alignment.Start,

                ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = 2.dp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
//        backgroundColor = GREY,
                    border = BorderStroke(1.dp, YELLOW),


                    contentColor = Color.Black,
                    backgroundColor = GREY
                ) {

//        Row(modifier = Modifier.padding(all = 8.dp)) {
// Set pictures
//            Image(
//                painter = painterResource(R.drawable.chat),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(25.dp)
//                    .clip(CircleShape)
//                    .border(1.5.dp, GREY)
//            )// Set the spacing
//            Spacer(modifier = Modifier.width(8.dp))

                    // We keep track if the message is expanded or not in this
                    // variable  Whether the message is animated
//            var isExpanded by remember { mutableStateOf(false) }

                    // We toggle the isExpanded variable when we click on this Column
//        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        // Set text properties
                        Text(
                            text = msg.message!!,
                            color = YELLOW,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(4.dp))

//                Surface(
////                shape = MaterialTheme.shapes.medium,
//                    elevation = 1.dp,
//                    color = GREY
//                ) {
                        Text(
                            text = msg.datetime!!,
                            modifier = Modifier
                                .padding(top = 1.dp, start = 20.dp)
                                .align(Alignment.End),
                            // If the message is expanded, we display all its content
                            // otherwise we only display the first line
//                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            color = LEMON_YELLOW,
                            fontSize = 8.sp

                        )
//                }
                    }
                }
//    }
            }
        }
    }





