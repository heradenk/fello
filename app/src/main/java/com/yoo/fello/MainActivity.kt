package com.yoo.fello

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoo.fello.auth.UserDataModel
import com.yoo.fello.setting.SettingActivity
import com.yoo.fello.slider.CardStackAdapter
import com.yoo.fello.utils.FirebaseAuthUtils
import com.yoo.fello.utils.FirebaseRef
import com.yoo.fello.utils.MyInfo
import com.yuyakaido.android.cardstackview.*


class MainActivity : AppCompatActivity() {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager: CardStackLayoutManager

    private val TAG = "MainActivity"

    private val usersDataList = mutableListOf<UserDataModel>()

    private var userCount = 0

    private lateinit var currentUserGender : String

    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserListUid = mutableListOf<String>()
    private val likeUserList = mutableListOf<UserDataModel>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //로직: 내가 좋아요한 사람이 좋아요한 사람의 리스트 -> 내가 있는지 확인


        val setting = findViewById<ImageView>(R.id.settingIcon)
        setting.setOnClickListener {

            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)

        }



        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)



        manager = CardStackLayoutManager(baseContext, object : CardStackListener {
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {

                if (direction == Direction.Right) {
//                    Toast.makeText(this@MainActivity, "right", Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, usersDataList[userCount].uid.toString())

                    userLikeOtherUser(uid,usersDataList[userCount].uid.toString())

                }

                if (direction == Direction.Left) {
//                    Toast.makeText(this@MainActivity, "left", Toast.LENGTH_SHORT).show()
                }

                userCount = userCount + 1

                if (userCount == usersDataList.count()) {
                    getUserDataList(currentUserGender)

                    Toast.makeText(this@MainActivity, "유저 새롭게 받아옵니다", Toast.LENGTH_SHORT).show()
                }


            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }


        })

        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter = cardStackAdapter

//        getUserDataList()
        getMyUserData()

    }


    // 내 정보 불러오기 (MypageActivity 참고)
    private fun getMyUserData() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                Log.d(TAG, data?.gender.toString())

                currentUserGender = data?.gender.toString()


                MyInfo.myNickname = data?.nickname.toString()

                getUserDataList(currentUserGender)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)

    }


    // 만약에 uid가 내 likeList에 있다면 1. Datalist에서 제외하고 List 전부 보여주기 2.likeList에 없는 것만 보여주기
    private fun getUserDataList(currentUserGender: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)
                    var liked = user!!.uid.toString()

                    // 성별 같은 경우 제외
                    if (user!!.gender.toString().equals(currentUserGender)) {
                    }


                    else { usersDataList.add(user!!)
                    }

//                    if (uid in likeUserListUid) {
//                        usersDataList.removeAt(uid.toString())
//                    }

                }

                        cardStackAdapter.notifyDataSetChanged()

                    }


            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }


    // 유저의 좋아요를 표시하는 부분
    // 나의 uid, 내가 좋아하는 사람의 uid
    private fun userLikeOtherUser(myUid : String, otherUid : String){

        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("true")

        getOtherUserLikeList(otherUid)

    }

    // 내가 좋아요한 사람의 좋아요 리스트
    private fun getOtherUserLikeList(otherUid: String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //리스트에 나의 UID가 있는지 확인
                for (dataModel in dataSnapshot.children) {


                    val likeUserKey = dataModel.key.toString()
                    if(likeUserKey.equals(uid)){
                        Toast.makeText(this@MainActivity, "매칭완료", Toast.LENGTH_SHORT).show()

                        // 상대방을 내 카드에서 제외




                        createNotificationChannel()
                        sendNotification()
                    }

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)

    }


    //Notification
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Test_Channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification(){
    var builder = NotificationCompat.Builder(this, "Test_Channel")
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setContentTitle("매칭완료")
        .setContentText("매칭이 완료되었습니다")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    with(NotificationManagerCompat.from(this)){
        notify(123, builder.build())
    }
    }

}
