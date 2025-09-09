# ?? Banking MVP - Sistema Bancario Completo con Interfaz Web

Un sistema bancario completo desarrollado con **FastAPI**, **SQLAlchemy**, **MySQL** y una **interfaz web moderna** construida con **HTML5**, **CSS3**, **JavaScript** y **Bootstrap 5**.

## ? **NUEVA CARACTERÍSTICA: INTERFAZ WEB COMPLETA**

### ?? **Dashboard Web Interactivo**
- **Dashboard Principal** con estadísticas en tiempo real
- **Gestión de Clientes** con formularios interactivos
- **Administración de Cuentas** con visualización de balances
- **Procesamiento de Transacciones** en tiempo real
- **Sistema de Pagos Externos** (SINPE/SWIFT)
- **Reportería Avanzada** con gráficos y exportación
- **Verificación AML/ROS** automatizada
- **Calculadora de Intereses** integrada

### ?? **Características de la Interfaz**
- ? **Diseño Responsivo** - Compatible con móviles y desktop
- ? **Interfaz Moderna** - Bootstrap 5.3 + Font Awesome 6.4
- ? **Gráficos Interactivos** - Chart.js para visualización de datos
- ? **Validación en Tiempo Real** - Formularios con validación instantánea
- ? **Notificaciones Toast** - Feedback inmediato al usuario
- ? **Navegación Intuitiva** - Menú de navegación claro y consistente
- ? **Estados en Tiempo Real** - Actualización automática de datos
- ? **Exportación de Datos** - CSV y JSON para reportes

## ?? **Inicio Rápido con Interfaz Web**

### 1. **Instalación**
```bash
# Clonar el repositorio
git clone https://github.com/rubencoto/Proyecto3.git
cd Proyecto3

# Instalar dependencias
pip install -r requirements.txt
```

### 2. **Configuración de Base de Datos**
```sql
-- En MySQL
CREATE DATABASE banking_mvp;
CREATE USER 'banking_user'@'%' IDENTIFIED BY 'banking_password';
GRANT ALL PRIVILEGES ON banking_mvp.* TO 'banking_user'@'%';
FLUSH PRIVILEGES;
```

### 3. **Lanzar la Aplicación Web**
```bash
# Opción 1: Launcher web completo
python web_launcher.py

# Opción 2: Directamente
python main.py

# Opción 3: Con uvicorn
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. **Acceder a la Interfaz Web**
```
?? Dashboard Principal: http://localhost:8000/
?? API Documentation: http://localhost:8000/docs
?? API Redoc: http://localhost:8000/redoc
```

## ?? **Páginas Web Disponibles**

| Página | URL | Descripción |
|--------|-----|-------------|
| **Dashboard** | `/` | Panel principal con estadísticas y acciones rápidas |
| **Clientes** | `/customers-page` | Gestión completa de clientes con KYC |
| **Cuentas** | `/accounts-page` | Administración de cuentas bancarias |
| **Transacciones** | `/transactions-page` | Procesamiento de depósitos, retiros y transferencias |
| **Pagos** | `/payments-page` | Sistema de pagos externos (SINPE/SWIFT) |
| **Reportes** | `/reports-page` | Reportería financiera y regulatoria |

## ??? **Stack Tecnológico**

### **Frontend**
- **HTML5** - Estructura semántica moderna
- **CSS3** - Estilos avanzados con variables CSS
- **JavaScript ES6+** - Lógica interactiva y asíncrona
- **Bootstrap 5.3** - Framework CSS responsivo
- **Font Awesome 6.4** - Iconografía profesional
- **Chart.js** - Gráficos interactivos

### **Backend**
- **FastAPI** - Framework web moderno y rápido
- **SQLAlchemy 2.0** - ORM avanzado
- **Alembic** - Migraciones de base de datos
- **Pydantic** - Validación de datos
- **Jinja2** - Motor de templates
- **PyMySQL** - Conector MySQL con SSL

### **Base de Datos**
- **MySQL 8.0+** - Base de datos relacional
- **SSL/TLS** - Conexiones seguras
- **Transacciones ACID** - Integridad de datos

## ?? **Funcionalidades Principales**

### ?? **Dashboard Interactivo**
- Resumen de clientes, cuentas y balances
- Gráficos de distribución por moneda
- Acciones rápidas y atajos
- Monitoreo de estado del sistema
- Actividad reciente en tiempo real

### ?? **Gestión de Clientes**
- **Registro KYC** con validación de listas de sanciones
- **Búsqueda y filtros** avanzados
- **Vista detallada** con cuentas asociadas
- **Estados de clientes** (activo/sancionado)
- **Auditoría completa** de cambios

### ?? **Administración de Cuentas**
- **Creación de cuentas** multi-moneda (CRC/USD)
- **Consulta de balances** en tiempo real
- **Límites diarios** configurables
- **Estados de cuenta** detallados
- **Trial Balance** automático

### ?? **Sistema de Transacciones**
- **Depósitos, Retiros y Transferencias**
- **Formularios rápidos** para operaciones frecuentes
- **Validación AML/ROS** automática
- **Límites diarios** con alertas
- **Contabilidad de doble entrada**

### ?? **Pagos Externos**
- **SINPE** - Pagos instantáneos con número de teléfono
- **SWIFT** - Transferencias internacionales
- **Validación en tiempo real** de datos
- **Comprobantes digitales**
- **Seguimiento de estados**

### ?? **Reportería Avanzada**
- **Trial Balance** con exportación CSV/JSON
- **Reportes SUGEF e IFRS** regulatorios
- **Gráficos interactivos** de balances y cuentas
- **Calculadora de intereses** con retención fiscal
- **Alertas AML** con seguimiento

## ?? **Seguridad y Compliance**

### **AML/ROS (Anti-Money Laundering)**
- ? Detección automática de transacciones ? ?10,000,000
- ? Verificación contra listas de sanciones
- ? Alertas en tiempo real
- ? Reportería de actividades sospechosas

### **Controles Bancarios**
- ? Límites diarios por cuenta
- ? Estados de cuenta (activa/bloqueada/cerrada)
- ? Validación de fondos suficientes
- ? Atomicidad en transferencias

### **Seguridad Técnica**
- ? Conexiones SSL/TLS obligatorias
- ? Validación de datos con Pydantic
- ? Transacciones ACID en base de datos
- ? Logging completo de operaciones

## ?? **Testing y Calidad**

### **Suite de Pruebas**
```bash
# Ejecutar todas las pruebas
pytest test_main.py -v

# Pruebas específicas
pytest test_main.py::test_customer_creation -v
pytest test_main.py::test_transaction_processing -v
pytest test_main.py::test_aml_detection -v
```

### **Demostración del Sistema**
```bash
# Script de demostración completa
python demo.py

# Verificación de conexión DB
python simple_check.py
```

## ?? **API REST Completa**

La aplicación mantiene compatibilidad total con la API REST:

### **Endpoints Principales**
```
POST /customers          # Crear cliente
GET  /customers          # Listar clientes
POST /accounts           # Crear cuenta
GET  /accounts           # Listar cuentas
GET  /accounts/{id}/balance    # Consultar balance
POST /transactions/deposit     # Depósito
POST /transactions/withdraw    # Retiro  
POST /transactions/transfer    # Transferencia
POST /payments/sinpe     # Pago SINPE
POST /payments/swift     # Transferencia SWIFT
GET  /reports/trial-balance    # Trial Balance
POST /aml/check          # Verificación AML
```

## ??? **Configuración**

### **Variables de Entorno (.env)**
```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_USER=banking_user
DB_PASS=banking_password
DB_NAME=banking_mvp

# Application Configuration
APP_HOST=0.0.0.0
APP_PORT=8000
DEBUG=True

# Logging
LOG_LEVEL=INFO
```

### **Personalización**
- **Temas CSS** - Variables CSS fácilmente modificables
- **Límites de transacción** - Configurables por tipo de cuenta
- **Tasas de interés** - Parametrizables en la calculadora
- **Reportes** - Templates personalizables

## ?? **Despliegue**

### **Desarrollo**
```bash
python web_launcher.py
```

### **Producción**
```bash
# Con Gunicorn
gunicorn main:app -w 4 -k uvicorn.workers.UvicornWorker -b 0.0.0.0:8000

# Con Docker (ejemplo)
docker build -t banking-mvp .
docker run -p 8000:8000 banking-mvp
```

## ?? **Soporte y Contacto**

- **Repositorio**: https://github.com/rubencoto/Proyecto3
- **Issues**: Reportar bugs y solicitudes de features
- **Documentación**: Disponible en `/docs` cuando la app esté ejecutándose

## ?? **Licencia**

Proyecto educativo desarrollado para demostrar capacidades de desarrollo full-stack con Python, FastAPI y tecnologías web modernas.

---

## ?? **¡Disfruta del Sistema Bancario Completo!**

La combinación de una **API REST robusta** con una **interfaz web moderna** proporciona una experiencia completa tanto para desarrolladores como para usuarios finales. El sistema está diseñado para ser escalable, seguro y fácil de mantener.

**¡Explora todas las funcionalidades en http://localhost:8000/ después de iniciar la aplicación!**