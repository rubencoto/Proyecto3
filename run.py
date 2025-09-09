#!/usr/bin/env python3
"""
Banking MVP Startup Script
Initializes the database and starts the FastAPI server
"""

import sys
import subprocess
from pathlib import Path
from database import create_tables, engine
from sqlalchemy import text
from config import settings
from loguru import logger

def check_dependencies():
    """Check if all required packages are installed"""
    try:
        import fastapi
        import sqlalchemy
        import pymysql
        import pydantic
        import loguru
        print("? All dependencies are installed")
        return True
    except ImportError as e:
        print(f"? Missing dependency: {e}")
        print("Run: pip install -r requirements.txt")
        return False

def test_database_connection():
    """Test database connection"""
    try:
        with engine.connect() as connection:
            result = connection.execute(text("SELECT 1"))
            print("? Database connection successful")
            return True
    except Exception as e:
        print(f"? Database connection failed: {e}")
        print("Please check your database configuration in .env file")
        return False

def initialize_database():
    """Create database tables"""
    try:
        create_tables()
        print("? Database tables created successfully")
        return True
    except Exception as e:
        print(f"? Failed to create database tables: {e}")
        return False

def start_server():
    """Start the FastAPI server"""
    try:
        import uvicorn
        print(f"?? Starting server on {settings.app_host}:{settings.app_port}")
        print(f"?? API Documentation: http://{settings.app_host}:{settings.app_port}/docs")
        uvicorn.run(
            "main:app",
            host=settings.app_host,
            port=settings.app_port,
            reload=settings.debug
        )
    except KeyboardInterrupt:
        print("\n?? Server stopped by user")
    except Exception as e:
        print(f"? Failed to start server: {e}")

def main():
    """Main startup function"""
    print("?? Banking MVP - Starting Up...")
    print("=" * 50)
    
    # Check dependencies
    if not check_dependencies():
        sys.exit(1)
    
    # Test database connection
    if not test_database_connection():
        sys.exit(1)
    
    # Initialize database
    if not initialize_database():
        sys.exit(1)
    
    print("=" * 50)
    print("? All checks passed - Ready to start!")
    print("=" * 50)
    
    # Start server
    start_server()

if __name__ == "__main__":
    main()