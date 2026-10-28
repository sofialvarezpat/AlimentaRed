package com.example.ecoabastos.data.network

import android.util.Log
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // URL pública de tu API desplegada en Render (debe terminar en "/")
    private const val BASE_URL = "https://alimentared-bxhk.onrender.com/"

    // Interceptor obligatorio con nivel BODY para auditar todas las peticiones y respuestas HTTP en Logcat
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("HTTP_BODY", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * DNS resiliente para el Emulador de Android en Windows:
     * Si el DNS interno del emulador (10.0.2.3) falla con "Unable to resolve host",
     * resuelve el dominio directamente contra los DNS públicos 8.8.8.8 y 1.1.1.1.
     */
    val resilientDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (e: UnknownHostException) {
                Log.w("HTTP_BODY", "Fallo DNS del sistema para $hostname, intentando con DNS público 8.8.8.8 / 1.1.1.1...")
                resolveViaPublicDns(hostname, "8.8.8.8")
                    .ifEmpty { resolveViaPublicDns(hostname, "1.1.1.1") }
                    .ifEmpty { throw e }
            }
        }
    }

    private fun resolveViaPublicDns(hostname: String, dnsServer: String): List<InetAddress> {
        return try {
            DatagramSocket().use { socket ->
                socket.soTimeout = 4000
                val query = buildDnsQuery(hostname)
                val serverAddr = InetAddress.getByName(dnsServer)
                socket.send(DatagramPacket(query, query.size, serverAddr, 53))

                val buffer = ByteArray(512)
                val responsePacket = DatagramPacket(buffer, buffer.size)
                socket.receive(responsePacket)
                parseDnsResponse(hostname, buffer, responsePacket.length)
            }
        } catch (ex: Exception) {
            Log.e("API_ERROR", "Fallo consulta DNS directa ($dnsServer) para $hostname: ${ex.message}")
            emptyList()
        }
    }

    private fun buildDnsQuery(domain: String): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        dos.writeShort(0x1234) // Transaction ID
        dos.writeShort(0x0100) // Flags: Standard query with recursion
        dos.writeShort(1)      // Questions = 1
        dos.writeShort(0)      // Answer RRs
        dos.writeShort(0)      // Authority RRs
        dos.writeShort(0)      // Additional RRs
        for (part in domain.split(".")) {
            val bytes = part.toByteArray(Charsets.UTF_8)
            dos.writeByte(bytes.size)
            dos.write(bytes)
        }
        dos.writeByte(0)       // Fin de QNAME
        dos.writeShort(1)      // QTYPE = A (IPv4)
        dos.writeShort(1)      // QCLASS = IN
        return baos.toByteArray()
    }

    private fun parseDnsResponse(hostname: String, data: ByteArray, length: Int): List<InetAddress> {
        val results = mutableListOf<InetAddress>()
        val dis = DataInputStream(data.inputStream(0, length))
        dis.readUnsignedShort() // ID
        dis.readUnsignedShort() // Flags
        val qdCount = dis.readUnsignedShort()
        val anCount = dis.readUnsignedShort()
        dis.readUnsignedShort() // NSCount
        dis.readUnsignedShort() // ARCount

        repeat(qdCount) {
            skipDnsName(dis)
            dis.readUnsignedShort()
            dis.readUnsignedShort()
        }

        repeat(anCount) {
            skipDnsName(dis)
            val type = dis.readUnsignedShort()
            dis.readUnsignedShort() // Class
            dis.readInt()           // TTL
            val rdLength = dis.readUnsignedShort()
            if (type == 1 && rdLength == 4) {
                val ip = ByteArray(4)
                dis.readFully(ip)
                results.add(InetAddress.getByAddress(hostname, ip))
            } else {
                dis.skipBytes(rdLength)
            }
        }
        return results
    }

    private fun skipDnsName(dis: DataInputStream) {
        while (true) {
            val len = dis.readUnsignedByte()
            if (len == 0) break
            if ((len and 0xC0) == 0xC0) {
                dis.readUnsignedByte()
                break
            }
            dis.skipBytes(len)
        }
    }

    // Render (plan gratuito) "duerme" el servidor tras inactividad:
    // la primera petición puede tardar 30-60 s en despertar.
    private val okHttpClient = OkHttpClient.Builder()
        .dns(resilientDns)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
