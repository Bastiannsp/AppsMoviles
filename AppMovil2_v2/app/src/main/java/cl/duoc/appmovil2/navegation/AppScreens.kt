package cl.duoc.appmovil2.navegation

sealed class AppScreens(val route: String) {
    data object Home : AppScreens("home_page")
    data object Profile : AppScreens("profile_page")
    data object Settings : AppScreens("settings_page")

    data class Detail(val itemId: String) : AppScreens("detail_page/$itemId"){
        fun buildRoute(): String {
            return route.replace("{itemId}",itemId)
        }
    }

}
