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

    // Form Loading State Simulation
    const authForms = document.querySelectorAll('.auth-form');
    authForms.forEach(form => {
        form.addEventListener('submit', (e) => {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.classList.add('loading');
                submitBtn.disabled = true;
                
                // For demo purposes, we prevent actual submission for a moment
                // In a real app, this would be handled by the backend response
                // e.preventDefault(); 
                // setTimeout(() => { submitBtn.classList.remove('loading'); submitBtn.disabled = false; }, 2000);
            }
        });
    });

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
