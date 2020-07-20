package com.chichi.practice

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxj.xpopup.XPopup
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    var checkedRes = ArrayList<String>() // 保存用户的选择结果，"0"表示未选择
    var resList = ArrayList<HashMap<String, String>>() // 保存所有的题目及其答案

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvGrade.text = " 你好$account ，来一套题吧"

        initGetQuestions() // 获取所有的问题
        initSubmit()  // 提交答案
    }

    private fun initSubmit() {
        btSubmit.setOnClickListener {
            pbState.visibility = View.VISIBLE
            val postData = StringBuilder().append(intent.getStringExtra("postData")!! + "\n")
            for ((idx, res) in resList.withIndex()) {
                postData.append("{\"queId\":${res["queId"]},\"answer\":${checkedRes[idx]}}\n")
            }
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(baseContext, "提交失败", Toast.LENGTH_SHORT).show()
                        pbState.visibility = View.GONE
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread { pbState.visibility = View.GONE }// 进度条隐藏
                    if (response.isSuccessful) { // 请求成功，返回数据为多行json
                        val jsonStrList = response.body!!.string().split("\n")
                        val ansList = ArrayList<HashMap<String, String>>() // 保存正确答案等结果
                        for (jsonStr in jsonStrList) {
                            if (TextUtils.isEmpty(jsonStr)) { // 排除空串
                                continue
                            }
                            val jsonObject = JSONObject(jsonStr)
                            val hashMap = HashMap<String, String>()
                            for (jsonKey in jsonObject.keys()) {
                                hashMap[jsonKey.toString()] = jsonObject[jsonKey].toString()
                            }
                            ansList.add(hashMap)
                        }
                        submitUpdateUi(ansList) // 显示分数以及正确答案
                    } else {
                        runOnUiThread {
                            Toast.makeText(baseContext, "网络错误：无法请求服务器", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            sendAndGet(questionsUrl, postData.toString(), callback) // 返回答题情况并获取题目答案
        }
    }

    private fun initGetQuestions() {
        btGetQuestions.setOnClickListener {
            pbState.visibility = View.VISIBLE // 显示正在获取试卷
            val postData = intent.getStringExtra("postData")!!
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(baseContext, "提交失败", Toast.LENGTH_SHORT).show()
                        pbState.visibility = View.GONE
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread { pbState.visibility = View.GONE }// 进度条隐藏
                    if (response.isSuccessful) { // 请求成功，返回数据为多行json
                        val jsonStrList = response.body!!.string().split("\n")
                        for (jsonStr in jsonStrList) {
                            if (TextUtils.isEmpty(jsonStr)) { // 排除空串
                                continue
                            }
                            val jsonObject = JSONObject(jsonStr)
                            val hashMap = HashMap<String, String>()
                            for (jsonKey in jsonObject.keys()) {
                                hashMap[jsonKey.toString()] = jsonObject[jsonKey].toString()
                            }
                            resList.add(hashMap)
                        }
                        runOnUiThread {
                            btGetQuestions.visibility = View.GONE
                            rvQuestions.visibility = View.VISIBLE // 显示题目
                            btSubmit.visibility = View.VISIBLE // 显示提交按钮
                            rvQuestions.layoutManager = LinearLayoutManager(baseContext)
                            rvQuestions.adapter = QuestionsAdapter(resList, checkedRes, baseContext)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(baseContext, "网络错误：无法请求服务器", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            sendAndGet(questionsUrl, postData, callback)
        }
    }

    class QuestionsAdapter(
        private val data: ArrayList<HashMap<String, String>>,
        private val checkedRes: ArrayList<String>,
        private val context: Context
    ) :
        RecyclerView.Adapter<QuestionsAdapter.ViewHolder>() {
        init {
            for (item in data) {
                checkedRes.add("0")
            }
        }

        inner class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            var cbA: CheckBox = view.findViewById(R.id.cbA)
            var cbB: CheckBox = view.findViewById(R.id.cbB)
            var cbC: CheckBox = view.findViewById(R.id.cbC)
            var cbD: CheckBox = view.findViewById(R.id.cbD)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.question, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val questionMap = data[position]
            holder.tvTitle.text = String.format(
                context.getString(R.string.option),
                (position + 1).toString(),
                questionMap["que_title"]
            )

            val cbList = ArrayList<CheckBox>()
            cbList.add(holder.cbA)
            cbList.add(holder.cbB)
            cbList.add(holder.cbC)
            cbList.add(holder.cbD)
            for ((idx, cb) in cbList.withIndex()) { // 设置单选，用到了Ascii字符与列表索引的一一对应关系
                val char = (idx + 65).toChar().toString()
                cb.text = String.format(context.getString(R.string.option), char, questionMap[char])
                cb.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) { // 如果被选中，则将其余选项设为未选中状态
                        val checkedAns = checkedRes[position]
                        if (checkedAns != "0") { // 答案为A或B或C或D
                            // checkedAns.toInt() - "A".toInt() 表示该答案对应的checkBox对象的索引
                            cbList[checkedAns.toCharArray()[0].toInt() - 65].isChecked = false
                        }
                        checkedRes[position] = char
                    } else {
                        checkedRes[position] = "0"
                    }
                }
            }
        }
    }

    fun submitUpdateUi(resList: ArrayList<HashMap<String, String>>) {
        runOnUiThread {
            XPopup.setPrimaryColor(ContextCompat.getColor(baseContext, R.color.colorTheme))
            val dataList = ArrayList<String>()
            for ((idx, item) in resList.withIndex()) {
                if (idx > 0) {
                    val ans = item["answer"]
                    val rightAns = item["rightAnswer"]
                    dataList.add("题目${idx}${if (ans == rightAns) "正确" else "错误"}，正确答案 $rightAns")
                }
            }
            XPopup.Builder(this).asCenterList(
                "你的成绩是 ${resList[0]["score"]} 分", dataList.toTypedArray()
            ) { _, _ -> }.show()
            tvGrade.text = "你好 $account ，你的成绩是 ${resList[0]["score"]} 分"
        }
    }
}