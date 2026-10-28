package com.example.ecoabastos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecoabastos.data.model.LoteRequest
import com.example.ecoabastos.data.model.RegisterRequest
import com.example.ecoabastos.data.model.Rol
import com.example.ecoabastos.data.network.SessionManager
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EntregaEstadoUi
import com.example.ecoabastos.ui.screens.Profile.PerfilUi
import com.example.ecoabastos.ui.screens.Profile.PerfilUiState
import com.example.ecoabastos.ui.screens.Profile.ProfileScreen
import com.example.ecoabastos.ui.screens.admin.AdminSummaryScreen
import com.example.ecoabastos.ui.screens.admin.AdminUsersScreen
import com.example.ecoabastos.ui.screens.admin.destinosAdministrador
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorHomeScreen
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorHistoryScreen
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorHistorialItemUi
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorHistorialUiState
import com.example.ecoabastos.ui.screens.coordinador.destinosCoordinador
import com.example.ecoabastos.ui.screens.donante.DonationDetailScreen
import com.example.ecoabastos.ui.screens.donante.DonationHistoryScreen
import com.example.ecoabastos.ui.screens.donante.DonorHomeScreen
import com.example.ecoabastos.ui.screens.donante.EditLoteScreen
import com.example.ecoabastos.ui.screens.donante.EntregaUi
import com.example.ecoabastos.ui.screens.donante.LoteEditable
import com.example.ecoabastos.ui.screens.donante.PublishLoteScreen
import com.example.ecoabastos.ui.screens.login.LoginScreen
import com.example.ecoabastos.ui.screens.organizacion.CatalogoLoteUi
import com.example.ecoabastos.ui.screens.organizacion.OrganizationHomeScreen
import com.example.ecoabastos.ui.screens.organizacion.OrganizationRequestsScreen
import com.example.ecoabastos.ui.screens.organizacion.destinosOrganizacion
import com.example.ecoabastos.ui.screens.register.RegisterScreen
import com.example.ecoabastos.viewmodel.AdminViewModel
import com.example.ecoabastos.viewmodel.AuthViewModel
import com.example.ecoabastos.viewmodel.CoordinatorViewModel
import com.example.ecoabastos.viewmodel.DonorViewModel
import com.example.ecoabastos.viewmodel.OrganizationViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search

private val destinosDonador = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Historial", Icons.Outlined.Search, Icons.Filled.Search),
    BottomBarItem("Perfil", Icons.Outlined.Person, Icons.Filled.Person)
)

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    donorViewModel: DonorViewModel = viewModel(),
    orgViewModel: OrganizationViewModel = viewModel(),
    coordViewModel: CoordinatorViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val navController = rememberNavController()

    // Cache de entregas para poder recuperar la entrega completa al navegar al detalle
    var entregasCache by remember { mutableStateOf<List<EntregaUi>>(emptyList()) }
    var loteSeleccionadoParaSolicitud by remember { mutableStateOf<CatalogoLoteUi?>(null) }

    fun navegarSegunRol(rol: Rol) {
        val destino = when (rol) {
            Rol.DONANTE -> Routes.DONOR_HOME
            Rol.RECEPTORA -> Routes.ORG_HOME
            Rol.COORDINADOR -> Routes.COORD_HOME
            Rol.ADMINISTRADOR -> Routes.ADMIN_SUMMARY
        }
        navController.navigate(destino) { popUpTo(Routes.LOGIN) { inclusive = true } }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {

        // ──────────────────────── AUTENTICACIÓN ────────────────────────
        composable(Routes.LOGIN) {
            val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
            val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()

            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password) { rol -> navegarSegunRol(rol) }
                },
                onForgotPasswordClick = { },
                onRegisterClick = { authViewModel.clearError(); navController.navigate(Routes.REGISTER) },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        composable(Routes.REGISTER) {
            val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
            val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()

            RegisterScreen(
                onRegisterClick = { rol, nombre, email, direccion, password ->
                    authViewModel.register(
                        RegisterRequest(nombre = nombre, email = email, password = password,
                            rol = rol, nombreOrganizacion = nombre, direccionOrganizacion = direccion)
                    ) { rolRegistrado -> navegarSegunRol(rolRegistrado) }
                },
                onLoginClick = { authViewModel.clearError(); navController.popBackStack() },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        // ──────────────────────── DONADOR ────────────────────────
        composable(Routes.DONOR_HOME) {
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        donorViewModel.cargarMisLotes()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { donorViewModel.cargarMisLotes() }
            val publicacionesState by donorViewModel.publicacionesState.collectAsStateWithLifecycle()
            val nombre = SessionManager.usuario?.nombre ?: "Donador"

            DonorHomeScreen(
                nombreUsuario = nombre,
                state = publicacionesState,
                selectedTab = 0,
                onTabSelected = { tab ->
                    when (tab) {
                        1 -> navController.navigate(Routes.DONATION_HISTORY)
                        2 -> navController.navigate(Routes.DONOR_PROFILE)
                    }
                },
                onAddLoteClick = { navController.navigate(Routes.PUBLISH_LOTE) },
                onEditLote = { id -> navController.navigate(Routes.editLote(id)) },
                onLoteDetail = { id -> navController.navigate(Routes.loteDetail(id)) },
                onRetry = { donorViewModel.cargarMisLotes() }
            )
        }

        composable(Routes.PUBLISH_LOTE) {
            val context = LocalContext.current
            val isPublishing by donorViewModel.isPublishing.collectAsStateWithLifecycle()
            val publishError by donorViewModel.publishError.collectAsStateWithLifecycle()

            PublishLoteScreen(
                onPublishClick = { nombre, categoria, cantidad, unidadMedida, fechaIso, ubicacion, fotoUri ->
                    val request = LoteRequest(nombre = nombre, categoria = categoria,
                        cantidad = cantidad, unidadMedida = unidadMedida,
                        fechaLimite = fechaIso, ubicacion = ubicacion.ifBlank { null })
                    donorViewModel.publicarLote(context, request, fotoUri) { navController.popBackStack() }
                },
                isLoading = isPublishing,
                errorMessage = publishError
            )
        }

        composable(
            route = Routes.EDIT_LOTE,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            EditLoteScreen(
                lote = LoteEditable(id = loteId, nombre = "Lote #$loteId", categoria = "General",
                    cantidad = "10", unidadMedida = "kg", fecha = "2026-11-05",
                    hora = "06:00 PM", ubicacion = "", observaciones = ""),
                onUpdateClick = { navController.popBackStack() }
            )
        }

        composable(Routes.DONATION_HISTORY) {
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        donorViewModel.cargarHistorial()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { donorViewModel.cargarHistorial() }
            val historialState by donorViewModel.historialState.collectAsStateWithLifecycle()

            // Guardamos las entregas en caché para poder recuperarlas en el detalle
            if (historialState is com.example.ecoabastos.ui.screens.donante.HistorialUiState.Exito) {
                entregasCache = (historialState as com.example.ecoabastos.ui.screens.donante.HistorialUiState.Exito).entregas
            }

            DonationHistoryScreen(
                state = historialState,
                selectedTab = 1,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.DONOR_HOME) { popUpTo(Routes.DONOR_HOME) { inclusive = true } }
                        2 -> navController.navigate(Routes.DONOR_PROFILE)
                    }
                },
                onPublishLoteClick = { navController.navigate(Routes.PUBLISH_LOTE) },
                onVerDetalle = { entrega -> navController.navigate(Routes.donationDetail(entrega.id)) },
                onRetry = { donorViewModel.cargarHistorial() },
                onRefresh = { donorViewModel.cargarHistorial() }
            )
        }

        composable(
            route = Routes.DONATION_DETAIL,
            arguments = listOf(navArgument("entregaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entregaId = backStackEntry.arguments?.getLong("entregaId") ?: 0L
            val entrega = entregasCache.find { it.id == entregaId } ?: EntregaUi(
                id = entregaId,
                nombreLote = "Lote #$entregaId",
                categoria = "Alimento",
                cantidad = "—",
                estado = EntregaEstadoUi.COMPLETADO,
                recogidoPor = "—",
                fecha = "—"
            )

            DonationDetailScreen(
                entrega = entrega,
                unidadMedida = entrega.unidadMedida,
                hora = entrega.hora,
                organizacion = entrega.recogidoPor,
                observaciones = entrega.observaciones,
                onVolverClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.LOTE_DETAIL,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
            LaunchedEffect(loteId) {
                donorViewModel.cargarDetalleLote(loteId)
            }
            val loteDetalle by donorViewModel.loteDetalle.collectAsStateWithLifecycle()
            val lote = loteDetalle

            val entrega = EntregaUi(
                id = loteId,
                nombreLote = lote?.nombre ?: "Lote #$loteId",
                categoria = lote?.categoria ?: "Alimento",
                cantidad = if (lote != null) "${lote.cantidadDisponible}" else "—",
                estado = when (lote?.estado) {
                    com.example.ecoabastos.data.model.EstadoLote.DISPONIBLE -> EntregaEstadoUi.PROGRAMADA
                    com.example.ecoabastos.data.model.EstadoLote.RESERVADO -> EntregaEstadoUi.RETIRADO
                    com.example.ecoabastos.data.model.EstadoLote.ENTREGADO -> EntregaEstadoUi.COMPLETADO
                    com.example.ecoabastos.data.model.EstadoLote.VENCIDO -> EntregaEstadoUi.INCIDENCIA
                    com.example.ecoabastos.data.model.EstadoLote.CANCELADO -> EntregaEstadoUi.INCIDENCIA
                    else -> EntregaEstadoUi.PROGRAMADA
                },
                recogidoPor = lote?.receptorNombre ?: "",
                fecha = lote?.fechaPublicacion?.take(10) ?: "Hoy"
            )

            DonationDetailScreen(
                entrega = entrega,
                unidadMedida = lote?.unidadMedida ?: "kg",
                hora = lote?.fechaLimite?.let { if (it.length >= 16) it.substring(11, 16) else "10:30 a. m." } ?: "10:30 a. m.",
                organizacion = if (entrega.recogidoPor.isNotBlank()) entrega.recogidoPor else "Esperando retiro",
                observaciones = lote?.ubicacion?.let { "Ubicación: $it" } ?: "Lote registrado y disponible para entrega.",
                onVolverClick = { navController.popBackStack() }
            )
        }

        composable(Routes.DONOR_PROFILE) {
            val usuarioActual = SessionManager.usuario
            ProfileScreen(
                state = PerfilUiState.Exito(PerfilUi(usuarioActual?.nombre ?: "Donante",
                    usuarioActual?.email ?: "", usuarioActual?.organizacionNombre ?: "Sede Donante")),
                bottomBarItems = destinosDonador,
                selectedTab = 2,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.DONOR_HOME) { popUpTo(Routes.DONOR_HOME) { inclusive = true } }
                        1 -> navController.navigate(Routes.DONATION_HISTORY)
                    }
                },
                onFieldClick = {},
                onLogoutClick = { authViewModel.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) } },
                onDeleteAccountClick = {},
                onRetry = {}
            )
        }

        // ──────────────────────── ORGANIZACIÓN ────────────────────────
        composable(Routes.ORG_HOME) {
            val lifecycleOwnerOrgHome = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwnerOrgHome) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) { orgViewModel.cargarCatalogo() }
                }
                lifecycleOwnerOrgHome.lifecycle.addObserver(observer)
                onDispose { lifecycleOwnerOrgHome.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { orgViewModel.cargarCatalogo() }
            val catalogoState by orgViewModel.catalogoState.collectAsStateWithLifecycle()
            val usuarioActual = SessionManager.usuario
            val nombreOrg = usuarioActual?.organizacionNombre ?: usuarioActual?.nombre ?: "Organización"

            OrganizationHomeScreen(
                nombreOrganizacion = nombreOrg,
                state = catalogoState,
                selectedTab = 0,
                onTabSelected = { tab ->
                    when (tab) {
                        1 -> navController.navigate(Routes.ORG_REQUESTS)
                        2 -> navController.navigate(Routes.ORG_PROFILE)
                    }
                },
                onSolicitarClick = { lote ->
                    loteSeleccionadoParaSolicitud = lote
                    navController.navigate(Routes.ORG_REQUESTS)
                },
                onRetry = { orgViewModel.cargarCatalogo() }
            )
        }

        composable(Routes.ORG_REQUESTS) {
            val lifecycleOwnerOrgReq = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwnerOrgReq) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) { orgViewModel.cargarMisSolicitudes() }
                }
                lifecycleOwnerOrgReq.lifecycle.addObserver(observer)
                onDispose { lifecycleOwnerOrgReq.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { orgViewModel.cargarMisSolicitudes() }
            val solicitudesState by orgViewModel.solicitudesState.collectAsStateWithLifecycle()
            val mensajeUi by orgViewModel.mensajeUi.collectAsStateWithLifecycle()
            val isRecogiendo by orgViewModel.isRecogiendo.collectAsStateWithLifecycle()

            OrganizationRequestsScreen(
                loteSeleccionado = loteSeleccionadoParaSolicitud,
                solicitudesState = solicitudesState,
                mensajeExito = mensajeUi,
                isRecogiendo = isRecogiendo,
                onMensajeConsumido = { orgViewModel.limpiarMensaje() },
                selectedTab = 1,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.ORG_HOME) { popUpTo(Routes.ORG_HOME) { inclusive = true } }
                        2 -> navController.navigate(Routes.ORG_PROFILE)
                    }
                },
                onGenerarSolicitud = { _, _, cantidad, _, _ ->
                    val loteId = loteSeleccionadoParaSolicitud?.id ?: 1L
                    orgViewModel.solicitarDonacion(loteId, cantidad) {
                        loteSeleccionadoParaSolicitud = null
                    }
                },
                onConfirmarRetiro = { id, novedades -> orgViewModel.confirmarRetiro(id, novedades) },
                onIrAlCatalogo = {
                    navController.navigate(Routes.ORG_HOME) { popUpTo(Routes.ORG_HOME) { inclusive = true } }
                }
            )
        }

        composable(Routes.ORG_PROFILE) {
            val usuarioActual = SessionManager.usuario
            ProfileScreen(
                state = PerfilUiState.Exito(PerfilUi(usuarioActual?.nombre ?: "Organización",
                    usuarioActual?.email ?: "", usuarioActual?.organizacionNombre ?: "Comedor")),
                bottomBarItems = destinosOrganizacion,
                selectedTab = 2,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.ORG_HOME) { popUpTo(Routes.ORG_HOME) { inclusive = true } }
                        1 -> navController.navigate(Routes.ORG_REQUESTS)
                    }
                },
                onFieldClick = {},
                onLogoutClick = { authViewModel.logout(); navController.navigate(Routes.LOGIN) { popUpTo(0) } },
                onDeleteAccountClick = {},
                onRetry = {}
            )
        }

        // ──────────────────────── COORDINADOR ────────────────────────
        composable(Routes.COORD_HOME) {
            val lifecycleOwnerCoordHome = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwnerCoordHome) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) { coordViewModel.cargarSolicitudes() }
                }
                lifecycleOwnerCoordHome.lifecycle.addObserver(observer)
                onDispose { lifecycleOwnerCoordHome.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { coordViewModel.cargarSolicitudes() }
            val coordState by coordViewModel.coordinatorState.collectAsStateWithLifecycle()

            CoordinatorHomeScreen(
                state = coordState,
                selectedBottomTab = 0,
                onBottomTabSelected = { tab ->
                    when (tab) {
                        1 -> navController.navigate(Routes.COORD_HISTORY)
                        2 -> navController.navigate(Routes.COORD_PROFILE)
                    }
                },
                onResolverSolicitud = { id, aprobada ->
                    coordViewModel.resolverSolicitud(id, aprobada, null, null)
                },
                onRetry = { coordViewModel.cargarSolicitudes() }
            )
        }

        composable(Routes.COORD_HISTORY) {
            val lifecycleOwnerCoordHist = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwnerCoordHist) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) { coordViewModel.cargarSolicitudes() }
                }
                lifecycleOwnerCoordHist.lifecycle.addObserver(observer)
                onDispose { lifecycleOwnerCoordHist.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { coordViewModel.cargarSolicitudes() }
            val historialState by coordViewModel.historialState.collectAsStateWithLifecycle()

            CoordinatorHistoryScreen(
                state = historialState,
                selectedBottomTab = 1,
                onBottomTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.COORD_HOME) { popUpTo(Routes.COORD_HOME) { inclusive = true } }
                        2 -> navController.navigate(Routes.COORD_PROFILE)
                    }
                },
                onRetry = { coordViewModel.cargarSolicitudes() }
            )
        }

        composable(Routes.COORD_PROFILE) {
            val usuarioActual = SessionManager.usuario
            ProfileScreen(
                state = PerfilUiState.Exito(
                    PerfilUi(
                        nombre = usuarioActual?.nombre ?: "Coordinador",
                        email = usuarioActual?.email ?: "",
                        direccion = "Coordinación Logística AlimentaRed"
                    )
                ),
                bottomBarItems = destinosCoordinador,
                selectedTab = 2,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.COORD_HOME) { popUpTo(Routes.COORD_HOME) { inclusive = true } }
                        1 -> navController.navigate(Routes.COORD_HISTORY)
                    }
                },
                onFieldClick = {},
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onDeleteAccountClick = {},
                onRetry = {}
            )
        }

        // ──────────────────────── ADMINISTRADOR ────────────────────────
        composable(Routes.ADMIN_SUMMARY) {
            val lifecycleOwnerAdminSum = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwnerAdminSum) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) { adminViewModel.cargarResumenImpacto() }
                }
                lifecycleOwnerAdminSum.lifecycle.addObserver(observer)
                onDispose { lifecycleOwnerAdminSum.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { adminViewModel.cargarResumenImpacto() }
            val resumenState by adminViewModel.resumenState.collectAsStateWithLifecycle()

            AdminSummaryScreen(
                state = resumenState,
                selectedTab = 0,
                onTabSelected = { tab ->
                    when (tab) {
                        1 -> navController.navigate(Routes.ADMIN_USERS)
                        2 -> navController.navigate(Routes.ADMIN_PROFILE)
                    }
                },
                onRetry = { adminViewModel.cargarResumenImpacto() }
            )
        }

        composable(Routes.ADMIN_USERS) {
            val lifecycleOwnerAdminUsers = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwnerAdminUsers) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) { adminViewModel.cargarUsuarios() }
                }
                lifecycleOwnerAdminUsers.lifecycle.addObserver(observer)
                onDispose { lifecycleOwnerAdminUsers.lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { adminViewModel.cargarUsuarios() }
            val usuariosState by adminViewModel.usuariosState.collectAsStateWithLifecycle()

            AdminUsersScreen(
                state = usuariosState,
                selectedTab = 1,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.ADMIN_SUMMARY) { popUpTo(Routes.ADMIN_SUMMARY) { inclusive = true } }
                        2 -> navController.navigate(Routes.ADMIN_PROFILE)
                    }
                },
                onToggleUsuarioActivo = { id -> adminViewModel.toggleUsuarioActivo(id) },
                onFiltrarPorRol = { rol -> adminViewModel.filtrarUsuariosPorRol(rol) },
                onRetry = { adminViewModel.cargarUsuarios() }
            )
        }

        composable(Routes.ADMIN_PROFILE) {
            val usuarioActual = SessionManager.usuario
            ProfileScreen(
                state = PerfilUiState.Exito(
                    PerfilUi(
                        nombre = usuarioActual?.nombre ?: "Administrador",
                        email = usuarioActual?.email ?: "",
                        direccion = "Administración AlimentaRed"
                    )
                ),
                bottomBarItems = destinosAdministrador,
                selectedTab = 2,
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate(Routes.ADMIN_SUMMARY) { popUpTo(Routes.ADMIN_SUMMARY) { inclusive = true } }
                        1 -> navController.navigate(Routes.ADMIN_USERS) { popUpTo(Routes.ADMIN_SUMMARY) }
                    }
                },
                onFieldClick = {},
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onDeleteAccountClick = {},
                onRetry = {}
            )
        }
    }
}
