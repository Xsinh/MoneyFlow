package com.prof18.moneyflow.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prof18.moneyflow.ComposeNavigationFactory
import com.prof18.moneyflow.R
import com.prof18.moneyflow.Screen
import com.prof18.moneyflow.domain.entities.BalanceRecap
import com.prof18.moneyflow.domain.entities.MoneyTransaction
import com.prof18.moneyflow.domain.entities.TransactionTypeUI
import com.prof18.moneyflow.features.home.components.HeaderNavigator
import com.prof18.moneyflow.features.home.components.HomeRecap
import com.prof18.moneyflow.presentation.CategoryIcon
import com.prof18.moneyflow.presentation.home.HomeModel
import com.prof18.moneyflow.ui.components.Loader
import com.prof18.moneyflow.ui.components.TransactionCard
import com.prof18.moneyflow.ui.style.AppMargins
import com.prof18.moneyflow.ui.style.MoneyFlowTheme
import org.koin.androidx.compose.getViewModel
import timber.log.Timber

class HomeScreenFactory(private val paddingValues: PaddingValues) : ComposeNavigationFactory {
    override fun create(navGraphBuilder: NavGraphBuilder, navController: NavController) {
        navGraphBuilder.composable(Screen.HomeScreen.route) {
            val homeViewModel = getViewModel<HomeViewModel>()
            val homeModelState: HomeModel by homeViewModel.homeState.collectAsState()
            val hideSensitiveDataState: Boolean by homeViewModel.hideSensitiveDataState.collectAsState()


            HomeScreen(
                navigateToAddTransaction = {
                    navController.navigate(Screen.AddTransactionScreen.route)
                },
                paddingValues = paddingValues,
                deleteTransaction = { transactionId ->
                    homeViewModel.deleteTransaction(transactionId)
                },
                homeModel = homeModelState,
                hideSensitiveDataState = hideSensitiveDataState,
                changeSensitiveDataVisibility = { visibility ->
                    homeViewModel.changeSensitiveDataVisibility(
                        visibility
                    )
                },
                navigateToAllTransactions = { navController.navigate(Screen.AllTransactionsScreen.route) }
            )
        }
    }
}

@Composable
fun HomeScreen(
    navigateToAddTransaction: () -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(0.dp),
    deleteTransaction: (Long) -> Unit = {},
    homeModel: HomeModel,
    hideSensitiveDataState: Boolean,
    changeSensitiveDataVisibility: (Boolean) -> Unit = {},
    navigateToAllTransactions: () -> Unit
) {


    when (homeModel) {
        is HomeModel.Loading -> Loader()
        is HomeModel.HomeState -> {

            Column(modifier = Modifier.padding(AppMargins.small)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.my_wallet),
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier
                            .padding(horizontal = AppMargins.regular)
                            .padding(top = AppMargins.regular)
                    )


                    Row {

                        IconButton(
                            onClick = { changeSensitiveDataVisibility(hideSensitiveDataState.not()) },
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(top = AppMargins.small)
                        ) {
                            if (hideSensitiveDataState) {
                                Icon(
                                    Icons.Rounded.Visibility,
                                    contentDescription = stringResource(R.string.show_sensitive_data),
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.VisibilityOff,
                                    contentDescription = stringResource(R.string.hide_sensitive_data),
                                )
                            }
                        }

                        IconButton(
                            onClick = { navigateToAddTransaction() },
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(top = AppMargins.small)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                            )
                        }
                    }
                }

                HomeRecap(
                    balanceRecap = homeModel.balanceRecap,
                    hideSensitiveData = hideSensitiveDataState
                )
                HeaderNavigator(
                    title = stringResource(R.string.latest_transactions),
                    onClick = navigateToAllTransactions
                )

                if (homeModel.latestTransactions.isEmpty()) {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(id = R.string.shrug),
                                modifier = Modifier
                                    .padding(bottom = AppMargins.small),
                                style = MaterialTheme.typography.h6
                            )

                            Text(
                                stringResource(R.string.empty_wallet),
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        items(homeModel.latestTransactions) { transaction ->
                            val (showTransactionMenu, setShowTransactionMenu) = remember {
                                mutableStateOf(
                                    false
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.TopStart)
                            ) {
                                TransactionCard(
                                    transaction = transaction,
                                    onClick = {
                                        Timber.d("onClick")
                                    },
                                    onLongPress = {
                                        setShowTransactionMenu(true)
                                    },
                                    hideSensitiveData = hideSensitiveDataState
                                )
                                DropdownMenu(
                                    expanded = showTransactionMenu,
                                    onDismissRequest = { setShowTransactionMenu(false) }
                                ) {
                                    DropdownMenuItem(onClick = {
                                        deleteTransaction(transaction.id)
                                        setShowTransactionMenu(false)
                                    }) {
                                        Text(stringResource(R.string.delete))
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                }
            }
        }
        is HomeModel.Error -> Text(stringResource(R.string.generic_error_message))
    }
}

@Preview
@Composable
fun HomeScreenLightPreview() {
    MoneyFlowTheme {
        Surface {
            HomeScreen(
                homeModel = HomeModel.HomeState(
                    balanceRecap = BalanceRecap(
                        totalBalance = 5000.0,
                        monthlyIncome = 1000.0,
                        monthlyExpenses = 50.0
                    ),
                    latestTransactions = listOf(
                        MoneyTransaction(
                            id = 0,
                            title = "Ice Cream",
                            icon = CategoryIcon.IC_ICE_CREAM_SOLID,
                            amount = 10.0,
                            type = TransactionTypeUI.EXPENSE,
                            milliseconds = 0,
                            formattedDate = "12 July 2021"
                        ),
                        MoneyTransaction(
                            id = 1,
                            title = "Tip",
                            icon = CategoryIcon.IC_MONEY_CHECK_ALT_SOLID,
                            amount = 50.0,
                            type = TransactionTypeUI.INCOME,
                            milliseconds = 0,
                            formattedDate = "12 July 2021"
                        )
                    )
                ),
                hideSensitiveDataState = true,
                navigateToAllTransactions = {}
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenDarkPreview() {
    MoneyFlowTheme(darkTheme = true) {
        Surface {
            HomeScreen(
                homeModel = HomeModel.HomeState(
                    balanceRecap = BalanceRecap(
                        totalBalance = 5000.0,
                        monthlyIncome = 1000.0,
                        monthlyExpenses = 50.0
                    ),
                    latestTransactions = listOf(
                        MoneyTransaction(
                            id = 0,
                            title = "Ice Cream",
                            icon = CategoryIcon.IC_ICE_CREAM_SOLID,
                            amount = 10.0,
                            type = TransactionTypeUI.EXPENSE,
                            milliseconds = 0,
                            formattedDate = "12 July 2021"
                        ),
                        MoneyTransaction(
                            id = 1,
                            title = "Tip",
                            icon = CategoryIcon.IC_MONEY_CHECK_ALT_SOLID,
                            amount = 50.0,
                            type = TransactionTypeUI.INCOME,
                            milliseconds = 0,
                            formattedDate = "12 July 2021"
                        )
                    )
                ),
                hideSensitiveDataState = false,
                navigateToAllTransactions = {}
            )
        }
    }
}
