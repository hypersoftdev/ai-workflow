package YOUR.PACKAGE.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import YOUR.PACKAGE.feature.entrance.ENTRANCE_ROUTE
import YOUR.PACKAGE.feature.entrance.EntranceScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ENTRANCE_ROUTE,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable(route = ENTRANCE_ROUTE) {
            EntranceScreen(
                navigateToNext = {
                    // Wire first real destination in create-mvi; keep popUpTo Entrance inclusive.
                },
            )
        }
    }
}
