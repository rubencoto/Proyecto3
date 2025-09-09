import pytest
import asyncio
from httpx import AsyncClient
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from main import app
from database import Base, get_db
from decimal import Decimal

# Test database configuration
TEST_DATABASE_URL = "sqlite:///./test_banking.db"

# Create test engine
test_engine = create_engine(TEST_DATABASE_URL, connect_args={"check_same_thread": False})
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)


def override_get_db():
    try:
        db = TestingSessionLocal()
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db

# Create test client
client = TestClient(app)


@pytest.fixture(scope="module")
def setup_database():
    Base.metadata.create_all(bind=test_engine)
    yield
    Base.metadata.drop_all(bind=test_engine)


@pytest.fixture
def test_customer_data():
    return {
        "first_name": "Juan",
        "last_name": "Pérez",
        "document_id": "123456789",
        "email": "juan.perez@email.com",
        "phone": "87654321",
        "is_sanction_listed": False
    }


@pytest.fixture
def test_sanctioned_customer_data():
    return {
        "first_name": "Carlos",
        "last_name": "Sancionado",
        "document_id": "999999999",
        "email": "carlos.sancionado@email.com",
        "phone": "99999999",
        "is_sanction_listed": True
    }


@pytest.fixture
def test_account_data():
    return {
        "customer_id": 1,
        "currency": "CRC",
        "daily_limit": "1000000.00"
    }


class TestCustomers:
    def test_create_customer_success(self, setup_database, test_customer_data):
        response = client.post("/customers", json=test_customer_data)
        assert response.status_code == 200
        data = response.json()
        assert data["first_name"] == test_customer_data["first_name"]
        assert data["email"] == test_customer_data["email"]
        assert data["is_sanction_listed"] == False

    def test_create_sanctioned_customer_fails(self, setup_database, test_sanctioned_customer_data):
        """Test: Alta cliente sancionado ? 409"""
        response = client.post("/customers", json=test_sanctioned_customer_data)
        assert response.status_code == 409
        assert "sanction" in response.json()["detail"].lower()


class TestAccounts:
    def test_create_account_success(self, setup_database, test_customer_data, test_account_data):
        # First create a customer
        customer_response = client.post("/customers", json=test_customer_data)
        customer_id = customer_response.json()["id"]
        
        # Create account
        test_account_data["customer_id"] = customer_id
        response = client.post("/accounts", json=test_account_data)
        assert response.status_code == 200
        data = response.json()
        assert data["currency"] == "CRC"
        assert data["status"] == "ACTIVE"

    def test_get_account_balance(self, setup_database):
        response = client.get("/accounts/1/balance")
        assert response.status_code == 200
        data = response.json()
        assert "balance" in data
        assert "currency" in data


class TestTransactions:
    def test_deposit_success(self, setup_database):
        deposit_data = {
            "account_id": 1,
            "transaction_type": "DEPOSIT",
            "amount": "1000.00",
            "description": "Test deposit"
        }
        response = client.post("/transactions/deposit", json=deposit_data)
        assert response.status_code == 200
        data = response.json()
        assert data["transaction_type"] == "DEPOSIT"
        assert float(data["amount"]) == 1000.00

    def test_withdraw_insufficient_funds(self, setup_database):
        """Test: Transferencia sin saldo ? 400"""
        withdraw_data = {
            "account_id": 1,
            "transaction_type": "WITHDRAW",
            "amount": "999999.00",
            "description": "Test withdrawal - should fail"
        }
        response = client.post("/transactions/withdraw", json=withdraw_data)
        assert response.status_code == 400
        assert "insufficient" in response.json()["detail"].lower()

    def test_transfer_success(self, setup_database, test_customer_data):
        """Test: Transferencia válida ? actualiza balances + crea 2 asientos"""
        # Create second customer and account for transfer
        second_customer = {
            "first_name": "María",
            "last_name": "González",
            "document_id": "987654321",
            "email": "maria.gonzalez@email.com",
            "phone": "12345678",
            "is_sanction_listed": False
        }
        customer_response = client.post("/customers", json=second_customer)
        customer_id = customer_response.json()["id"]
        
        account_data = {
            "customer_id": customer_id,
            "currency": "CRC",
            "daily_limit": "1000000.00"
        }
        account_response = client.post("/accounts", json=account_data)
        target_account_id = account_response.json()["id"]

        # First, ensure source account has sufficient balance
        deposit_data = {
            "account_id": 1,
            "transaction_type": "DEPOSIT",
            "amount": "5000.00",
            "description": "Setup for transfer test"
        }
        client.post("/transactions/deposit", json=deposit_data)

        # Get initial balances
        source_balance_response = client.get("/accounts/1/balance")
        target_balance_response = client.get(f"/accounts/{target_account_id}/balance")
        initial_source_balance = Decimal(source_balance_response.json()["balance"])
        initial_target_balance = Decimal(target_balance_response.json()["balance"])

        # Perform transfer
        transfer_data = {
            "account_id": 1,
            "transaction_type": "TRANSFER",
            "amount": "500.00",
            "description": "Test transfer",
            "target_account_id": target_account_id
        }
        transfer_response = client.post("/transactions/transfer", json=transfer_data)
        assert transfer_response.status_code == 200

        # Check balances updated
        final_source_balance_response = client.get("/accounts/1/balance")
        final_target_balance_response = client.get(f"/accounts/{target_account_id}/balance")
        final_source_balance = Decimal(final_source_balance_response.json()["balance"])
        final_target_balance = Decimal(final_target_balance_response.json()["balance"])

        # Verify balance changes
        assert final_source_balance == initial_source_balance - Decimal("500.00")
        assert final_target_balance == initial_target_balance + Decimal("500.00")


class TestAML:
    def test_aml_large_amount_triggers_alert(self, setup_database):
        """Test: AML stub ? depósito >= ?10,000,000 devuelve alerta"""
        aml_request = {
            "account_id": 1,
            "transaction_type": "DEPOSIT",
            "amount": "10000000.00"  # 10 million CRC
        }
        response = client.post("/aml/check", json=aml_request)
        assert response.status_code == 200
        data = response.json()
        assert data["alert_triggered"] == True
        assert data["risk_level"] == "HIGH"
        assert "AML Alert" in data["message"]

    def test_aml_small_amount_no_alert(self, setup_database):
        aml_request = {
            "account_id": 1,
            "transaction_type": "DEPOSIT",
            "amount": "5000000.00"  # 5 million CRC
        }
        response = client.post("/aml/check", json=aml_request)
        assert response.status_code == 200
        data = response.json()
        assert data["alert_triggered"] == False
        assert data["risk_level"] == "LOW"


class TestInterestCalculation:
    def test_interest_calculation_correct(self, setup_database):
        """Test: Interests stub ? cálculo correcto de interés + retención"""
        interest_request = {
            "amount": "100000.00",
            "annual_rate": "5.0",
            "days": 30
        }
        response = client.post("/interests/calc", json=interest_request)
        assert response.status_code == 200
        data = response.json()
        
        # Verify calculations
        principal = Decimal("100000.00")
        rate = Decimal("5.0")
        days = 30
        
        expected_interest = (principal * rate * days) / (Decimal("365") * Decimal("100"))
        expected_tax = (expected_interest * Decimal("8.0")) / Decimal("100")
        expected_net = expected_interest - expected_tax
        
        assert abs(Decimal(data["interest_earned"]) - expected_interest) < Decimal("0.01")
        assert abs(Decimal(data["tax_withheld"]) - expected_tax) < Decimal("0.01")
        assert abs(Decimal(data["net_interest"]) - expected_net) < Decimal("0.01")


class TestMiscellaneous:
    def test_health_check(self, setup_database):
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] in ["healthy", "unhealthy"]
        assert "database_connected" in data

    def test_trial_balance_json(self, setup_database):
        response = client.get("/reports/trial-balance?format=json")
        assert response.status_code == 200
        data = response.json()
        assert "generated_at" in data
        assert "total_accounts" in data
        assert "accounts" in data

    def test_trial_balance_csv(self, setup_database):
        response = client.get("/reports/trial-balance?format=csv")
        assert response.status_code == 200
        assert response.headers["content-type"] == "text/csv; charset=utf-8"

    def test_cards_not_implemented(self, setup_database):
        response = client.post("/cards", json={})
        assert response.status_code == 200
        data = response.json()
        assert data["message"] == "Not implemented yet"

    def test_loans_not_implemented(self, setup_database):
        response = client.post("/loans", json={})
        assert response.status_code == 200
        data = response.json()
        assert data["message"] == "Not implemented yet"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])