#!/usr/bin/env python3
"""
Banking MVP Demo Script
Demonstrates key functionality of the banking system
"""

import requests
import json
from datetime import datetime
from decimal import Decimal

BASE_URL = "http://localhost:8000"

def demo_health_check():
    """Test system health"""
    print("?? Checking system health...")
    response = requests.get(f"{BASE_URL}/health")
    print(f"Status: {response.status_code}")
    print(json.dumps(response.json(), indent=2))
    print()

def demo_customer_creation():
    """Demo customer creation and sanction list validation"""
    print("?? Creating customers...")
    
    # Valid customer
    customer1 = {
        "first_name": "Juan",
        "last_name": "Pérez",
        "document_id": "123456789",
        "email": "juan.perez@email.com",
        "phone": "87654321",
        "is_sanction_listed": False
    }
    
    response1 = requests.post(f"{BASE_URL}/customers", json=customer1)
    print(f"? Valid customer: {response1.status_code}")
    if response1.status_code == 200:
        customer1_id = response1.json()["id"]
        print(f"   Customer ID: {customer1_id}")
    
    # Sanctioned customer (should fail)
    customer2 = {
        "first_name": "Carlos",
        "last_name": "Sancionado",
        "document_id": "999999999",
        "email": "carlos.sancionado@email.com",
        "phone": "99999999",
        "is_sanction_listed": True
    }
    
    response2 = requests.post(f"{BASE_URL}/customers", json=customer2)
    print(f"? Sanctioned customer: {response2.status_code} (Expected 409)")
    print(f"   Error: {response2.json().get('detail', 'N/A')}")
    print()
    
    return customer1_id if response1.status_code == 200 else None

def demo_account_creation(customer_id):
    """Demo account creation"""
    print("?? Creating accounts...")
    
    account1 = {
        "customer_id": customer_id,
        "currency": "CRC",
        "daily_limit": "1000000.00"
    }
    
    account2 = {
        "customer_id": customer_id,
        "currency": "USD",
        "daily_limit": "5000.00"
    }
    
    response1 = requests.post(f"{BASE_URL}/accounts", json=account1)
    response2 = requests.post(f"{BASE_URL}/accounts", json=account2)
    
    account1_id = response1.json()["id"] if response1.status_code == 200 else None
    account2_id = response2.json()["id"] if response2.status_code == 200 else None
    
    print(f"? CRC Account: {response1.status_code}, ID: {account1_id}")
    print(f"? USD Account: {response2.status_code}, ID: {account2_id}")
    print()
    
    return account1_id, account2_id

def demo_transactions(account1_id, account2_id):
    """Demo transaction processing"""
    print("?? Processing transactions...")
    
    # Deposit to account 1
    deposit = {
        "account_id": account1_id,
        "transaction_type": "DEPOSIT",
        "amount": "50000.00",
        "description": "Initial deposit"
    }
    
    response = requests.post(f"{BASE_URL}/transactions/deposit", json=deposit)
    print(f"? Deposit: {response.status_code}")
    
    # Check balance
    balance_response = requests.get(f"{BASE_URL}/accounts/{account1_id}/balance")
    print(f"?? Balance: {balance_response.json()['balance']} {balance_response.json()['currency']}")
    
    # Try withdrawal with insufficient funds
    large_withdrawal = {
        "account_id": account1_id,
        "transaction_type": "WITHDRAW",
        "amount": "100000.00",
        "description": "Large withdrawal"
    }
    
    response = requests.post(f"{BASE_URL}/transactions/withdraw", json=large_withdrawal)
    print(f"? Large withdrawal: {response.status_code} (Expected 400)")
    print(f"   Error: {response.json().get('detail', 'N/A')}")
    
    # Valid withdrawal
    withdrawal = {
        "account_id": account1_id,
        "transaction_type": "WITHDRAW",
        "amount": "10000.00",
        "description": "Valid withdrawal"
    }
    
    response = requests.post(f"{BASE_URL}/transactions/withdraw", json=withdrawal)
    print(f"? Valid withdrawal: {response.status_code}")
    
    # Transfer between accounts (need second customer and account)
    print()

def demo_aml_check(account_id):
    """Demo AML monitoring"""
    print("?? Testing AML monitoring...")
    
    # Small amount (no alert)
    small_check = {
        "account_id": account_id,
        "transaction_type": "DEPOSIT",
        "amount": "5000000.00"
    }
    
    response = requests.post(f"{BASE_URL}/aml/check", json=small_check)
    result = response.json()
    print(f"? Small amount: Risk Level = {result['risk_level']}, Alert = {result['alert_triggered']}")
    
    # Large amount (should trigger alert)
    large_check = {
        "account_id": account_id,
        "transaction_type": "DEPOSIT",
        "amount": "15000000.00"
    }
    
    response = requests.post(f"{BASE_URL}/aml/check", json=large_check)
    result = response.json()
    print(f"?? Large amount: Risk Level = {result['risk_level']}, Alert = {result['alert_triggered']}")
    print(f"   Message: {result['message']}")
    print()

def demo_interest_calculation():
    """Demo interest calculation"""
    print("?? Testing interest calculation...")
    
    calc_request = {
        "amount": "100000.00",
        "annual_rate": "5.0",
        "days": 30
    }
    
    response = requests.post(f"{BASE_URL}/interests/calc", json=calc_request)
    result = response.json()
    
    print(f"Principal: ?{result['principal_amount']}")
    print(f"Interest Earned: ?{result['interest_earned']}")
    print(f"Tax Withheld (8%): ?{result['tax_withheld']}")
    print(f"Net Interest: ?{result['net_interest']}")
    print()

def demo_reports(account_id):
    """Demo reporting functionality"""
    print("?? Generating reports...")
    
    # Trial balance
    response = requests.get(f"{BASE_URL}/reports/trial-balance?format=json")
    if response.status_code == 200:
        trial_balance = response.json()
        print(f"?? Trial Balance: {trial_balance['total_accounts']} accounts")
        for currency, total in trial_balance['balances_by_currency'].items():
            print(f"   {currency}: {total}")
    
    # Account statement
    response = requests.get(f"{BASE_URL}/accounts/{account_id}/statement?limit=10")
    if response.status_code == 200:
        statement = response.json()
        print(f"?? Statement: {len(statement['transactions'])} transactions")
    
    print()

def demo_payment_stubs():
    """Demo payment system stubs"""
    print("?? Testing payment stubs...")
    
    # SINPE payment
    sinpe_payment = {
        "account_id": 1,
        "amount": "1000.00",
        "recipient_phone": "87654321",
        "description": "Test SINPE payment"
    }
    
    response = requests.post(f"{BASE_URL}/payments/sinpe", json=sinpe_payment)
    if response.status_code == 200:
        result = response.json()
        print(f"?? SINPE: {result['status']} - Confirmation: {result['confirmation_code']}")
    
    # SWIFT payment
    swift_payment = {
        "account_id": 1,
        "amount": "500.00",
        "recipient_bank": "Test Bank",
        "recipient_account": "123456789",
        "swift_code": "TESTUS33",
        "description": "Test SWIFT payment"
    }
    
    response = requests.post(f"{BASE_URL}/payments/swift", json=swift_payment)
    if response.status_code == 200:
        result = response.json()
        print(f"?? SWIFT: {result['status']} - MT103: {result['mt103_reference']}")
    
    print()

def main():
    """Run complete demo"""
    print("?? Banking MVP - Demonstration")
    print("=" * 50)
    
    try:
        # Health check
        demo_health_check()
        
        # Customer creation
        customer_id = demo_customer_creation()
        if not customer_id:
            print("? Cannot continue without valid customer")
            return
        
        # Account creation
        account1_id, account2_id = demo_account_creation(customer_id)
        if not account1_id:
            print("? Cannot continue without valid account")
            return
        
        # Transactions
        demo_transactions(account1_id, account2_id)
        
        # AML monitoring
        demo_aml_check(account1_id)
        
        # Interest calculation
        demo_interest_calculation()
        
        # Reports
        demo_reports(account1_id)
        
        # Payment stubs
        demo_payment_stubs()
        
        print("? Demo completed successfully!")
        print("?? Visit http://localhost:8000/docs for interactive API documentation")
        
    except requests.exceptions.ConnectionError:
        print("? Cannot connect to the server. Make sure the application is running on localhost:8000")
    except Exception as e:
        print(f"? Demo failed: {e}")

if __name__ == "__main__":
    main()