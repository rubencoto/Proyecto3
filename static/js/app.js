// Banking MVP JavaScript Application

// Global variables
let toast = null;

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap components
    initializeToast();
    
    // Setup global error handling
    window.addEventListener('unhandledrejection', function(event) {
        console.error('Unhandled promise rejection:', event.reason);
        showToast('Error inesperado en la aplicación', 'error');
    });
});

// Toast notification system
function initializeToast() {
    const toastElement = document.getElementById('toast');
    if (toastElement) {
        toast = new bootstrap.Toast(toastElement);
    }
}

function showToast(message, type = 'info') {
    if (!toast) {
        console.log(message);
        return;
    }
    
    const toastBody = document.getElementById('toast-body');
    const toastHeader = document.querySelector('#toast .toast-header');
    
    // Set message
    toastBody.textContent = message;
    
    // Set icon and color based on type
    let icon, iconClass;
    switch (type) {
        case 'success':
            icon = 'fa-check-circle';
            iconClass = 'text-success';
            break;
        case 'error':
        case 'danger':
            icon = 'fa-exclamation-triangle';
            iconClass = 'text-danger';
            break;
        case 'warning':
            icon = 'fa-exclamation-circle';
            iconClass = 'text-warning';
            break;
        default:
            icon = 'fa-info-circle';
            iconClass = 'text-primary';
    }
    
    const iconElement = toastHeader.querySelector('i');
    iconElement.className = `fas ${icon} ${iconClass} me-2`;
    
    toast.show();
}

// API Helper functions
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    const mergedOptions = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(url, mergedOptions);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.detail || `HTTP error! status: ${response.status}`);
        }
        
        return { data, response };
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// Utility functions
function formatCurrency(amount, currency = 'CRC') {
    const symbol = currency === 'USD' ? '$' : '?';
    return `${symbol} ${parseFloat(amount).toLocaleString()}`;
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('es-CR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePhone(phone) {
    const re = /^[0-9]{8,15}$/;
    return re.test(phone.replace(/\s/g, ''));
}

function validateAmount(amount) {
    return !isNaN(amount) && parseFloat(amount) > 0;
}

// Form validation helpers
function validateForm(formId, rules) {
    const form = document.getElementById(formId);
    let isValid = true;
    
    for (const [fieldId, rule] of Object.entries(rules)) {
        const field = document.getElementById(fieldId);
        const value = field.value.trim();
        
        // Remove previous validation states
        field.classList.remove('is-valid', 'is-invalid');
        
        // Check if field is required and empty
        if (rule.required && !value) {
            field.classList.add('is-invalid');
            isValid = false;
            continue;
        }
        
        // Check specific validation rules
        if (value && rule.validator && !rule.validator(value)) {
            field.classList.add('is-invalid');
            isValid = false;
            continue;
        }
        
        // Field is valid
        if (value) {
            field.classList.add('is-valid');
        }
    }
    
    return isValid;
}

function clearFormValidation(formId) {
    const form = document.getElementById(formId);
    const fields = form.querySelectorAll('.form-control, .form-select');
    
    fields.forEach(field => {
        field.classList.remove('is-valid', 'is-invalid');
    });
}

// Loading states
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Cargando...</span>
                </div>
            </div>
        `;
    }
}

function hideLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '';
    }
}

// Navigation helpers
function setActiveNavItem(page) {
    // Remove active class from all nav items
    document.querySelectorAll('.navbar-nav .nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    // Add active class to current page
    const currentLink = document.querySelector(`[href="${page}"]`);
    if (currentLink) {
        currentLink.classList.add('active');
    }
}

// URL parameter helpers
function getUrlParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

function setUrlParameter(name, value) {
    const url = new URL(window.location);
    url.searchParams.set(name, value);
    window.history.pushState({}, '', url);
}

// Data formatting helpers
function formatAccountNumber(accountNumber) {
    // Format as: 1234-5678-90
    return accountNumber.replace(/(\d{4})(\d{4})(\d{2})/, '$1-$2-$3');
}

function getTransactionTypeIcon(type) {
    switch (type) {
        case 'DEPOSIT':
            return '<i class="fas fa-arrow-down text-success"></i>';
        case 'WITHDRAW':
            return '<i class="fas fa-arrow-up text-danger"></i>';
        case 'TRANSFER':
            return '<i class="fas fa-exchange-alt text-info"></i>';
        default:
            return '<i class="fas fa-question text-muted"></i>';
    }
}

function getTransactionTypeName(type) {
    switch (type) {
        case 'DEPOSIT':
            return 'Depósito';
        case 'WITHDRAW':
            return 'Retiro';
        case 'TRANSFER':
            return 'Transferencia';
        default:
            return 'Desconocido';
    }
}

function getAccountStatusBadge(status) {
    switch (status) {
        case 'ACTIVE':
            return '<span class="badge bg-success">Activa</span>';
        case 'BLOCKED':
            return '<span class="badge bg-warning">Bloqueada</span>';
        case 'CLOSED':
            return '<span class="badge bg-danger">Cerrada</span>';
        default:
            return '<span class="badge bg-secondary">Desconocido</span>';
    }
}

// Export functions for use in templates
window.BankingMVP = {
    showToast,
    apiRequest,
    formatCurrency,
    formatDate,
    validateEmail,
    validatePhone,
    validateAmount,
    validateForm,
    clearFormValidation,
    showLoading,
    hideLoading,
    setActiveNavItem,
    getUrlParameter,
    setUrlParameter,
    formatAccountNumber,
    getTransactionTypeIcon,
    getTransactionTypeName,
    getAccountStatusBadge
};