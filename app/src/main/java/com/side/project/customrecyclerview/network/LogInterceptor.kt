package com.side.project.customrecyclerview.network

import android.util.Log
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

class LogInterceptor: Interceptor {
    private val tag = "Interceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val UTF8: Charset = Charset.forName("UTF-8")

        // Request
        val request: Request = chain.request()
        val requestBody: RequestBody? = request.body
        var reqBody: String? = null
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            var charset: Charset = UTF8
            val contentType: MediaType? = requestBody.contentType()
            if (contentType != null)
                charset = contentType.charset(UTF8)!!

            reqBody = buffer.readString(charset)
        }
        Log.e(tag, "\nRequest:\nmethod:${request.method}\nURL:${request.url}\nheaders:${request.headers}\nbody:${reqBody}")

        // Response
        val response: Response = chain.proceed(request)
        val responseBody: ResponseBody? = response.body
        var respBody: String? = null
        if (responseBody != null) {
            val source: BufferedSource = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer

            var charset: Charset = UTF8
            val contentType: MediaType? = responseBody.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)!!
                } catch (e: UnsupportedCharsetException) {
                    e.printStackTrace()
                }
            }
            respBody = buffer.clone().readString(charset)
        }
        Log.e(tag, "\nResponse:\ncode:${response.code}\nURL:${response.request.url}\nbody:${respBody}")

        return response
    }
}