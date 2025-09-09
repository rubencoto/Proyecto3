from pydantic import BaseModel, EmailStr, validator
from typing import Optional, List
from datetime import datetime
from decimal import Decimal
from database import AccountStatus, Currency, TransactionType, ComplaintStatus, AlertType


# Customer Schemas
class CustomerCreate(BaseModel):
    first_name: str
    last_name: str
    document_id: str
    email: EmailStr
    phone: Optional[str] = None
    is_sanction_listed: bool = False

    @validator('first_name', 'last_name')
    def validate_names(cls, v):
        if not v or len(v.strip()) < 2:
            raise ValueError('Name must be at least 2 characters long')
        return v.strip()

    @validator('document_id')
    def validate_document_id(cls, v):
        if not v or len(v.strip()) < 5:
            raise ValueError('Document ID must be at least 5 characters long')
        return v.strip()


class CustomerResponse(BaseModel):
    id: int
    first_name: str
    last_name: str
    document_id: str
    email: str
    phone: Optional[str]
    is_sanction_listed: bool
    created_at: datetime

    class Config:
        from_attributes = True


# Account Schemas
class AccountCreate(BaseModel):
    customer_id: int
    currency: Currency
    daily_limit: Optional[Decimal] = Decimal('1000000.00')

    @validator('daily_limit')
    def validate_daily_limit(cls, v):
        if v <= 0:
            raise ValueError('Daily limit must be positive')
        return v


class AccountResponse(BaseModel):
    id: int
    account_number: str
    customer_id: int
    currency: Currency
    balance: Decimal
    status: AccountStatus
    daily_limit: Decimal
    created_at: datetime

    class Config:
        from_attributes = True


# Transaction Schemas
class TransactionCreate(BaseModel):
    account_id: int
    transaction_type: TransactionType
    amount: Decimal
    description: Optional[str] = None
    target_account_id: Optional[int] = None

    @validator('amount')
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError('Amount must be positive')
        return v

    @validator('target_account_id')
    def validate_transfer(cls, v, values):
        if values.get('transaction_type') == TransactionType.TRANSFER and not v:
            raise ValueError('Target account is required for transfers')
        return v


class TransactionResponse(BaseModel):
    id: int
    account_id: int
    transaction_type: TransactionType
    amount: Decimal
    description: Optional[str]
    reference_number: Optional[str]
    target_account_id: Optional[int]
    created_at: datetime

    class Config:
        from_attributes = True


# Balance and Statement Schemas
class BalanceResponse(BaseModel):
    account_id: int
    account_number: str
    currency: Currency
    balance: Decimal
    status: AccountStatus


class StatementResponse(BaseModel):
    account_id: int
    account_number: str
    currency: Currency
    current_balance: Decimal
    transactions: List[TransactionResponse]


# Limits and Alerts Schemas
class LimitCheckRequest(BaseModel):
    account_id: int
    amount: Decimal

    @validator('amount')
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError('Amount must be positive')
        return v


class LimitCheckResponse(BaseModel):
    account_id: int
    requested_amount: Decimal
    daily_limit: Decimal
    used_today: Decimal
    available: Decimal
    is_within_limit: bool
    alert_created: bool = False


class AlertResponse(BaseModel):
    id: int
    account_id: Optional[int]
    alert_type: AlertType
    message: str
    amount: Optional[Decimal]
    is_resolved: bool
    created_at: datetime

    class Config:
        from_attributes = True


# Complaint Schemas
class ComplaintCreate(BaseModel):
    customer_id: int
    subject: str
    description: str
    sla_hours: Optional[int] = 24

    @validator('subject')
    def validate_subject(cls, v):
        if not v or len(v.strip()) < 5:
            raise ValueError('Subject must be at least 5 characters long')
        return v.strip()

    @validator('description')
    def validate_description(cls, v):
        if not v or len(v.strip()) < 10:
            raise ValueError('Description must be at least 10 characters long')
        return v.strip()

    @validator('sla_hours')
    def validate_sla_hours(cls, v):
        if v <= 0 or v > 720:  # Max 30 days
            raise ValueError('SLA hours must be between 1 and 720')
        return v


class ComplaintResponse(BaseModel):
    id: int
    customer_id: int
    subject: str
    description: str
    status: ComplaintStatus
    sla_hours: int
    due_at: datetime
    created_at: datetime
    resolved_at: Optional[datetime]

    class Config:
        from_attributes = True


# Trial Balance Schema
class TrialBalanceEntry(BaseModel):
    account_id: int
    account_number: str
    currency: Currency
    balance: Decimal


class TrialBalanceResponse(BaseModel):
    generated_at: datetime
    total_accounts: int
    balances_by_currency: dict
    accounts: List[TrialBalanceEntry]


# Payment Schemas (Stubs)
class SinpePaymentRequest(BaseModel):
    account_id: int
    amount: Decimal
    recipient_phone: str
    description: Optional[str] = None

    @validator('amount')
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError('Amount must be positive')
        return v

    @validator('recipient_phone')
    def validate_phone(cls, v):
        if not v or len(v.strip()) < 8:
            raise ValueError('Valid phone number required')
        return v.strip()


class SinpePaymentResponse(BaseModel):
    transaction_id: str
    status: str
    amount: Decimal
    recipient_phone: str
    confirmation_code: str
    processed_at: datetime


class SwiftPaymentRequest(BaseModel):
    account_id: int
    amount: Decimal
    recipient_bank: str
    recipient_account: str
    swift_code: str
    description: Optional[str] = None

    @validator('amount')
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError('Amount must be positive')
        return v

    @validator('swift_code')
    def validate_swift(cls, v):
        if not v or len(v.strip()) not in [8, 11]:
            raise ValueError('SWIFT code must be 8 or 11 characters')
        return v.strip().upper()


class SwiftPaymentResponse(BaseModel):
    transaction_id: str
    mt103_reference: str
    status: str
    amount: Decimal
    recipient_bank: str
    tracking_number: str
    processed_at: datetime


# AML Schema
class AMLCheckRequest(BaseModel):
    account_id: int
    transaction_type: str
    amount: Decimal

    @validator('amount')
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError('Amount must be positive')
        return v


class AMLCheckResponse(BaseModel):
    account_id: int
    amount: Decimal
    risk_level: str
    alert_triggered: bool
    alert_id: Optional[int] = None
    message: str


# Interest Calculation Schema
class InterestCalculationRequest(BaseModel):
    amount: Decimal
    annual_rate: Optional[Decimal] = Decimal('5.0')
    days: Optional[int] = 30

    @validator('amount')
    def validate_amount(cls, v):
        if v <= 0:
            raise ValueError('Amount must be positive')
        return v

    @validator('annual_rate')
    def validate_rate(cls, v):
        if v < 0 or v > 100:
            raise ValueError('Annual rate must be between 0 and 100')
        return v

    @validator('days')
    def validate_days(cls, v):
        if v <= 0 or v > 365:
            raise ValueError('Days must be between 1 and 365')
        return v


class InterestCalculationResponse(BaseModel):
    principal_amount: Decimal
    annual_rate: Decimal
    days: int
    interest_earned: Decimal
    tax_withheld: Decimal
    net_interest: Decimal


# MFA Schema
class MFARequest(BaseModel):
    customer_id: int
    phone: str
    action: str

    @validator('phone')
    def validate_phone(cls, v):
        if not v or len(v.strip()) < 8:
            raise ValueError('Valid phone number required')
        return v.strip()


class MFAResponse(BaseModel):
    customer_id: int
    verification_code: str
    expires_at: datetime
    status: str


# Generic Response Schemas
class MessageResponse(BaseModel):
    message: str
    status: str = "success"


class HealthResponse(BaseModel):
    status: str
    timestamp: datetime
    database_connected: bool
    version: str = "1.0.0"