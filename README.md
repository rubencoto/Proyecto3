# Banking MVP

A complete banking MVP built with FastAPI, SQLAlchemy, and MySQL with SSL support.

## Features

### Core Banking
- **Customer Management (KYC)**: Create customers with sanction list validation
- **Account Management**: Multi-currency accounts (CRC/USD) with status management
- **Transaction Processing**: Deposits, withdrawals, and internal transfers with dual-entry accounting
- **Balance Inquiry**: Real-time balance and statement generation
- **Daily Limits**: Configurable daily transaction limits with alerts
- **Complaints**: Ticket system with SLA tracking

### External Payments (Stubs)
- **SINPE**: Mock Costa Rican instant payment system
- **SWIFT**: Mock international wire transfer with MT103 references

### Compliance & Risk
- **AML/ROS**: Anti-money laundering detection for transactions ? ?10,000,000
- **Interest Calculation**: Tax withholding calculation for interest earnings
- **Regulatory Reporting**: SUGEF/IFRS style reports (JSON/CSV export)

### Digital Channels
- **MFA**: Multi-factor authentication simulation
- **Health Monitoring**: System health checks

### Reports
- **Trial Balance**: Account summaries by currency (JSON/CSV export)
- **Account Statements**: Transaction history with pagination

## Tech Stack

- **FastAPI**: Modern, fast web framework
- **SQLAlchemy**: ORM with MySQL support
- **PyMySQL**: MySQL connector with SSL support
- **Alembic**: Database migrations
- **Pydantic**: Data validation
- **Pytest**: Testing framework
- **Loguru**: Advanced logging
- **Python-dotenv**: Environment management

## Installation

1. **Install Dependencies**:
```bash
pip install -r requirements.txt
```

2. **Configure Environment**:
Create a `.env` file with your database configuration:
```
DB_HOST=localhost
DB_PORT=3306
DB_USER=banking_user
DB_PASS=banking_password
DB_NAME=banking_mvp
```

3. **Setup Database**:
Ensure MySQL is running with SSL enabled and create the database:
```sql
CREATE DATABASE banking_mvp;
CREATE USER 'banking_user'@'%' IDENTIFIED BY 'banking_password';
GRANT ALL PRIVILEGES ON banking_mvp.* TO 'banking_user'@'%';
FLUSH PRIVILEGES;
```

## Running the Application

**Development Server**:
```bash
python main.py
```

**Production (with Uvicorn)**:
```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

The API will be available at `http://localhost:8000`
Interactive documentation at `http://localhost:8000/docs`

## API Endpoints

### Core Banking
- `POST /customers` - Create customer (rejects if sanctioned)
- `POST /accounts` - Create account
- `POST /transactions/deposit` - Process deposit
- `POST /transactions/withdraw` - Process withdrawal  
- `POST /transactions/transfer` - Internal transfer (atomic)
- `GET /accounts/{id}/balance` - Get account balance
- `GET /accounts/{id}/statement` - Get account statement
- `POST /limits/check` - Check daily limits
- `POST /complaints` - Create complaint ticket

### Reports
- `GET /reports/trial-balance` - Generate trial balance (JSON/CSV)
- `GET /reports/regulatory` - Regulatory reports (SUGEF/IFRS)

### External Payments (Stubs)
- `POST /payments/sinpe` - SINPE payment simulation
- `POST /payments/swift` - SWIFT payment simulation

### Compliance (Stubs)
- `POST /aml/check` - AML risk assessment
- `POST /interests/calc` - Interest and tax calculation
- `POST /auth/mfa` - Multi-factor authentication

### Not Implemented (Stubs)
- `POST /cards` - Card management
- `POST /loans` - Loan management

### System
- `GET /health` - System health check

## Testing

Run the complete test suite:
```bash
pytest test_main.py -v
```

### Test Coverage
- ? Sanctioned customer rejection (409)
- ? Insufficient funds validation (400)
- ? Atomic transfers with dual accounting entries
- ? AML alerts for large transactions (? ?10,000,000)
- ? Interest calculation with tax withholding
- ? System health monitoring

## Database Schema

The application automatically creates the following tables:
- `customers` - Customer information and KYC data
- `accounts` - Account details with balances and limits
- `transactions` - All transaction records
- `accounting_entries` - Dual-entry bookkeeping records
- `daily_limits` - Daily transaction limit tracking
- `alerts` - System alerts and notifications
- `complaints` - Customer complaint tickets

## Security Features

- **SSL Required**: All database connections use SSL
- **Sanction List**: Automatic validation against sanction lists
- **Daily Limits**: Configurable transaction limits
- **AML Monitoring**: Large transaction detection
- **Audit Trail**: Complete transaction logging

## Business Rules

1. **Customer Creation**: Rejected if `is_sanction_listed=True`
2. **Transfers**: Must have sufficient balance, updates both accounts atomically
3. **Daily Limits**: Configurable per account, generates alerts when exceeded
4. **AML**: Flags transactions ? ?10,000,000 for review
5. **Complaints**: Automatic SLA calculation with due dates
6. **Accounting**: Every transaction creates proper debit/credit entries

## Production Considerations

- Configure proper SSL certificates for MySQL
- Set up database connection pooling
- Implement proper authentication/authorization
- Add rate limiting and API security
- Configure production logging
- Set up monitoring and alerting
- Implement database backups
- Add data encryption for sensitive fields

## License

This is an educational MVP for banking system development.