from sqlalchemy import create_engine, Column, Integer, String, Decimal, DateTime, Boolean, ForeignKey, Enum, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy.sql import func
from datetime import datetime, timedelta
import enum
from config import settings

# Database Engine
engine = create_engine(
    settings.database_url,
    echo=settings.debug,
    pool_pre_ping=True
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


# Enums
class AccountStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    BLOCKED = "BLOCKED"
    CLOSED = "CLOSED"


class Currency(str, enum.Enum):
    CRC = "CRC"
    USD = "USD"


class TransactionType(str, enum.Enum):
    DEPOSIT = "DEPOSIT"
    WITHDRAW = "WITHDRAW"
    TRANSFER = "TRANSFER"


class EntryType(str, enum.Enum):
    DEBIT = "DEBIT"
    CREDIT = "CREDIT"


class ComplaintStatus(str, enum.Enum):
    OPEN = "OPEN"
    IN_PROGRESS = "IN_PROGRESS"
    RESOLVED = "RESOLVED"
    CLOSED = "CLOSED"


class AlertType(str, enum.Enum):
    DAILY_LIMIT = "DAILY_LIMIT"
    AML = "AML"
    INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS"


# Models
class Customer(Base):
    __tablename__ = "customers"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String(100), nullable=False)
    last_name = Column(String(100), nullable=False)
    document_id = Column(String(50), unique=True, nullable=False)
    email = Column(String(255), unique=True, nullable=False)
    phone = Column(String(20))
    is_sanction_listed = Column(Boolean, default=False, nullable=False)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

    accounts = relationship("Account", back_populates="customer")


class Account(Base):
    __tablename__ = "accounts"

    id = Column(Integer, primary_key=True, index=True)
    account_number = Column(String(20), unique=True, nullable=False)
    customer_id = Column(Integer, ForeignKey("customers.id"), nullable=False)
    currency = Column(Enum(Currency), nullable=False)
    balance = Column(Decimal(15, 2), default=0.00, nullable=False)
    status = Column(Enum(AccountStatus), default=AccountStatus.ACTIVE, nullable=False)
    daily_limit = Column(Decimal(15, 2), default=1000000.00)  # Default 1M CRC
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

    customer = relationship("Customer", back_populates="accounts")
    transactions = relationship("Transaction", back_populates="account")
    accounting_entries = relationship("AccountingEntry", back_populates="account")


class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    account_id = Column(Integer, ForeignKey("accounts.id"), nullable=False)
    transaction_type = Column(Enum(TransactionType), nullable=False)
    amount = Column(Decimal(15, 2), nullable=False)
    description = Column(String(500))
    reference_number = Column(String(100), unique=True)
    target_account_id = Column(Integer, ForeignKey("accounts.id"), nullable=True)  # For transfers
    created_at = Column(DateTime, default=func.now())

    account = relationship("Account", back_populates="transactions", foreign_keys=[account_id])
    target_account = relationship("Account", foreign_keys=[target_account_id])


class AccountingEntry(Base):
    __tablename__ = "accounting_entries"

    id = Column(Integer, primary_key=True, index=True)
    account_id = Column(Integer, ForeignKey("accounts.id"), nullable=False)
    transaction_id = Column(Integer, ForeignKey("transactions.id"), nullable=False)
    entry_type = Column(Enum(EntryType), nullable=False)
    amount = Column(Decimal(15, 2), nullable=False)
    description = Column(String(500))
    created_at = Column(DateTime, default=func.now())

    account = relationship("Account", back_populates="accounting_entries")
    transaction = relationship("Transaction")


class DailyLimit(Base):
    __tablename__ = "daily_limits"

    id = Column(Integer, primary_key=True, index=True)
    account_id = Column(Integer, ForeignKey("accounts.id"), nullable=False)
    date = Column(DateTime, nullable=False)
    used_amount = Column(Decimal(15, 2), default=0.00, nullable=False)
    limit_amount = Column(Decimal(15, 2), nullable=False)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

    account = relationship("Account")


class Alert(Base):
    __tablename__ = "alerts"

    id = Column(Integer, primary_key=True, index=True)
    account_id = Column(Integer, ForeignKey("accounts.id"), nullable=True)
    alert_type = Column(Enum(AlertType), nullable=False)
    message = Column(Text, nullable=False)
    amount = Column(Decimal(15, 2), nullable=True)
    is_resolved = Column(Boolean, default=False)
    created_at = Column(DateTime, default=func.now())
    resolved_at = Column(DateTime, nullable=True)

    account = relationship("Account")


class Complaint(Base):
    __tablename__ = "complaints"

    id = Column(Integer, primary_key=True, index=True)
    customer_id = Column(Integer, ForeignKey("customers.id"), nullable=False)
    subject = Column(String(200), nullable=False)
    description = Column(Text, nullable=False)
    status = Column(Enum(ComplaintStatus), default=ComplaintStatus.OPEN, nullable=False)
    sla_hours = Column(Integer, default=24, nullable=False)
    due_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())
    resolved_at = Column(DateTime, nullable=True)

    customer = relationship("Customer")


# Database dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# Create tables
def create_tables():
    Base.metadata.create_all(bind=engine)