import sys
from sqlalchemy import create_engine, text
from config import settings

def test_database_connection():
    print("Verificando conexion a la base de datos...")
    print(f"Host: {settings.db_host}:{settings.db_port}")
    print(f"Base de datos: {settings.db_name}")
    print(f"Usuario: {settings.db_user}")
    print(f"SSL: REQUIRED")
    print("=" * 50)
    
    try:
        engine = create_engine(
            settings.database_url,
            echo=True,
            pool_pre_ping=True,
            connect_args={
                "connect_timeout": 10,
                "read_timeout": 10,
                "write_timeout": 10
            }
        )
        
        print("Intentando conectar...")
        
        with engine.connect() as connection:
            print("Conexion establecida exitosamente!")
            
            result = connection.execute(text("SELECT VERSION() as version, NOW() as current_time"))
            row = result.fetchone()
            
            print(f"Version de MySQL: {row.version}")
            print(f"Hora del servidor: {row.current_time}")
            
            ssl_result = connection.execute(text("SHOW STATUS LIKE 'Ssl_cipher'"))
            ssl_row = ssl_result.fetchone()
            
            if ssl_row and ssl_row[1]:
                print(f"Conexion SSL activa: {ssl_row[1]}")
            else:
                print("Advertencia: No se pudo verificar SSL")
            
            try:
                connection.execute(text("SHOW TABLES"))
                print("Permisos de lectura: OK")
            except Exception as e:
                print(f"Error en permisos de lectura: {e}")
            
            try:
                connection.execute(text("CREATE TEMPORARY TABLE test_permissions (id INT)"))
                connection.execute(text("DROP TEMPORARY TABLE test_permissions"))
                print("Permisos de escritura: OK")
            except Exception as e:
                print(f"Error en permisos de escritura: {e}")
                
        return True
        
    except Exception as e:
        print(f"Error de conexion: {e}")
        print("\nPosibles soluciones:")
        print("1. Verificar que MySQL este ejecutandose")
        print("2. Comprobar las credenciales en el archivo .env")
        print("3. Asegurar que el usuario tenga permisos en la base de datos")
        print("4. Verificar que SSL este habilitado en MySQL")
        print("5. Comprobar la conectividad de red al servidor")
        return False

def check_mysql_requirements():
    print("Verificando dependencias...")
    
    try:
        import pymysql
        print(f"PyMySQL: {pymysql.__version__}")
    except ImportError:
        print("PyMySQL no esta instalado")
        return False
    
    try:
        import sqlalchemy
        print(f"SQLAlchemy: {sqlalchemy.__version__}")
    except ImportError:
        print("SQLAlchemy no esta instalado")
        return False
    
    try:
        import cryptography
        print("Cryptography: Disponible")
    except ImportError:
        print("Cryptography no esta instalado")
        return False
    
    return True

def show_database_config():
    print("Configuracion actual:")
    print(f"   URL de conexion: {settings.database_url}")
    print(f"   Debug: {settings.debug}")
    print(f"   Log level: {settings.log_level}")

def main():
    print("Banking MVP - Verificacion de Base de Datos")
    print("=" * 60)
    
    if not check_mysql_requirements():
        print("\nFaltan dependencias requeridas")
        print("Ejecutar: pip install -r requirements.txt")
        sys.exit(1)
    
    print()
    show_database_config()
    print()
    
    if test_database_connection():
        print("\nConexion a la base de datos exitosa!")
        print("El sistema esta listo para funcionar")
    else:
        print("\nError en la conexion a la base de datos")
        print("El sistema no puede iniciar sin conexion a BD")
        sys.exit(1)

if __name__ == "__main__":
    main()