package com.chichi.practice

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException


open class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initRegister()
    }

    private fun initRegister() {
        btRegister.setOnClickListener {
            pbState.visibility = View.VISIBLE // 显示正在注册
            if (!isLegalInput()) {
                pbState.visibility = View.GONE
                return@setOnClickListener
            }
            val postData = "{\"user_name\":${etAccount.text}, \"user_password\":${etPassword.text}}"
            val callback = object : Callback {
                override fun onFailure(call: Call, e: IOException) { // 设置进度条隐藏
                    runOnUiThread {
                        Toast.makeText(baseContext, "登录失败", Toast.LENGTH_SHORT).show()
                        pbState.visibility = View.GONE
                    }
                }

                override fun onResponse(call: Call, response: Response) { // 设置进度条隐藏
                    runOnUiThread { pbState.visibility = View.GONE }
                    response.use {
                        if (response.isSuccessful) { // 连接成功
                            val res = response.body!!.string()
                            if (res == "success") { // 注册成功
                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        MainActivity::class.java
                                    ).putExtra("postData", postData)
                                )
                                account = etAccount.text.toString()
                                this@RegisterActivity.finish()
                            } else { //登录失败
                                runOnUiThread {
                                    Toast.makeText(baseContext, res, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(baseContext, "网络错误", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            sendAndGet(registerUrl, postData, callback)
        }
    }

    private fun isLegalInput(): Boolean {
        tvAccountHint.text = "" // 清空警告信息
        tvPasswordHint.text = ""
        tvPasswordConfirmHint.text = ""
        if (TextUtils.isEmpty(etAccount.text)) {
            tvAccountHint.text = "用户名不可为空"
            return false
        }
        if (etPassword.text.toString().length < 8) {
            tvPasswordHint.text = "密码强度不够，请输入至少8个字符"
            return false
        }
        if (etPassword.text.toString() != etPasswordConfirm.text.toString()) {
            tvPasswordConfirmHint.text = "两次输入的密码不一致"
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}