from fastapi import FastAPI, Depends, HTTPException, Query, Request
from fastapi.responses import StreamingResponse, HTMLResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from sqlalchemy import text
from database import get_db, create_tables, engine
from schemas import *
from services import (
    CustomerService, AccountService, TransactionService, 
    LimitService, ComplaintService, ReportService, AMLService
)
from datetime import datetime, timedelta
from decimal import Decimal
import uuid
import random
import io
import csv
import json
from loguru import logger
from config import settings

# Configure logging
logger.add("banking_mvp.log", rotation="1 day", retention="30 days", level=settings.log_level)

# Create FastAPI app
app = FastAPI(
    title="Banking MVP",
    description="A complete banking MVP with FastAPI, SQLAlchemy, and MySQL",
    version="1.0.0"
)

# Mount static files and templates
app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")

# Create tables on startup
@app.on_event("startup")
async def startup_event():
    create_tables()
    logger.info("Banking MVP started successfully")


# Web Interface Routes
@app.get("/", response_class=HTMLResponse)
async def dashboard(request: Request):
    """Main dashboard page"""
    return templates.TemplateResponse("dashboard.html", {"request": request})

@app.get("/customers-page", response_class=HTMLResponse)
async def customers_page(request: Request):
    """Customer management page"""
    return templates.TemplateResponse("customers.html", {"request": request})

@app.get("/accounts-page", response_class=HTMLResponse)
async def accounts_page(request: Request):
    """Account management page"""
    return templates.TemplateResponse("accounts.html", {"request": request})

@app.get("/transactions-page", response_class=HTMLResponse)
async def transactions_page(request: Request):
    """Transaction management page"""
    return templates.TemplateResponse("transactions.html", {"request": request})

@app.get("/reports-page", response_class=HTMLResponse)
async def reports_page(request: Request):
    """Reports page"""
    return templates.TemplateResponse("reports.html", {"request": request})

@app.get("/payments-page", response_class=HTMLResponse)
async def payments_page(request: Request):
    """Payments page"""
    return templates.TemplateResponse("payments.html", {"request": request})


# Health Check
@app.get("/health", response_model=HealthResponse)
async def health_check(db: Session = Depends(get_db)):
    try:
        # Test database connection
        db.execute(text("SELECT 1"))
        db_connected = True
    except Exception as e:
        logger.error(f"Database connection failed: {e}")
        db_connected = False
    
    return HealthResponse(
        status="healthy" if db_connected else "unhealthy",
        timestamp=datetime.now(),
        database_connected=db_connected
    )


# Customer Endpoints
@app.post("/customers", response_model=CustomerResponse)
async def create_customer(customer: CustomerCreate, db: Session = Depends(get_db)):
    try:
        created_customer = CustomerService.create_customer(db, customer)
        logger.info(f"Customer created: {created_customer.id}")
        return created_customer
    except ValueError as e:
        logger.warning(f"Customer creation failed: {str(e)}")
        raise HTTPException(status_code=409 if "sanction" in str(e).lower() else 400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error creating customer: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


@app.get("/customers", response_model=List[CustomerResponse])
async def get_customers(db: Session = Depends(get_db)):
    """Get all customers"""
    from database import Customer
    customers = db.query(Customer).all()
    return customers


# Account Endpoints
@app.post("/accounts", response_model=AccountResponse)
async def create_account(account: AccountCreate, db: Session = Depends(get_db)):
    try:
        created_account = AccountService.create_account(db, account)
        logger.info(f"Account created: {created_account.id}")
        return created_account
    except ValueError as e:
        logger.warning(f"Account creation failed: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error creating account: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


@app.get("/accounts", response_model=List[AccountResponse])
async def get_accounts(db: Session = Depends(get_db)):
    """Get all accounts"""
    from database import Account
    accounts = db.query(Account).all()
    return accounts


@app.get("/accounts/{account_id}/balance", response_model=BalanceResponse)
async def get_account_balance(account_id: int, db: Session = Depends(get_db)):
    account = AccountService.get_account_balance(db, account_id)
    if not account:
        raise HTTPException(status_code=404, detail="Account not found")
    
    return BalanceResponse(
        account_id=account.id,
        account_number=account.account_number,
        currency=account.currency,
        balance=account.balance,
        status=account.status
    )


@app.get("/accounts/{account_id}/statement", response_model=StatementResponse)
async def get_account_statement(
    account_id: int, 
    limit: int = Query(50, ge=1, le=500),
    db: Session = Depends(get_db)
):
    account, transactions = AccountService.get_account_statement(db, account_id, limit)
    if not account:
        raise HTTPException(status_code=404, detail="Account not found")
    
    return StatementResponse(
        account_id=account.id,
        account_number=account.account_number,
        currency=account.currency,
        current_balance=account.balance,
        transactions=transactions
    )


# Transaction Endpoints
@app.post("/transactions/deposit", response_model=TransactionResponse)
async def deposit(transaction: TransactionCreate, db: Session = Depends(get_db)):
    if transaction.transaction_type != TransactionType.DEPOSIT:
        raise HTTPException(status_code=400, detail="Invalid transaction type for deposit")
    
    try:
        created_transaction = TransactionService.process_transaction(db, transaction)
        logger.info(f"Deposit processed: {created_transaction.id}")
        return created_transaction
    except ValueError as e:
        logger.warning(f"Deposit failed: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error processing deposit: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


@app.post("/transactions/withdraw", response_model=TransactionResponse)
async def withdraw(transaction: TransactionCreate, db: Session = Depends(get_db)):
    if transaction.transaction_type != TransactionType.WITHDRAW:
        raise HTTPException(status_code=400, detail="Invalid transaction type for withdrawal")
    
    try:
        created_transaction = TransactionService.process_transaction(db, transaction)
        logger.info(f"Withdrawal processed: {created_transaction.id}")
        return created_transaction
    except ValueError as e:
        logger.warning(f"Withdrawal failed: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error processing withdrawal: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


@app.post("/transactions/transfer", response_model=TransactionResponse)
async def transfer(transaction: TransactionCreate, db: Session = Depends(get_db)):
    if transaction.transaction_type != TransactionType.TRANSFER:
        raise HTTPException(status_code=400, detail="Invalid transaction type for transfer")
    
    try:
        created_transaction = TransactionService.process_transaction(db, transaction)
        logger.info(f"Transfer processed: {created_transaction.id}")
        return created_transaction
    except ValueError as e:
        logger.warning(f"Transfer failed: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error processing transfer: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


# Limits and Alerts
@app.post("/limits/check", response_model=LimitCheckResponse)
async def check_limit(request: LimitCheckRequest, db: Session = Depends(get_db)):
    try:
        result = LimitService.check_limit(db, request)
        return LimitCheckResponse(**result)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error checking limit: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


# Complaints
@app.post("/complaints", response_model=ComplaintResponse)
async def create_complaint(complaint: ComplaintCreate, db: Session = Depends(get_db)):
    try:
        created_complaint = ComplaintService.create_complaint(db, complaint)
        logger.info(f"Complaint created: {created_complaint.id}")
        return created_complaint
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error creating complaint: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


# Reports
@app.get("/reports/trial-balance")
async def get_trial_balance(
    format: str = Query("json", regex="^(json|csv)$"),
    db: Session = Depends(get_db)
):
    try:
        trial_balance = ReportService.generate_trial_balance(db)
        
        if format == "csv":
            # Generate CSV
            output = io.StringIO()
            writer = csv.writer(output)
            
            # Write header
            writer.writerow(["Account ID", "Account Number", "Currency", "Balance"])
            
            # Write data
            for account in trial_balance["accounts"]:
                writer.writerow([
                    account["account_id"],
                    account["account_number"],
                    account["currency"],
                    str(account["balance"])
                ])
            
            # Add summary
            writer.writerow([])
            writer.writerow(["Currency", "Total Balance"])
            for currency, total in trial_balance["balances_by_currency"].items():
                writer.writerow([currency, str(total)])
            
            output.seek(0)
            return StreamingResponse(
                io.StringIO(output.getvalue()),
                media_type="text/csv",
                headers={"Content-Disposition": "attachment; filename=trial_balance.csv"}
            )
        
        return trial_balance
    except Exception as e:
        logger.error(f"Error generating trial balance: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


# Payment Stubs
@app.post("/payments/sinpe", response_model=SinpePaymentResponse)
async def sinpe_payment(payment: SinpePaymentRequest, db: Session = Depends(get_db)):
    # Validate account and balance
    account = AccountService.get_account(db, payment.account_id)
    if not account:
        raise HTTPException(status_code=404, detail="Account not found")
    
    if account.balance < payment.amount:
        raise HTTPException(status_code=400, detail="Insufficient funds")
    
    # Mock SINPE processing
    transaction_id = str(uuid.uuid4())
    confirmation_code = ''.join(random.choices('0123456789', k=8))
    
    logger.info(f"SINPE payment mock processed: {transaction_id}")
    
    return SinpePaymentResponse(
        transaction_id=transaction_id,
        status="COMPLETED",
        amount=payment.amount,
        recipient_phone=payment.recipient_phone,
        confirmation_code=confirmation_code,
        processed_at=datetime.now()
    )


@app.post("/payments/swift", response_model=SwiftPaymentResponse)
async def swift_payment(payment: SwiftPaymentRequest, db: Session = Depends(get_db)):
    # Validate account and balance
    account = AccountService.get_account(db, payment.account_id)
    if not account:
        raise HTTPException(status_code=404, detail="Account not found")
    
    if account.balance < payment.amount:
        raise HTTPException(status_code=400, detail="Insufficient funds")
    
    # Mock SWIFT processing
    transaction_id = str(uuid.uuid4())
    mt103_reference = f"MT103{random.randint(100000, 999999)}"
    tracking_number = f"TRK{random.randint(10000000, 99999999)}"
    
    logger.info(f"SWIFT payment mock processed: {transaction_id}")
    
    return SwiftPaymentResponse(
        transaction_id=transaction_id,
        mt103_reference=mt103_reference,
        status="PENDING",
        amount=payment.amount,
        recipient_bank=payment.recipient_bank,
        tracking_number=tracking_number,
        processed_at=datetime.now()
    )


# AML/ROS Stub
@app.post("/aml/check", response_model=AMLCheckResponse)
async def aml_check(request: AMLCheckRequest, db: Session = Depends(get_db)):
    try:
        result = AMLService.check_aml(db, request)
        logger.info(f"AML check completed for account {request.account_id}: {result['risk_level']}")
        return AMLCheckResponse(**result)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Unexpected error in AML check: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")


# Interest Calculation Stub
@app.post("/interests/calc", response_model=InterestCalculationResponse)
async def calculate_interest(request: InterestCalculationRequest):
    # Calculate interest: (Principal * Rate * Days) / (365 * 100)
    interest_earned = (request.amount * request.annual_rate * request.days) / (Decimal('365') * Decimal('100'))
    
    # Tax withholding: 8% on interest (Costa Rica standard)
    tax_rate = Decimal('8.0')
    tax_withheld = (interest_earned * tax_rate) / Decimal('100')
    net_interest = interest_earned - tax_withheld
    
    return InterestCalculationResponse(
        principal_amount=request.amount,
        annual_rate=request.annual_rate,
        days=request.days,
        interest_earned=interest_earned,
        tax_withheld=tax_withheld,
        net_interest=net_interest
    )


# MFA Stub
@app.post("/auth/mfa", response_model=MFAResponse)
async def mfa_verification(request: MFARequest, db: Session = Depends(get_db)):
    # Validate customer exists
    customer = CustomerService.get_customer(db, request.customer_id)
    if not customer:
        raise HTTPException(status_code=404, detail="Customer not found")
    
    # Generate mock verification code
    verification_code = ''.join(random.choices('0123456789', k=6))
    expires_at = datetime.now() + timedelta(minutes=5)
    
    logger.info(f"MFA verification code generated for customer {request.customer_id}")
    
    return MFAResponse(
        customer_id=request.customer_id,
        verification_code=verification_code,
        expires_at=expires_at,
        status="SENT"
    )


# Regulatory Reports Stub
@app.get("/reports/regulatory")
async def regulatory_report(
    format: str = Query("json", regex="^(json|csv)$"),
    report_type: str = Query("SUGEF", regex="^(SUGEF|IFRS)$"),
    db: Session = Depends(get_db)
):
    # Mock regulatory data
    regulatory_data = {
        "report_type": report_type,
        "generated_at": datetime.now().isoformat(),
        "period": f"{datetime.now().year}-{datetime.now().month:02d}",
        "institution_code": "001",
        "data": [
            {"account_type": "DEPOSITS", "amount": "50000000.00", "currency": "CRC"},
            {"account_type": "LOANS", "amount": "35000000.00", "currency": "CRC"},
            {"account_type": "DEPOSITS", "amount": "150000.00", "currency": "USD"},
            {"account_type": "LOANS", "amount": "100000.00", "currency": "USD"}
        ]
    }
    
    if format == "csv":
        output = io.StringIO()
        writer = csv.writer(output)
        
        # Write header
        writer.writerow(["Report Type", "Period", "Institution Code", "Account Type", "Amount", "Currency"])
        
        # Write data
        for item in regulatory_data["data"]:
            writer.writerow([
                regulatory_data["report_type"],
                regulatory_data["period"],
                regulatory_data["institution_code"],
                item["account_type"],
                item["amount"],
                item["currency"]
            ])
        
        output.seek(0)
        return StreamingResponse(
            io.StringIO(output.getvalue()),
            media_type="text/csv",
            headers={"Content-Disposition": f"attachment; filename={report_type.lower()}_report.csv"}
        )
    
    return regulatory_data


# Cards Stub
@app.post("/cards", response_model=MessageResponse)
async def create_card():
    return MessageResponse(
        message="Not implemented yet",
        status="not_implemented"
    )


# Loans Stub
@app.post("/loans", response_model=MessageResponse)
async def create_loan():
    return MessageResponse(
        message="Not implemented yet",
        status="not_implemented"
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=settings.debug
    )