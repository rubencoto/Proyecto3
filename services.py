from sqlalchemy.orm import Session
from sqlalchemy import func, and_, desc
from database import (
    Customer, Account, Transaction, AccountingEntry, DailyLimit, 
    Alert, Complaint, AccountStatus, TransactionType, EntryType, 
    AlertType, ComplaintStatus
)
from schemas import (
    CustomerCreate, AccountCreate, TransactionCreate, ComplaintCreate,
    LimitCheckRequest, AMLCheckRequest
)
from datetime import datetime, timedelta
from decimal import Decimal
import uuid
import random
import string
from typing import List, Optional


class CustomerService:
    @staticmethod
    def create_customer(db: Session, customer_data: CustomerCreate) -> Customer:
        # Validate not sanction listed
        if customer_data.is_sanction_listed:
            raise ValueError("Cannot create customer: Customer is in sanction list")
        
        # Check if document_id already exists
        existing = db.query(Customer).filter(Customer.document_id == customer_data.document_id).first()
        if existing:
            raise ValueError("Customer with this document ID already exists")
        
        # Check if email already exists
        existing_email = db.query(Customer).filter(Customer.email == customer_data.email).first()
        if existing_email:
            raise ValueError("Customer with this email already exists")
        
        customer = Customer(**customer_data.dict())
        db.add(customer)
        db.commit()
        db.refresh(customer)
        return customer

    @staticmethod
    def get_customer(db: Session, customer_id: int) -> Optional[Customer]:
        return db.query(Customer).filter(Customer.id == customer_id).first()


class AccountService:
    @staticmethod
    def create_account(db: Session, account_data: AccountCreate) -> Account:
        # Verify customer exists
        customer = db.query(Customer).filter(Customer.id == account_data.customer_id).first()
        if not customer:
            raise ValueError("Customer not found")
        
        # Generate unique account number
        account_number = AccountService._generate_account_number(db)
        
        account = Account(
            account_number=account_number,
            customer_id=account_data.customer_id,
            currency=account_data.currency,
            daily_limit=account_data.daily_limit
        )
        
        db.add(account)
        db.commit()
        db.refresh(account)
        return account

    @staticmethod
    def get_account(db: Session, account_id: int) -> Optional[Account]:
        return db.query(Account).filter(Account.id == account_id).first()

    @staticmethod
    def get_account_balance(db: Session, account_id: int) -> Optional[Account]:
        return db.query(Account).filter(Account.id == account_id).first()

    @staticmethod
    def get_account_statement(db: Session, account_id: int, limit: int = 50) -> tuple:
        account = db.query(Account).filter(Account.id == account_id).first()
        if not account:
            return None, []
        
        transactions = db.query(Transaction).filter(
            Transaction.account_id == account_id
        ).order_by(desc(Transaction.created_at)).limit(limit).all()
        
        return account, transactions

    @staticmethod
    def _generate_account_number(db: Session) -> str:
        while True:
            # Generate 10-digit account number
            account_number = ''.join(random.choices(string.digits, k=10))
            existing = db.query(Account).filter(Account.account_number == account_number).first()
            if not existing:
                return account_number


class TransactionService:
    @staticmethod
    def process_transaction(db: Session, transaction_data: TransactionCreate) -> Transaction:
        try:
            # Start transaction
            account = db.query(Account).filter(Account.id == transaction_data.account_id).first()
            if not account:
                raise ValueError("Account not found")
            
            if account.status != AccountStatus.ACTIVE:
                raise ValueError("Account is not active")

            # Check daily limits for withdrawals and transfers
            if transaction_data.transaction_type in [TransactionType.WITHDRAW, TransactionType.TRANSFER]:
                TransactionService._check_daily_limit(db, account, transaction_data.amount)

            # Process based on transaction type
            if transaction_data.transaction_type == TransactionType.DEPOSIT:
                return TransactionService._process_deposit(db, account, transaction_data)
            elif transaction_data.transaction_type == TransactionType.WITHDRAW:
                return TransactionService._process_withdrawal(db, account, transaction_data)
            elif transaction_data.transaction_type == TransactionType.TRANSFER:
                return TransactionService._process_transfer(db, account, transaction_data)
            else:
                raise ValueError("Invalid transaction type")

        except Exception as e:
            db.rollback()
            raise e

    @staticmethod
    def _process_deposit(db: Session, account: Account, transaction_data: TransactionCreate) -> Transaction:
        # Create transaction
        transaction = Transaction(
            account_id=account.id,
            transaction_type=transaction_data.transaction_type,
            amount=transaction_data.amount,
            description=transaction_data.description,
            reference_number=str(uuid.uuid4())
        )
        db.add(transaction)
        db.flush()

        # Update account balance
        account.balance += transaction_data.amount
        
        # Create accounting entry (Credit for deposit)
        entry = AccountingEntry(
            account_id=account.id,
            transaction_id=transaction.id,
            entry_type=EntryType.CREDIT,
            amount=transaction_data.amount,
            description=f"Deposit: {transaction_data.description or 'No description'}"
        )
        db.add(entry)

        db.commit()
        db.refresh(transaction)
        return transaction

    @staticmethod
    def _process_withdrawal(db: Session, account: Account, transaction_data: TransactionCreate) -> Transaction:
        # Check sufficient balance
        if account.balance < transaction_data.amount:
            raise ValueError("Insufficient funds")

        # Create transaction
        transaction = Transaction(
            account_id=account.id,
            transaction_type=transaction_data.transaction_type,
            amount=transaction_data.amount,
            description=transaction_data.description,
            reference_number=str(uuid.uuid4())
        )
        db.add(transaction)
        db.flush()

        # Update account balance
        account.balance -= transaction_data.amount
        
        # Create accounting entry (Debit for withdrawal)
        entry = AccountingEntry(
            account_id=account.id,
            transaction_id=transaction.id,
            entry_type=EntryType.DEBIT,
            amount=transaction_data.amount,
            description=f"Withdrawal: {transaction_data.description or 'No description'}"
        )
        db.add(entry)

        # Update daily limit usage
        TransactionService._update_daily_limit_usage(db, account, transaction_data.amount)

        db.commit()
        db.refresh(transaction)
        return transaction

    @staticmethod
    def _process_transfer(db: Session, source_account: Account, transaction_data: TransactionCreate) -> Transaction:
        # Get target account
        target_account = db.query(Account).filter(Account.id == transaction_data.target_account_id).first()
        if not target_account:
            raise ValueError("Target account not found")
        
        if target_account.status != AccountStatus.ACTIVE:
            raise ValueError("Target account is not active")

        # Check sufficient balance
        if source_account.balance < transaction_data.amount:
            raise ValueError("Insufficient funds")

        # Check currency compatibility (for this MVP, we'll allow only same currency transfers)
        if source_account.currency != target_account.currency:
            raise ValueError("Currency mismatch between accounts")

        # Create transfer transaction
        transaction = Transaction(
            account_id=source_account.id,
            transaction_type=transaction_data.transaction_type,
            amount=transaction_data.amount,
            description=transaction_data.description,
            target_account_id=target_account.id,
            reference_number=str(uuid.uuid4())
        )
        db.add(transaction)
        db.flush()

        # Update balances in ONE database transaction
        source_account.balance -= transaction_data.amount
        target_account.balance += transaction_data.amount

        # Create accounting entries (Debit for source, Credit for target)
        debit_entry = AccountingEntry(
            account_id=source_account.id,
            transaction_id=transaction.id,
            entry_type=EntryType.DEBIT,
            amount=transaction_data.amount,
            description=f"Transfer to {target_account.account_number}: {transaction_data.description or 'No description'}"
        )
        
        credit_entry = AccountingEntry(
            account_id=target_account.id,
            transaction_id=transaction.id,
            entry_type=EntryType.CREDIT,
            amount=transaction_data.amount,
            description=f"Transfer from {source_account.account_number}: {transaction_data.description or 'No description'}"
        )
        
        db.add(debit_entry)
        db.add(credit_entry)

        # Update daily limit usage for source account
        TransactionService._update_daily_limit_usage(db, source_account, transaction_data.amount)

        db.commit()
        db.refresh(transaction)
        return transaction

    @staticmethod
    def _check_daily_limit(db: Session, account: Account, amount: Decimal):
        today = datetime.now().date()
        
        # Get or create daily limit record
        daily_limit = db.query(DailyLimit).filter(
            and_(
                DailyLimit.account_id == account.id,
                func.date(DailyLimit.date) == today
            )
        ).first()

        if not daily_limit:
            daily_limit = DailyLimit(
                account_id=account.id,
                date=datetime.now(),
                limit_amount=account.daily_limit,
                used_amount=Decimal('0.00')
            )
            db.add(daily_limit)
            db.flush()

        # Check if transaction would exceed limit
        if daily_limit.used_amount + amount > daily_limit.limit_amount:
            # Create alert
            alert = Alert(
                account_id=account.id,
                alert_type=AlertType.DAILY_LIMIT,
                message=f"Daily limit exceeded. Attempted: {amount}, Available: {daily_limit.limit_amount - daily_limit.used_amount}",
                amount=amount
            )
            db.add(alert)
            db.commit()
            raise ValueError("Daily limit exceeded")

    @staticmethod
    def _update_daily_limit_usage(db: Session, account: Account, amount: Decimal):
        today = datetime.now().date()
        
        daily_limit = db.query(DailyLimit).filter(
            and_(
                DailyLimit.account_id == account.id,
                func.date(DailyLimit.date) == today
            )
        ).first()

        if daily_limit:
            daily_limit.used_amount += amount
        else:
            daily_limit = DailyLimit(
                account_id=account.id,
                date=datetime.now(),
                limit_amount=account.daily_limit,
                used_amount=amount
            )
            db.add(daily_limit)


class LimitService:
    @staticmethod
    def check_limit(db: Session, request: LimitCheckRequest) -> dict:
        account = db.query(Account).filter(Account.id == request.account_id).first()
        if not account:
            raise ValueError("Account not found")

        today = datetime.now().date()
        
        # Get daily limit usage
        daily_limit = db.query(DailyLimit).filter(
            and_(
                DailyLimit.account_id == account.id,
                func.date(DailyLimit.date) == today
            )
        ).first()

        used_today = daily_limit.used_amount if daily_limit else Decimal('0.00')
        available = account.daily_limit - used_today
        is_within_limit = request.amount <= available
        
        alert_created = False
        if not is_within_limit:
            # Create alert
            alert = Alert(
                account_id=account.id,
                alert_type=AlertType.DAILY_LIMIT,
                message=f"Limit check failed. Requested: {request.amount}, Available: {available}",
                amount=request.amount
            )
            db.add(alert)
            db.commit()
            alert_created = True

        return {
            "account_id": account.id,
            "requested_amount": request.amount,
            "daily_limit": account.daily_limit,
            "used_today": used_today,
            "available": available,
            "is_within_limit": is_within_limit,
            "alert_created": alert_created
        }


class ComplaintService:
    @staticmethod
    def create_complaint(db: Session, complaint_data: ComplaintCreate) -> Complaint:
        # Verify customer exists
        customer = db.query(Customer).filter(Customer.id == complaint_data.customer_id).first()
        if not customer:
            raise ValueError("Customer not found")

        # Calculate due date
        due_at = datetime.now() + timedelta(hours=complaint_data.sla_hours)

        complaint = Complaint(
            customer_id=complaint_data.customer_id,
            subject=complaint_data.subject,
            description=complaint_data.description,
            sla_hours=complaint_data.sla_hours,
            due_at=due_at
        )

        db.add(complaint)
        db.commit()
        db.refresh(complaint)
        return complaint


class ReportService:
    @staticmethod
    def generate_trial_balance(db: Session) -> dict:
        accounts = db.query(Account).filter(Account.status == AccountStatus.ACTIVE).all()
        
        trial_balance = {
            "generated_at": datetime.now(),
            "total_accounts": len(accounts),
            "balances_by_currency": {},
            "accounts": []
        }

        currency_totals = {}
        
        for account in accounts:
            trial_balance["accounts"].append({
                "account_id": account.id,
                "account_number": account.account_number,
                "currency": account.currency,
                "balance": account.balance
            })
            
            if account.currency not in currency_totals:
                currency_totals[account.currency] = Decimal('0.00')
            currency_totals[account.currency] += account.balance

        trial_balance["balances_by_currency"] = currency_totals
        return trial_balance


class AMLService:
    @staticmethod
    def check_aml(db: Session, request: AMLCheckRequest) -> dict:
        # AML threshold: ?10,000,000 (10 million CRC)
        aml_threshold = Decimal('10000000.00')
        
        account = db.query(Account).filter(Account.id == request.account_id).first()
        if not account:
            raise ValueError("Account not found")

        alert_triggered = False
        alert_id = None
        risk_level = "LOW"
        message = "Transaction within normal parameters"

        if request.amount >= aml_threshold:
            risk_level = "HIGH"
            alert_triggered = True
            message = f"AML Alert: Large transaction detected (? ?10,000,000). Amount: {request.amount}"
            
            # Create AML alert
            alert = Alert(
                account_id=account.id,
                alert_type=AlertType.AML,
                message=message,
                amount=request.amount
            )
            db.add(alert)
            db.commit()
            db.refresh(alert)
            alert_id = alert.id

        return {
            "account_id": request.account_id,
            "amount": request.amount,
            "risk_level": risk_level,
            "alert_triggered": alert_triggered,
            "alert_id": alert_id,
            "message": message
        }