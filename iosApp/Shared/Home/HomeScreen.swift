//
//  HomeScreen.swift
//  iosApp
//
//  Created by Marco Gomiero on 30/08/2020.
//

import SwiftUI
import shared

struct HomeScreen: View {
    
    @ObservedObject var viewModel: HomeViewModel = HomeViewModel()
    
    var body: some View {
        
        NavigationView {
            
            VStack {
                
                if (viewModel.homeModel is HomeModel.Loading) {
                    Loader().edgesIgnoringSafeArea(.all)
                } else if (viewModel.homeModel is HomeModel.HomeState) {
                    
                    HomeRecap(balanceRecap: (viewModel.homeModel as! HomeModel.HomeState).balanceRecap)
                    HeaderNavigator()
                    
                    List {
                        ForEach((viewModel.homeModel as! HomeModel.HomeState).latestTransactions, id: \.self) { transaction in
                            TransactionCard(transaction: transaction)
                                 .listRowInsets(EdgeInsets())
                        }
                    }
                    .listStyle(PlainListStyle())
                }
            }
            .navigationBarTitle(Text("Wallet"), displayMode: .automatic)
            .navigationBarItems(trailing: Button(action: {
                print("tapped")
            }) {
                Text("Add transaction")
                
            })
            .onAppear {
                self.viewModel.startObserving()
            }.onDisappear {
                self.viewModel.stopObserving()
            }
        }
    }
}

struct HomeScreen_Previews: PreviewProvider {
    static var previews: some View {
        HomeScreen()
    }
}
