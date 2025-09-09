#!/usr/bin/env python3
"""
Launcher script for Banking MVP with Web Interface
Runs the complete banking system with HTML/CSS/JS frontend
"""

import os
import sys
import subprocess
import time
from pathlib import Path

def check_dependencies():
    """Check if all required dependencies are installed"""
    print("?? Verificando dependencias...")
    
    required_packages = [
        'fastapi', 'uvicorn', 'sqlalchemy', 'pymysql', 
        'cryptography', 'pydantic', 'pydantic-settings',
        'jinja2', 'python-multipart'
    ]
    
    missing_packages = []
    
    for package in required_packages:
        try:
            __import__(package.replace('-', '_'))
            print(f"? {package}")
        except ImportError:
            print(f"? {package}")
            missing_packages.append(package)
    
    if missing_packages:
        print(f"\n? Faltan dependencias: {', '.join(missing_packages)}")
        print("Instalar con: pip install -r requirements.txt")
        return False
    
    print("? Todas las dependencias están instaladas")
    return True

def check_directories():
    """Create required directories if they don't exist"""
    print("?? Verificando estructura de directorios...")
    
    required_dirs = ['templates', 'static', 'static/css', 'static/js']
    
    for dir_name in required_dirs:
        dir_path = Path(dir_name)
        if not dir_path.exists():
            print(f"?? Creando directorio: {dir_name}")
            dir_path.mkdir(parents=True, exist_ok=True)
        else:
            print(f"? {dir_name}")
    
    return True

def check_config():
    """Check configuration files"""
    print("??  Verificando configuración...")
    
    config_files = ['.env', 'config.py']
    
    for file_name in config_files:
        file_path = Path(file_name)
        if file_path.exists():
            print(f"? {file_name}")
        else:
            print(f"? {file_name} no encontrado")
            return False
    
    return True

def show_startup_info():
    """Show startup information"""
    print("=" * 60)
    print("?? BANKING MVP - INTERFAZ WEB COMPLETA")
    print("=" * 60)
    print()
    print("?? Características de la Interfaz Web:")
    print("   • Dashboard interactivo con gráficos")
    print("   • Gestión completa de clientes")
    print("   • Administración de cuentas bancarias")
    print("   • Procesamiento de transacciones en tiempo real")
    print("   • Sistema de pagos externos (SINPE/SWIFT)")
    print("   • Reportería y análisis financiero")
    print("   • Verificación AML/ROS automática")
    print("   • Calculadora de intereses")
    print("   • Interfaz responsiva (móvil/desktop)")
    print()
    print("?? Stack Tecnológico:")
    print("   • Frontend: HTML5, CSS3, JavaScript ES6+")
    print("   • Styling: Bootstrap 5.3")
    print("   • Icons: Font Awesome 6.4")
    print("   • Charts: Chart.js")
    print("   • Backend: FastAPI + SQLAlchemy")
    print("   • Templates: Jinja2")
    print("   • Database: MySQL con SSL")
    print()

def main():
    """Main function to launch the banking MVP web application"""
    show_startup_info()
    
    # Check all requirements
    print("?? Ejecutando verificaciones previas...")
    print()
    
    if not check_dependencies():
        sys.exit(1)
    
    if not check_directories():
        sys.exit(1)
    
    if not check_config():
        sys.exit(1)
    
    print()
    print("=" * 60)
    print("?? INICIANDO SERVIDOR WEB")
    print("=" * 60)
    print()
    print("?? URLs Disponibles:")
    print("   • Dashboard Principal: http://localhost:8000/")
    print("   • Gestión de Clientes: http://localhost:8000/customers-page")
    print("   • Administración de Cuentas: http://localhost:8000/accounts-page")
    print("   • Transacciones: http://localhost:8000/transactions-page")
    print("   • Pagos Externos: http://localhost:8000/payments-page")
    print("   • Reportes: http://localhost:8000/reports-page")
    print("   • API Documentation: http://localhost:8000/docs")
    print("   • API Redoc: http://localhost:8000/redoc")
    print()
    print("?? Funcionalidades Web:")
    print("   • Interfaz completamente interactiva")
    print("   • Formularios de validación en tiempo real")
    print("   • Notificaciones toast")
    print("   • Actualizaciones automáticas")
    print("   • Exportación de reportes (CSV/JSON)")
    print("   • Estados en tiempo real")
    print()
    print("??  Notas Importantes:")
    print("   • Asegurese de tener MySQL ejecutándose")
    print("   • La base de datos se creará automáticamente")
    print("   • SSL es requerido para la conexión MySQL")
    print("   • Presione Ctrl+C para detener el servidor")
    print()
    print("=" * 60)
    print()
    
    # Start the application
    try:
        print("?? Iniciando FastAPI con interfaz web...")
        print()
        
        # Import and run the application
        import uvicorn
        from config import settings
        
        uvicorn.run(
            "main:app",
            host=settings.app_host,
            port=settings.app_port,
            reload=settings.debug,
            log_level=settings.log_level.lower(),
            access_log=True
        )
        
    except ImportError as e:
        print(f"? Error importando módulos: {e}")
        print("Verificar que todas las dependencias estén instaladas")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n")
        print("?? Servidor detenido por el usuario")
        print("?? ¡Gracias por usar Banking MVP!")
    except Exception as e:
        print(f"? Error inesperado: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()