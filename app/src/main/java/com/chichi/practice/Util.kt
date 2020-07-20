package com.chichi.practice

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

var account = "" // 账户名
private const val baseUrl = "https://7ea05f9f.cpolar.io"
const val loginUrl = "$baseUrl/mlogin" // 登陆网址
const val questionsUrl = "$baseUrl/queRequest" // 获取题目的网址
const val registerUrl = "$baseUrl/mregist"

fun sendAndGet(url: String, postData: String, callback: Callback) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url)
        .post(postData.toRequestBody(("application/json; charset=utf-8").toMediaType()))
        .build()
    client.newCall(request).enqueue(callback)
}