package com.prof18.moneyflow

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

interface ComposeNavigationFactory {
    fun create(navGraphBuilder: NavGraphBuilder, navController: NavController)
}

sealed class Screen(val route: String) {
    object AddTransactionScreen : Screen("add_transaction_screen")
    object CategoriesScreen : Screen("categories_screen")
    object HomeScreen: Screen("home_screen")
    object RecapScreen: Screen("recap_screen")
    object BudgetScreen: Screen("budget_screen")
    object SettingsScreen: Screen("settings_screen")
    object AllTransactionsScreen: Screen("all_transactions_screen")
}

sealed class NavigationArguments(val key: String) {
    object FromAddTransaction: NavigationArguments("from_add_transaction")
    object Category: NavigationArguments("category")
}

data class BottomNavigationItem(
    val screen: Screen,
    @StringRes val titleResId: Int,
    @DrawableRes val drawableResId: Int
)

val bottomNavigationItems = listOf(

    BottomNavigationItem(
        screen = Screen.HomeScreen,
        titleResId = R.string.home_screen,
        drawableResId = R.drawable.ic_home_solid
    ),

//    // Coming Soon
//    BottomNavigationItem(
//        screen = Screen.RecapScreen,
//        titleResId = R.string.recap_screen,
//        drawableResId = R.drawable.ic_chart_pie_solid
//    ),
//
//    // Coming Soon
//    BottomNavigationItem(
//        screen = Screen.BudgetScreen,
//        titleResId = R.string.budget_screen,
//        drawableResId = R.drawable.ic_balance_scale_left_solid
//    ),

    BottomNavigationItem(
        screen = Screen.SettingsScreen,
        titleResId = R.string.settings_screen,
        drawableResId = R.drawable.ic_cog_solid
    ),

)

