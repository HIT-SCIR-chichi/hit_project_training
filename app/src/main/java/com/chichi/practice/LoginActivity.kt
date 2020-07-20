package com.chichi.practice

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initLogin() // 初始化登录系统
        initRegister() // 初始化注册系统
    }

    private fun initLogin() {
        btLogin.setOnClickListener {
            pbState.visibility = View.VISIBLE // 显示正在登录
            val postData = "{\"user_name\":${etAccount.text}, \"user_password\":${etPassword.text}}"
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) { // 设置进度条隐藏
                    runOnUiThread {
                        Toast.makeText(baseContext, "登录失败", Toast.LENGTH_SHORT).show()
                        pbState.visibility = View.GONE
                    }
                }

                override fun onResponse(call: Call, response: Response) { // 设置进度条隐藏
                    this@LoginActivity.runOnUiThread { pbState.visibility = View.GONE }
                    response.use {
                        if (response.isSuccessful) { // 请求成功
                            val jsonRes = JSONObject(response.body!!.string())
                            if (jsonRes["data"] == "true") { // 登陆成功
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        MainActivity::class.java
                                    ).putExtra("postData", postData)
                                )
                                account = etAccount.text.toString()
                                this@LoginActivity.finish()
                            } else { //登录失败
                                runOnUiThread {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        jsonRes["error"].toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "网络错误：无法请求服务器",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            sendAndGet(loginUrl, postData, callback)
        }
    }

    private fun initRegister() {
        btRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}