package com.example.wecon_jetpackcompose

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.wecon_jetpackcompose.ui.theme.TakeCameraPictureTheme
import com.example.wecon_jetpackcompose.ui.theme.ui.theme.WeCon_JetpackComposeTheme
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.launch


class AddProfileImage : ComponentActivity() {

    var isCameraSelected = false
    var imageUri: Uri? = null
    var bitmap: Bitmap? = null
    // Create a storage reference from our app

    var storageReference: StorageReference? = null
     lateinit var firebaseRefrence : DatabaseReference

//var uid = "wkKDavKJAORNdpcESx5lleawd263"

    lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        uid = intent.getStringExtra("uid")// we will save image with the name of uri
        // firebase stuff
        auth = FirebaseAuth.getInstance()
        // Firebase database
        firebaseRefrence = FirebaseDatabase.getInstance().getReference()
        setContent {
            WeCon_JetpackComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TakePicture(bitmap  )
                }
            }
        }
    }


//    @Preview(showBackground = true)
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun TakePicture(bitmap: Bitmap? ) {
        val context = LocalContext.current
        val bottomSheetModalState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val coroutineScope = rememberCoroutineScope()

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            this.imageUri = uri
            this.bitmap = null
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { btm: Bitmap? ->
            this.bitmap = btm
            this.imageUri = null
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (isCameraSelected) {
                    cameraLauncher.launch()
                } else {
                    galleryLauncher.launch("image/*")
                }
                coroutineScope.launch {
                    bottomSheetModalState.hide()
                }
            } else {
                Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }

        ModalBottomSheetLayout(
            sheetBackgroundColor = GREY,
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colors.primary.copy(0.08f))
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Set Profile!",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Divider(
                            modifier = Modifier
                                .height(1.dp)
                                .background(YELLOW)
                        )
                        Text(
                            text = "Take Photo",
                            modifier = Modifier

                                .fillMaxWidth()
                                .clickable {
                                    when (PackageManager.PERMISSION_GRANTED) {
                                        ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.CAMERA
                                        ) -> {
                                            cameraLauncher.launch()
                                            coroutineScope.launch {
                                                bottomSheetModalState.hide()
                                            }
                                        }
                                        else -> {
                                            isCameraSelected = true
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                }
                                .padding(15.dp),
                            color = YELLOW,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Divider(
                            modifier = Modifier
                                .height(0.5.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray)
                        )
                        Text(
                            text = "Choose from Gallery",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (PackageManager.PERMISSION_GRANTED) {
                                        ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) -> {
                                            galleryLauncher.launch("image/*")
                                            coroutineScope.launch {
                                                bottomSheetModalState.hide()
                                            }
                                        }
                                        else -> {
                                            isCameraSelected = false
                                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                        }
                                    }
                                }
                                .padding(15.dp),
                            color = YELLOW,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Divider(
                            modifier = Modifier
                                .height(0.5.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray)
                        )
                        Text(
                            text = "Cancel",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        bottomSheetModalState.hide()
                                    }
                                }
                                .padding(15.dp),
                            color = BUTTER_YELLOW,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            },
            sheetState = bottomSheetModalState,
            sheetShape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            modifier = Modifier
                .background(GREY)
        ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .fillMaxHeight(1f)
                .background(color = GREY)
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Spacer(Modifier.height(270.dp))
               Button(
                    onClick = {
                        coroutineScope.launch {
                            if (!bottomSheetModalState.isVisible) {
                                bottomSheetModalState.show()
                            } else {
                                bottomSheetModalState.hide()
                            }
                        }
                    },
                    modifier = Modifier

                        .padding(top = 100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = YELLOW)
                ) {
                    Text(
                        text = "Set Profile Picture",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = GREY,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

            Spacer(Modifier.height(70.dp))

                Button(
                    onClick = {

                              // to upload image to the firebase cloud
                        if (imageUri != null) {

                                uploadImage(imageUri )

                            }


                    },
                    modifier = Modifier

                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = YELLOW)
                ) {
                    Text(
                        text = "Done",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = GREY,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }


        imageUri?.let {
            if (!isCameraSelected) {
                this.bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
            }

            this.bitmap?.let { btm ->
                Column(
                    modifier = Modifier
                        .padding(40.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Spacer(Modifier.height(70.dp))

                    Image(
                        bitmap = btm.asImageBitmap(),

                        contentDescription = "profile image",
                        alignment = Alignment.BottomCenter,

                        modifier = Modifier
                            .padding(10.dp)
                            .size(190.dp)
                            .clip(CircleShape),

                        contentScale = ContentScale.Crop,

                        )
                }
            }
        }

        bitmap?.let { btm ->
            Column(
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Spacer(Modifier.height(70.dp))

                Image(
                bitmap = btm.asImageBitmap(),
                contentDescription = "profile image",

                contentScale = ContentScale.Crop,
                alignment  = Alignment.BottomCenter ,

                modifier = Modifier

                    .padding(10.dp)
                    .size(190.dp)
                    .clip(CircleShape),

            )
        }}
        }

    Log.i("Custom Note", "** $bitmap  |  $imageUri  **")
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setContent {
                // present in Theme (ui.theme)
                TakeCameraPictureTheme {
                    Surface(color = Color.Gray) {

                            TakePicture(bitmap)




                    }
                }
            }
        }
    }

    private fun uploadImage(imageUri: Uri? ) {
        val uid : String? = auth.currentUser?.uid
        // hwe we can use loading screen too
        if (uid.isNullOrEmpty())
        {
            Toast.makeText(this@AddProfileImage, "Failed to Upload : UID is null", Toast.LENGTH_SHORT).show()
            return
        }

//        val fileName: String =imageUri.toString()
        storageReference = FirebaseStorage.getInstance().getReference("images/$uid")


        if (imageUri != null) {
            storageReference!!.putFile(imageUri)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot?> {
//                    binding.firebaseimage.setImageURI(null)
                    Toast.makeText(this@AddProfileImage, "Your Profile is Successfully Created. Now Its time to Login :)", Toast.LENGTH_LONG)
                        .show()

                    firebaseRefrence.child("user").child(uid).child("profile_pic").setValue("https://firebasestorage.googleapis.com/v0/b/wecon-jetpackcompose.appspot.com/o/images%2F${uid}?alt=media&token=c92e2cbd-3ce9-4e82-a512-bbbaaece4058")

                    val intent1 = Intent(this, LogIn::class.java)
                      finishAffinity()  // to clear previous stack of the activities
                    startActivity(intent1)

                }).addOnFailureListener(OnFailureListener {

                    Toast.makeText(this@AddProfileImage, "Failed to Upload", Toast.LENGTH_SHORT).show()
                })
        }
    }
}


