import os
from pathlib import Path

def check_env_file():
    """Verifica si el archivo .env existe y tiene las variables necesarias"""
    env_path = Path(".env")
    
    if not env_path.exists():
        print("? Archivo .env no encontrado")
        return False
    
    print("? Archivo .env encontrado")
    
    # Leer variables del archivo .env
    env_vars = {}
    with open(env_path, 'r') as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                key, value = line.split('=', 1)
                env_vars[key] = value
    
    # Variables requeridas
    required_vars = [
        'DB_HOST', 'DB_PORT', 'DB_USER', 'DB_PASS', 'DB_NAME'
    ]
    
    print("\nConfiguracion de base de datos:")
    missing_vars = []
    
    for var in required_vars:
        if var in env_vars:
            if var == 'DB_PASS':
                print(f"  {var}: {'*' * len(env_vars[var])}")
            else:
                print(f"  {var}: {env_vars[var]}")
        else:
            print(f"  {var}: ? FALTA")
            missing_vars.append(var)
    
    if missing_vars:
        print(f"\n? Variables faltantes: {', '.join(missing_vars)}")
        return False
    else:
        print("\n? Todas las variables de configuracion estan presentes")
        return True

def check_connection_string():
    """Muestra como seria la cadena de conexion"""
    try:
        # Simular la importacion de settings
        import sys
        sys.path.append('.')
        from config import settings
        
        print(f"\nCadena de conexion generada:")
        # Ocultar la contraseña en la URL
        db_url = settings.database_url
        if '@' in db_url:
            parts = db_url.split('@')
            user_part = parts[0]
            if ':' in user_part:
                user, password = user_part.split('://')[-1].split(':')
                masked_url = db_url.replace(f':{password}@', ':****@')
                print(f"  {masked_url}")
            else:
                print(f"  {db_url}")
        else:
            print(f"  {db_url}")
        
        print(f"\nDetalles de conexion:")
        print(f"  Host: {settings.db_host}")
        print(f"  Puerto: {settings.db_port}")
        print(f"  Base de datos: {settings.db_name}")
        print(f"  Usuario: {settings.db_user}")
        print(f"  SSL: REQUIRED")
        
        return True
        
    except Exception as e:
        print(f"\n? Error al cargar configuracion: {e}")
        return False

def check_requirements():
    """Verifica si existe el archivo requirements.txt"""
    req_path = Path("requirements.txt")
    
    if not req_path.exists():
        print("? Archivo requirements.txt no encontrado")
        return False
    
    print("? Archivo requirements.txt encontrado")
    
    with open(req_path, 'r') as f:
        packages = [line.strip() for line in f if line.strip() and not line.startswith('#')]
    
    print(f"?? Paquetes requeridos ({len(packages)}):")
    for package in packages:
        print(f"  - {package}")
    
    return True

def main():
    print("=" * 60)
    print("VERIFICACION DE CONFIGURACION - Banking MVP")
    print("=" * 60)
    
    # Verificar archivo .env
    print("\n1. Verificando archivo de configuracion (.env):")
    env_ok = check_env_file()
    
    # Verificar requirements.txt
    print("\n2. Verificando dependencias:")
    req_ok = check_requirements()
    
    # Verificar cadena de conexion
    print("\n3. Verificando configuracion de base de datos:")
    config_ok = check_connection_string()
    
    # Resumen
    print("\n" + "=" * 60)
    print("RESUMEN:")
    print(f"  Configuracion .env: {'? OK' if env_ok else '? ERROR'}")
    print(f"  Requirements.txt: {'? OK' if req_ok else '? ERROR'}")
    print(f"  Config BD: {'? OK' if config_ok else '? ERROR'}")
    
    if env_ok and req_ok and config_ok:
        print("\n?? Configuracion completa!")
        print("\nPasos siguientes:")
        print("1. Instalar dependencias: pip install -r requirements.txt")
        print("2. Configurar MySQL server con SSL habilitado")
        print("3. Crear base de datos y usuario con permisos")
        print("4. Ejecutar: python main.py")
    else:
        print("\n? Hay errores en la configuracion que deben corregirse")
    
    print("\n" + "=" * 60)

if __name__ == "__main__":
    main()