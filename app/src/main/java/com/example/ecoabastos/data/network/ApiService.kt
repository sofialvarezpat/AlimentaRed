package com.example.ecoabastos.data.network

import com.example.ecoabastos.data.model.AuthResponse
import com.example.ecoabastos.data.model.ConfirmarEntregaRequest
import com.example.ecoabastos.data.model.EntregaResponse
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.data.model.IncidenciaRequest
import com.example.ecoabastos.data.model.IncidenciaResponse
import com.example.ecoabastos.data.model.LoginRequest
import com.example.ecoabastos.data.model.LoteResponse
import com.example.ecoabastos.data.model.OrganizacionResponse
import com.example.ecoabastos.data.model.RegisterRequest
import com.example.ecoabastos.data.model.ReporteImpactoResponse
import com.example.ecoabastos.data.model.ResolverSolicitudRequest
import com.example.ecoabastos.data.model.RetiroRequest
import com.example.ecoabastos.data.model.SolicitudRequest
import com.example.ecoabastos.data.model.SolicitudResponse
import com.example.ecoabastos.data.model.TipoOrganizacion
import com.example.ecoabastos.data.model.UsuarioResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ---------- Salud (público) ----------
    @GET("api/health")
    suspend fun health(): Map<String, Any>

    // ---------- Autenticación (público) ----------
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    // ---------- Usuario ----------
    @GET("api/usuarios/me")
    suspend fun me(): UsuarioResponse

    @GET("api/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioResponse>>

    @PATCH("api/usuarios/{id}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("id") id: Long,
        @Body body: Map<String, Boolean>
    ): Response<UsuarioResponse>

    // ---------- Lotes ----------
    @GET("api/lotes")
    suspend fun listarLotes(
        @Query("categoria") categoria: String? = null,
        @Query("ubicacion") ubicacion: String? = null,
        @Query("estado") estado: EstadoLote? = null,
        @Query("fechaLimiteHasta") fechaLimiteHasta: String? = null // "2026-11-30"
    ): List<LoteResponse>

    // Multipart: la parte "lote" va como JSON y "imagen" es MultipartBody.Part
    @Multipart
    @POST("api/lotes")
    suspend fun crearLote(
        @Part("lote") lote: RequestBody,
        @Part imagen: MultipartBody.Part? = null
    ): Response<LoteResponse>

    @GET("api/lotes/mios")
    suspend fun misLotes(): List<LoteResponse>

    @GET("api/lotes/{id}")
    suspend fun obtenerLote(@Path("id") id: Long): LoteResponse

    @PATCH("api/lotes/{id}/cancelar")
    suspend fun cancelarLote(@Path("id") id: Long): Response<LoteResponse>

    // ---------- Solicitudes ----------
    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body request: SolicitudRequest): Response<SolicitudResponse>

    @GET("api/solicitudes/mias")
    suspend fun misSolicitudes(): List<SolicitudResponse>

    @PATCH("api/solicitudes/{id}/resolver")
    suspend fun resolverSolicitud(
        @Path("id") id: Long,
        @Body request: ResolverSolicitudRequest
    ): Response<SolicitudResponse>

    @PATCH("api/solicitudes/{id}/cancelar")
    suspend fun cancelarSolicitud(@Path("id") id: Long): Response<SolicitudResponse>

    // ---------- Entregas ----------
    @GET("api/entregas")
    suspend fun misEntregas(): List<EntregaResponse>

    @GET("api/entregas/{id}")
    suspend fun obtenerEntrega(@Path("id") id: Long): EntregaResponse

    @PATCH("api/entregas/{id}/retiro")
    suspend fun registrarRetiro(
        @Path("id") id: Long,
        @Body request: RetiroRequest
    ): Response<EntregaResponse>

    @PATCH("api/entregas/{id}/confirmar")
    suspend fun confirmarEntrega(
        @Path("id") id: Long,
        @Body request: ConfirmarEntregaRequest
    ): Response<EntregaResponse>

    @POST("api/entregas/{id}/incidencias")
    suspend fun reportarIncidencia(
        @Path("id") id: Long,
        @Body request: IncidenciaRequest
    ): Response<IncidenciaResponse>

    @GET("api/entregas/{id}/incidencias")
    suspend fun listarIncidencias(@Path("id") id: Long): List<IncidenciaResponse>

    @PATCH("api/entregas/incidencias/{incidenciaId}/resolver")
    suspend fun resolverIncidencia(
        @Path("incidenciaId") incidenciaId: Long
    ): Response<IncidenciaResponse>

    // ---------- Reportes ----------
    @GET("api/reportes/impacto")
    suspend fun reporteImpacto(
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("categoria") categoria: String? = null
    ): ReporteImpactoResponse

    // ---------- Organizaciones ----------
    @GET("api/organizaciones")
    suspend fun listarOrganizaciones(
        @Query("tipo") tipo: TipoOrganizacion? = null
    ): List<OrganizacionResponse>
}
