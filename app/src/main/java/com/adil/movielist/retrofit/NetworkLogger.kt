package com.adil.movielist.retrofit

import android.util.Log
import okhttp3.*
import okhttp3.internal.http.promisesBody
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit

class NetworkLogger : Interceptor {
    private val TAG = "Network"
    private val UTF8 = Charset.forName("UTF-8")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val connection: Connection? = chain.connection()
        val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
        var requestStartMessage = "--> " + request.method + ' ' + request.url + ' ' + protocol
        if (hasRequestBody) {
            requestStartMessage += " (" + requestBody!!.contentLength() + "-byte body)"
        }
        Log.d(TAG, requestStartMessage)
        if (hasRequestBody) {
            // Request body headers are only present when installed as a network interceptor. Force
            // them to be included (when available) so there values are known.
            if (requestBody!!.contentType() != null) {
                Log.d(TAG, "Content-Type: " + requestBody.contentType())
            }
            if (requestBody.contentLength() != -1L) {
                Log.d(TAG, "Content-Length: " + requestBody.contentLength())
            }
        }
        val headers = request.headers
        run {
            var i = 0
            val count = headers.size
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(
                        name,
                        ignoreCase = true
                    )
                ) {
                    Log.d(TAG, name + ": " + headers.value(i))
                }
                i++
            }
        }
        if (!hasRequestBody || request.body!!.contentLength() > 1024 * 1024) {
            Log.d(TAG, "--> END " + request.method)
        } else if (bodyEncoded(request.headers)) {
            Log.d(TAG, "--> END " + request.method + " (encoded body omitted)")
        } else {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)
            var charset = UTF8
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            Log.d(TAG, "")
            if (isPlaintext(buffer)) {
                Log.d(TAG, buffer.readString(charset!!).replace("&".toRegex(), "\n"))
                Log.d(
                    TAG, "--> END " + request.method
                            + " (" + requestBody.contentLength() + "-byte body)"
                )
            } else {
                Log.d(
                    TAG, "--> END " + request.method + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)"
                )
            }
        }
        val startNs = System.nanoTime()
        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.d(TAG, "<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        Log.d(
            TAG, "<-- " + response.code + ' ' + response.message + ' '
                    + response.request.url + " (" + tookMs + "ms" + ')'
        )
        val responseHeaders = response.headers
        var i = 0
        val count = responseHeaders.size
        while (i < count) {
            Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i))
            i++
        }
        if (!response.promisesBody() || isMuted(request.url.toString())) {
            Log.d(TAG, "<-- END HTTP")
        } else if (bodyEncoded(response.headers)) {
            Log.d(TAG, "<-- END HTTP (encoded body omitted)")
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer
            var charset = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = try {
                    contentType.charset(UTF8)
                } catch (e: UnsupportedCharsetException) {
                    Log.d(TAG, "")
                    Log.d(TAG, "Couldn't decode the response body; charset is likely malformed.")
                    Log.d(TAG, "<-- END HTTP")
                    return response
                }
            }
            if (!isPlaintext(buffer)) {
                Log.d(TAG, "")
                Log.d(TAG, "<-- END HTTP (binary " + buffer.size + "-byte body omitted)")
                return response
            }
            if (contentLength != 0L) {
                Log.d(TAG, "")
                var responseBodyString = buffer.clone().readString(charset!!)
                try {
                    responseBodyString = JSONObject(responseBodyString).toString(4)
                } catch (ignore: JSONException) {
                }
                val responseBodyLines = responseBodyString.split("\n").toTypedArray()
                for (line in responseBodyLines) {
                    Log.d(TAG, line)
                }
            }
            Log.d(TAG, "<-- END HTTP (" + buffer.size + "-byte body)")
        }
        return response
    }

    private fun isMuted(url: String): Boolean {
        return url.contains("remote_config") || url.contains("fetch_form_data")
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false // Truncated UTF-8 sequence.
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }
}