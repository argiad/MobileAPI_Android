package com.brightpattern

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brightpattern.bpcontactcenter.ContactCenterCommunicator
import com.brightpattern.bpcontactcenter.entity.ContactCenterEvent
import com.brightpattern.bpcontactcenter.interfaces.ContactCenterEventsInterface
import com.brightpattern.bpcontactcenter.model.ContactCenterChatSessionProperties
import com.brightpattern.bpcontactcenter.utils.Failure
import com.brightpattern.bpcontactcenter.utils.Result
import com.brightpattern.bpcontactcenter.utils.Success
import com.brightpattern.chatdemo.R
import com.brightpattern.recyclerview.FunctionsListAdapter
import org.json.JSONObject
import java.util.*

@SuppressLint("SetTextI18n")
class TestActivity : AppCompatActivity() {

    val api: ContactCenterCommunicator by lazy {
        ChatDemo.api
    }

    private val adapter = FunctionsListAdapter()
    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.rv)
    }
    private val tvResult: TextView by lazy {
        findViewById(R.id.tvResult)
    }

    private var chatID: String = ""
    private var partyID: String = ""
    private var lastMessageID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.selection = { it ->
            Log.e("FB", "${ChatDemo.gcmToken}")
            when (it) {
                "checkAvailability" -> api.checkAvailability { r -> resultProcessing(r) }
                "requestChat" -> api.requestChat("555-555-5555", "Someone", JSONObject()) { r -> resultProcessing(r) }
                "getChatHistory" -> api.getChatHistory(chatID) { r -> resultProcessing(r) }
                "getCaseHistory" -> api.getCaseHistory(chatID) { r -> resultProcessing(r) }
                "sendChatMessage" -> api.sendChatMessage(chatID, "MY MESSAGE") { r -> resultProcessing(r) }
                "subscribeForRemoteNotificationsFirebase" -> api.subscribeForRemoteNotificationsFirebase(chatID, ChatDemo.gcmToken ?: "unknown") { r -> resultProcessing(r) }
                "subscribeForRemoteNotificationsAPNs" -> api.subscribeForRemoteNotificationsAPNs(chatID, ChatDemo.gcmToken ?: "unknown") { r -> resultProcessing(r) }
                "chatMessageDelivered" -> api.chatMessageDelivered(chatID,lastMessageID){ r -> resultProcessing(r) }
                "chatMessageRead" -> api.chatMessageRead(chatID,lastMessageID){ r -> resultProcessing(r) }
                "chatTyping" -> api.chatTyping(chatID){ r -> resultProcessing(r) }
                "chatNotTyping" -> api.chatNotTyping(chatID){ r -> resultProcessing(r) }
                "disconnectChat" -> api.disconnectChat(chatID){ r -> resultProcessing(r) }
                "endChat" -> api.endChat(chatID){ r -> resultProcessing(r) }
                else -> Log.e("EEEEE", "########################################################")
            }
        }

        api.callback = object: ContactCenterEventsInterface {
            override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
                Log.e("&&&&&&&&&&&&", " &&&&&&&&&&&&&&&&&&&&&&&&&&& \t\n\t $result")
                this@TestActivity.resultProcessing(result)
            }
        }

    }

    fun resultProcessing(result: Any) {
        when (result) {
            is Failure<*> -> {
                Log.e("Failure", ">>> ${result.reason}")
                tvResult.text = "Failure\n${result.reason}"
            }
            is Success<*> -> {
                Log.e("Success", ">>> ${result.value}")
                tvResult.text = "Success\n${result.value}"

                (result.value as? List<ContactCenterEvent>)?.firstOrNull {
                    (it as? ContactCenterEvent.ChatSessionMessage)!= null }?.let {
                        (it as ContactCenterEvent.ChatSessionMessage )
                    Log.e("TestActivity", "MessageId = ${it.messageID}")
                    lastMessageID = it.messageID
                }

                (result.value as? ContactCenterChatSessionProperties)?.let {
                    chatID = it.chatID
                    partyID = it.chatID
                }
            }
        }
    }
}