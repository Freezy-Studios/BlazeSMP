// VULNERABLE LOGIN SCRIPT - FOR EDUCATIONAL PURPOSES ONLY
// This script contains intentional security vulnerabilities

// Vulnerability 1: Plain text password storage
const STORED_CREDENTIALS = {
    username: 'admin',
    password: 'password123' // NEVER store passwords in plain text!
};

// Vulnerability 2: Expose credentials in global scope
window.adminCredentials = STORED_CREDENTIALS;

// Vulnerability 3: No rate limiting - allows brute force attacks
let loginAttempts = 0;

document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const messageDiv = document.getElementById('loginMessage');
    
    loginAttempts++;
    
    // Vulnerability 4: Expose attempt count in console (information disclosure)
    console.log(`Login attempt #${loginAttempts} for user: ${username}`);
    console.log('Stored credentials:', STORED_CREDENTIALS);
    
    // Vulnerability 5: Client-side authentication (easily bypassed)
    if (username === STORED_CREDENTIALS.username && password === STORED_CREDENTIALS.password) {
        // Vulnerability 6: Store credentials in localStorage (insecure)
        localStorage.setItem('userCredentials', JSON.stringify({
            username: username,
            password: password, // NEVER store passwords!
            loginTime: new Date().toISOString(),
            sessionId: Math.random().toString(36).substr(2, 9)
        }));
        
        // Vulnerability 7: Store session in URL parameters
        window.location.href = `dashboard.html?user=${username}&pass=${password}&session=${Math.random().toString(36)}`;
        
    } else {
        // Vulnerability 8: Information disclosure - revealing valid usernames
        if (username === STORED_CREDENTIALS.username) {
            messageDiv.innerHTML = `<div style="color: red; background: #ffebee; padding: 10px; border-radius: 4px;">
                Incorrect password for user "${username}". Try again!
            </div>`;
        } else {
            messageDiv.innerHTML = `<div style="color: red; background: #ffebee; padding: 10px; border-radius: 4px;">
                User "${username}" not found. Valid username is "admin".
            </div>`;
        }
        
        // Vulnerability 9: XSS vulnerability - using innerHTML with user input
        setTimeout(() => {
            messageDiv.innerHTML = `<div style="color: red;">
                Failed login attempt for: ${username}<br>
                <script>console.log('XSS executed!')</script>
                Attempts: ${loginAttempts}
            </div>`;
        }, 1000);
    }
});

// Vulnerability 10: Expose sensitive functions globally
window.bypassLogin = function() {
    console.log('🔓 VULNERABILITY: Login bypass function exposed!');
    localStorage.setItem('userCredentials', JSON.stringify({
        username: 'admin',
        password: 'password123',
        loginTime: new Date().toISOString(),
        sessionId: 'bypassed-session'
    }));
    window.location.href = 'dashboard.html?bypassed=true';
};

// Vulnerability 11: Debug mode exposure
window.DEBUG_MODE = true;
if (window.DEBUG_MODE) {
    console.log('🔓 DEBUG MODE ENABLED - Security features disabled!');
    console.log('🔓 Use window.bypassLogin() to bypass authentication!');
    console.log('🔓 Stored credentials:', STORED_CREDENTIALS);
}

// Vulnerability 12: Eval injection vulnerability
window.executeUserCode = function(code) {
    try {
        // NEVER use eval() with user input!
        eval(code);
    } catch (e) {
        console.error('Code execution error:', e);
    }
};

// Vulnerability 13: Expose localStorage contents
setInterval(() => {
    if (window.DEBUG_MODE) {
        console.log('Current localStorage:', localStorage);
    }
}, 5000);

// Vulnerability 14: CSRF token simulation (but it's predictable)
window.csrfToken = 'csrf_' + Date.now(); // Predictable token
console.log('CSRF Token:', window.csrfToken);

// Add some visual feedback for demo purposes
document.addEventListener('DOMContentLoaded', function() {
    const usernameField = document.getElementById('username');
    const passwordField = document.getElementById('password');
    
    // Show credential hints on focus (bad practice)
    usernameField.addEventListener('focus', function() {
        console.log('Hint: Try "admin"');
    });
    
    passwordField.addEventListener('focus', function() {
        console.log('Hint: Try "password123"');
    });
});