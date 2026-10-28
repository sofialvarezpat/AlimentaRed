package com.example.ecoabastos.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DONOR_HOME = "donor_home"
    const val PUBLISH_LOTE = "publish_lote"
    const val EDIT_LOTE = "edit_lote/{loteId}"
    const val DONATION_HISTORY = "donation_history"
    const val DONATION_DETAIL = "donation_detail/{entregaId}"
    const val LOTE_DETAIL = "lote_detail/{loteId}"
    const val DONOR_PROFILE = "donor_profile"

    // Organización Receptora
    const val ORG_HOME = "org_home"
    const val ORG_REQUESTS = "org_requests"
    const val ORG_PROFILE = "org_profile"

    // Coordinador
    const val COORD_HOME = "coord_home"
    const val COORD_HISTORY = "coord_history"
    const val COORD_PROFILE = "coord_profile"

    // Administrador
    const val ADMIN_SUMMARY = "admin_summary"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_PROFILE = "admin_profile"

    fun editLote(loteId: Long) = "edit_lote/$loteId"
    fun donationDetail(entregaId: Long) = "donation_detail/$entregaId"
    fun loteDetail(loteId: Long) = "lote_detail/$loteId"
}
