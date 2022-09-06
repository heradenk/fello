package com.yoo.fello.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yoo.fello.message.MyLikeListActivity
import com.yoo.fello.R
import com.yoo.fello.auth.IntroActivity
import com.yoo.fello.message.MyMsgActivity

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val mybtn = findViewById<Button>(R.id.myPageBtn)
        mybtn.setOnClickListener {

            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)

        }

        val myLikeBtn = findViewById<Button>(R.id.myLikeList)
        myLikeBtn.setOnClickListener {

            val intent = Intent(this, MyLikeListActivity::class.java)
            startActivity(intent)

        }

        val myMsg = findViewById<Button>(R.id.myMsg)
        myMsg.setOnClickListener {

            val intent = Intent(this, MyMsgActivity::class.java)
            startActivity(intent)

        }

        val logoutbtn = findViewById<Button>(R.id.logoutBtn)
        logoutbtn.setOnClickListener {

            val auth = Firebase.auth
            auth.signOut()

            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)


        }
    }
}