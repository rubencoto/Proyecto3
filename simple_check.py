import os

def main():
    print("Banking MVP - Verificacion de Configuracion")
    print("=" * 50)
    
    # Verificar archivo .env
    if os.path.exists(".env"):
        print("? Archivo .env encontrado")
        
        with open(".env", "r") as f:
            content = f.read()
            
        if "DB_HOST" in content:
            print("? DB_HOST configurado")
        if "DB_PORT" in content:
            print("? DB_PORT configurado")
        if "DB_USER" in content:
            print("? DB_USER configurado")
        if "DB_PASS" in content:
            print("? DB_PASS configurado")
        if "DB_NAME" in content:
            print("? DB_NAME configurado")
    else:
        print("? Archivo .env NO encontrado")
    
    # Verificar requirements.txt
    if os.path.exists("requirements.txt"):
        print("? requirements.txt encontrado")
    else:
        print("? requirements.txt NO encontrado")
    
    # Intentar cargar configuracion
    try:
        from config import settings
        print("? Modulo config cargado exitosamente")
        print(f"  Host: {settings.db_host}")
        print(f"  Puerto: {settings.db_port}")
        print(f"  Base de datos: {settings.db_name}")
        print(f"  Usuario: {settings.db_user}")
        
        # Mostrar URL de conexion (sin password)
        url = settings.database_url
        if settings.db_pass in url:
            url = url.replace(settings.db_pass, "****")
        print(f"  URL: {url}")
        
    except Exception as e:
        print(f"? Error cargando configuracion: {e}")
    
    print("\nPara verificar conexion real a MySQL:")
    print("1. Instalar: pip install -r requirements.txt")
    print("2. Configurar MySQL con SSL")
    print("3. Ejecutar: python test_db_connection.py")

if __name__ == "__main__":
    main()