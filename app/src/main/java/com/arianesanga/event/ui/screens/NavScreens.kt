package com.arianesanga.event.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline // Ícone para Criar
import androidx.compose.material.icons.filled.DateRange // Ícone para Eventos
import androidx.compose.material.icons.filled.Settings // Ícone para Perfil
import androidx.compose.ui.graphics.vector.ImageVector


sealed class OrganizerScreens(val route: String, val icon: ImageVector, val title: String) {
    // Tela principal de listagem de eventos
    object Eventos : OrganizerScreens("eventos_list", Icons.Default.DateRange, "Eventos")

    // Tela para ir para a criação de um novo evento
    object CriarEvento : OrganizerScreens("criar_evento", Icons.Default.AddCircleOutline, "Criar")

    // Tela de Perfil e Configurações
    object Perfil : OrganizerScreens("perfil", Icons.Default.Settings, "Perfil")
}