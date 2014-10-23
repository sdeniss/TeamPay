package com.teampay.teampay;

import java.util.ArrayList;


public class Algorithm {
	private static final double c = 3;


	public static double PaymentCost(ArrayList<User> users, String id, double pay){
		int N = users.size();
		double MinP = pay/(c*N);
		double MaxP = pay*((c-1)*N+1)/(c*N);
		//linear search to find the index by id
		int index = 0;
		for(int i=0;i<N;i++){
			if(users.get(i).getId().equals(id)){
				index = i;
			}
		}
		//Transferring to 'Money' Array
		ArrayList<Double> money = new ArrayList<Double>(0);
		double MinMoney = 0,MoneySum = 0;
		for(int i=0;i<N;i++){
			double balance = users.get(i).getBalance();
			double income = users.get(i).getIncome();
			money.add(5*balance + income);
			if(money.get(i) < MinMoney){
				MinMoney = money.get(i);
			}
		}
		for(int i=0;i<N;i++){
			money.set(i,money.get(i)+Math.abs(MinMoney)+pay/20);
			MoneySum += money.get(i);
		}
		//answer is x!!
		double x = MinP + (money.get(index)/MoneySum) * (MaxP-MinP);
		return x;
	}
	
	public static void main(String[] args) {
		ArrayList<User> users = new ArrayList<User>(0);
		users.add(new User("1",null,-500.0,-300.0));
		users.add(new User("2",null,1000.0,86.0));
		users.add(new User("3",null,2.0,53.0));
		users.add(new User("4",null,20.0,1.0));
		System.out.println(PaymentCost(users,"1",100));
		System.out.println(PaymentCost(users,"2",100));
		System.out.println(PaymentCost(users,"3",100));
		System.out.println(PaymentCost(users,"4",100));

	}
}
