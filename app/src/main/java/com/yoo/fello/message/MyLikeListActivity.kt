package com.yoo.fello.message

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoo.fello.R
import com.yoo.fello.auth.UserDataModel
import com.yoo.fello.message.ListViewAdapter
import com.yoo.fello.message.MsgModel
import com.yoo.fello.message.fcm.NotiModel
import com.yoo.fello.message.fcm.PushNotification
import com.yoo.fello.message.fcm.RetrofitInstance
import com.yoo.fello.utils.FirebaseAuthUtils
import com.yoo.fello.utils.FirebaseRef
import com.yoo.fello.utils.MyInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// 매칭 리스트
class MyLikeListActivity : AppCompatActivity() {

    private val TAG = "MyLikeListActivity"
    private val uid = FirebaseAuthUtils.getUid()

    private val likeUserListUid = mutableListOf<String>()
    private val likeUserList = mutableListOf<UserDataModel>()

    lateinit var listViewAdapter: ListViewAdapter
    lateinit var getterUid : String
    lateinit var getterToken : String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)


        val userListView = findViewById<ListView>(R.id.userListView)

        listViewAdapter = ListViewAdapter(this, likeUserList)
        userListView.adapter = listViewAdapter

//        getUserDataList()

        // 전체 유저 받아오기

        getMyLikeList()

        // 내가 좋아요한 유저 가져와서 나와의 매칭을 확인

//        userListView.setOnItemClickListener { parent, view, position, id ->
//
////            Log.d(TAG, likeUserList[position].uid.toString())
//            checkMatching(likeUserList[position].uid.toString())
//
//            val notiModel = NotiModel("a", "b")
//
//            val pushModel = PushNotification(notiModel, likeUserList[position].token.toString())
//
//            testPush(pushModel)
//        }


        //꾹 누르기(Long Click)
        userListView.setOnItemLongClickListener{ parent, view, position, id ->

        checkMatching(likeUserList[position].uid.toString())
        getterUid = likeUserList[position].uid.toString()
        getterToken = likeUserList[position].token.toString()

        return@setOnItemLongClickListener(true)

        }

        // 매칭한 유저를 클릭하면 메세지 보낼 수 있음(Long CLick)
        // 메시지 창 구성
        // 메시지 PUSH




    }

    private fun checkMatching(otherUid: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.d(TAG, otherUid)
                Log.e(TAG, dataSnapshot.toString())

                if (dataSnapshot.children.count() == 0) {
                    Toast.makeText(this@MyLikeListActivity, "상대방이 좋아요한 사람이 아무도 없습니다", Toast.LENGTH_SHORT).show()
                } else {

                    for (dataModel in dataSnapshot.children) {
                        val likeUserKey = dataModel.key.toString()
                        if (likeUserKey.equals(uid)) {

                            Toast.makeText(this@MyLikeListActivity, "매칭이 되었습니다", Toast.LENGTH_SHORT).show()
                            showDialog()



                        } else {
//                            Toast.makeText(this@MyLikeListActivity, "매칭이 안 되었습니다", Toast.LENGTH_SHORT).show()
                    }
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



    private fun getMyLikeList(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {
//                    Log.d(TAG, dataModel.key.toString())

                    // likeUserList에는 내가 좋아요한 사람들이 들어있음
                    likeUserListUid.add(dataModel.key.toString())
                }

                getUserDataList()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)

    }

    private fun getUserDataList() {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children) {

                    val user = dataModel.getValue(UserDataModel::class.java)

                    // 내가 좋아요한 사람 add
                    if(likeUserListUid.contains(user?.uid)){

                        likeUserList.add(user!!)
                }

            }
            listViewAdapter.notifyDataSetChanged()
            Log.d(TAG, likeUserList.toString())
        }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userInfoRef.addValueEventListener(postListener)

    }

    //Push
    private fun testPush(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch {

        RetrofitInstance.api.postNotification(notification)
    }


    // Dialog
    private fun showDialog(){

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("메시지 보내기")

        val mAlertDialog = mBuilder.show()

        val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
        val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)
        btn?.setOnClickListener {

            val msgText = textArea!!.text.toString()

            val msgModel = MsgModel(MyInfo.myNickname, msgText)

            FirebaseRef.userMsgRef.child(getterUid).push().setValue(msgModel)

            val notiModel = NotiModel(MyInfo.myNickname,msgText )

            val pushModel = PushNotification(notiModel, getterToken)

            testPush(pushModel)

            mAlertDialog.dismiss()
        }


        // message
        //받는 사람 uid
        //Message 내용
        // 보낸 사람
    }



}
