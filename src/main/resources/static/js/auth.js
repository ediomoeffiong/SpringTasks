document.addEventListener('DOMContentLoaded', () => {
    // Password Toggle Logic
    const togglePassword = (toggleId, inputId) => {
        const toggle = document.getElementById(toggleId);
        const input = document.getElementById(inputId);
        
        if (toggle && input) {
            toggle.addEventListener('click', () => {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                toggle.classList.toggle('bi-eye');
                toggle.classList.toggle('bi-eye-slash');
            });
        }
    };

    togglePassword('toggle-password', 'password');
    togglePassword('toggle-confirm-password', 'confirm-password');

    // Password Strength Indicator Logic
    const passwordInput = document.getElementById('password');
    const strengthMeter = document.querySelector('.strength-meter-fill');
    const strengthText = document.getElementById('strength-text');

    if (passwordInput && strengthMeter) {
        passwordInput.addEventListener('input', () => {
            const val = passwordInput.value;
            let strength = 0;

            if (val.length > 5) strength++;
            if (val.match(/[A-Z]/)) strength++;
            if (val.match(/[0-9]/)) strength++;
            if (val.match(/[^A-Za-z0-9]/)) strength++;

            strengthMeter.className = 'strength-meter-fill';
            
            if (val.length === 0) {
                strengthMeter.style.width = '0';
                if (strengthText) strengthText.textContent = '';
            } else if (strength <= 1) {
                strengthMeter.classList.add('strength-weak');
                if (strengthText) strengthText.textContent = 'Weak';
            } else if (strength <= 3) {
                strengthMeter.classList.add('strength-medium');
                if (strengthText) strengthText.textContent = 'Medium';
            } else {
                strengthMeter.classList.add('strength-strong');
                if (strengthText) strengthText.textContent = 'Strong';
            }
        });
    }

    // Form Submission Logic
    const authForms = document.querySelectorAll('.auth-form');
    authForms.forEach(form => {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const submitBtn = form.querySelector('button[type="submit"]');
            
            // Clear previous errors if any
            const existingAlert = document.querySelector('.alert-danger');
            if (existingAlert) {
                existingAlert.remove();
            }

            if (submitBtn) {
                submitBtn.classList.add('loading');
                submitBtn.disabled = true;
            }

            try {
                const isLogin = form.getAttribute('action').includes('login');
                const url = isLogin ? '/api/auth/login' : '/api/auth/register';
                
                const formData = new FormData(form);
                const data = Object.fromEntries(formData.entries());
                
                if (!isLogin && data.password !== data.confirmPassword) {
                    showError(form, "Passwords do not match");
                    return;
                }
                
                if (!isLogin) {
                    delete data.confirmPassword;
                }

                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    if (isLogin) {
                        const result = await response.json();
                        localStorage.setItem('token', result.token);
                        window.location.href = '/dashboard';
                    } else {
                        window.location.href = '/login?signupSuccess';
                    }
                } else {
                    const errorResponse = await response.text();
                    let errorMessage = 'Authentication failed';
                    try {
                        const parsed = JSON.parse(errorResponse);
                        errorMessage = parsed.message || errorMessage;
                    } catch (e) {
                        errorMessage = errorResponse || errorMessage;
                    }
                    showError(form, errorMessage);
                }
            } catch (error) {
                console.error('Error during authentication:', error);
                showError(form, 'An unexpected error occurred. Please try again.');
            } finally {
                if (submitBtn) {
                    submitBtn.classList.remove('loading');
                    submitBtn.disabled = false;
                }
            }
        });
    });

    function showError(form, message) {
        let alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger alert-dismissible fade show';
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        form.parentNode.insertBefore(alertDiv, form);
    }

    // Simple Form Validation Feedback
    const inputs = document.querySelectorAll('.form-control');
    inputs.forEach(input => {
        input.addEventListener('blur', () => {
            if (input.checkValidity()) {
                input.classList.remove('is-invalid');
                input.classList.add('is-valid');
            } else {
                input.classList.remove('is-valid');
                input.classList.add('is-invalid');
            }
        });
    });
});
