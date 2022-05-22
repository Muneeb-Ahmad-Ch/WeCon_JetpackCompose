package com.example.wecon_jetpackcompose

// user holds following parameters
// name , email , user id as uid , profile pic link (uploaded to firebase storage) , contacts (which is basically the list of users he chat)
class User(var name: String?, var email: String?, var uid: String?, var profile_pic: String?, var contacts: ArrayList<String>?)

{
    constructor() : this(null, null, null , null  , null) // this is important to add in because if we use this in

}