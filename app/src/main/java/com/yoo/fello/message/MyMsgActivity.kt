package com.yoo.fello.message

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yoo.fello.R
import com.yoo.fello.utils.FirebaseAuthUtils
import com.yoo.fello.utils.FirebaseRef

class MyMsgActivity : AppCompatActivity() {

    private val TAG = "MyMsgActivity"

    lateinit var listviewAdapter: MsgAdapter
    val msgList = mutableListOf<MsgModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_msg)

        val listview = findViewById<ListView>(R.id.msgListView)

        listviewAdapter = MsgAdapter(this, msgList)
        listview.adapter = listviewAdapter

        getMyMsg()

    }



    private fun getMyMsg(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                msgList.clear()

                for (dataModel in dataSnapshot.children) {

                    val msg = dataModel.getValue(MsgModel::class.java)
                    msgList.add(msg!!)
                    Log.d(TAG, msg.toString())


                }
                msgList.reverse()
                listviewAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.userMsgRef.child(FirebaseAuthUtils.getUid()).addValueEventListener(postListener)

    }

}