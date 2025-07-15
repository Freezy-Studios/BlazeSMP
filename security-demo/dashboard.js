// VULNERABLE DASHBOARD SCRIPT - FOR EDUCATIONAL PURPOSES ONLY
// This script contains intentional security vulnerabilities

// Check if user is "authenticated" (vulnerability: client-side check only)
window.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const userCredentials = JSON.parse(localStorage.getItem('userCredentials') || '{}');
    
    // Vulnerability 1: Authentication data in URL parameters
    const urlUser = urlParams.get('user');
    const urlPass = urlParams.get('pass');
    
    if (!userCredentials.username && !urlUser) {
        alert('Access denied! Redirecting to login...');
        window.location.href = 'index.html';
        return;
    }
    
    // Vulnerability 2: Display sensitive information
    const currentUser = userCredentials.username || urlUser || 'Unknown';
    document.getElementById('currentUser').textContent = currentUser;
    
    // Vulnerability 3: Expose session information
    const sessionId = userCredentials.sessionId || urlParams.get('session') || 'no-session';
    document.getElementById('sessionId').textContent = sessionId;
    
    // Vulnerability 4: Display stored credentials in plain text
    document.getElementById('storedCreds').innerHTML = 
        `Username: ${userCredentials.username || 'N/A'}, 
         Password: ${userCredentials.password || 'N/A'}`;
    
    // Vulnerability 5: Log sensitive data to console
    console.log('User logged in:', {
        username: currentUser,
        credentials: userCredentials,
        urlParams: Object.fromEntries(urlParams)
    });
});

// Vulnerability 6: XSS in command execution
function executeCommand() {
    const command = document.getElementById('commandInput').value;
    const output = document.getElementById('commandOutput');
    
    // NEVER use innerHTML with user input!
    output.innerHTML = `<strong>Executing:</strong> ${command}<br>`;
    
    // Simulate command execution with XSS vulnerability
    if (command.toLowerCase().includes('alert')) {
        // This allows XSS attacks
        output.innerHTML += `<div style="color: green;">
            Command result: ${command}
            <script>${command}</script>
        </div>`;
    } else if (command.toLowerCase().includes('script')) {
        // Another XSS vector
        output.innerHTML += `<div>${command}</div>`;
    } else {
        output.innerHTML += `<div style="color: blue;">
            Command "${command}" executed successfully!<br>
            Output: System response for ${command}
        </div>`;
    }
    
    // Vulnerability 7: Eval injection
    if (command.startsWith('eval:')) {
        try {
            const code = command.substring(5);
            eval(code); // NEVER do this!
            output.innerHTML += `<div style="color: red;">⚠️ Eval executed: ${code}</div>`;
        } catch (e) {
            output.innerHTML += `<div style="color: red;">Eval error: ${e.message}</div>`;
        }
    }
}

// Vulnerability 8: SQL Injection simulation
function searchUsers() {
    const searchTerm = document.getElementById('searchInput').value;
    const results = document.getElementById('searchResults');
    
    // Simulate SQL injection vulnerability
    let query = `SELECT * FROM users WHERE username LIKE '%${searchTerm}%'`;
    
    results.innerHTML = `<strong>SQL Query:</strong><br><code>${query}</code><br><br>`;
    
    // Show how SQL injection might work
    if (searchTerm.includes("'") || searchTerm.includes(';') || searchTerm.includes('--')) {
        results.innerHTML += `<div style="color: red; background: #ffebee; padding: 10px;">
            ⚠️ <strong>SQL INJECTION DETECTED!</strong><br>
            Malicious query detected: <code>${searchTerm}</code><br>
            In a real application, this could expose the entire database!<br><br>
            <strong>Simulated exposed data:</strong><br>
            • admin:password123<br>
            • user1:123456<br>
            • user2:qwerty<br>
            • All credit card numbers<br>
            • All personal information
        </div>`;
    } else {
        // Normal search simulation
        const fakeResults = [
            'admin (Administrator)',
            'testuser (Test User)',
            'guest (Guest User)'
        ].filter(user => user.toLowerCase().includes(searchTerm.toLowerCase()));
        
        if (fakeResults.length > 0) {
            results.innerHTML += '<strong>Results:</strong><br>' + 
                fakeResults.map(user => `• ${user}`).join('<br>');
        } else {
            results.innerHTML += '<em>No users found.</em>';
        }
    }
}

// Vulnerability 9: Insecure logout
function logout() {
    // Vulnerability: Not properly clearing sensitive data
    localStorage.removeItem('userCredentials');
    
    // Vulnerability: Sensitive data still in memory/console
    console.log('User logged out, but session data may still be accessible');
    
    // Vulnerability: No server-side session invalidation
    alert('Logged out! (Note: In a real app, server-side session should be invalidated)');
    
    window.location.href = 'index.html';
}

// Vulnerability 10: Expose admin functions globally
window.adminFunctions = {
    deleteAllUsers: function() {
        console.log('🔓 VULNERABILITY: Admin function exposed!');
        alert('All users deleted! (Simulated)');
    },
    
    resetPasswords: function() {
        console.log('🔓 VULNERABILITY: Password reset function exposed!');
        alert('All passwords reset to "123456"! (Simulated)');
    },
    
    exportData: function() {
        console.log('🔓 VULNERABILITY: Data export function exposed!');
        const sensitiveData = {
            users: ['admin', 'user1', 'user2'],
            passwords: ['password123', '123456', 'qwerty'],
            creditCards: ['4111-1111-1111-1111', '4222-2222-2222-2222'],
            ssn: ['123-45-6789', '987-65-4321']
        };
        console.log('Exported sensitive data:', sensitiveData);
        alert('Sensitive data exported to console!');
    }
};

// Vulnerability 11: Automatic data exposure
setInterval(function() {
    // Continuously expose sensitive information
    window.currentSessionData = {
        user: document.getElementById('currentUser').textContent,
        sessionId: document.getElementById('sessionId').textContent,
        loginTime: new Date().toISOString(),
        permissions: ['read', 'write', 'admin', 'delete'],
        apiKeys: {
            database: 'db_key_12345',
            payment: 'pay_key_67890',
            admin: 'admin_key_abcde'
        }
    };
}, 1000);

// Vulnerability 12: Debug panel
if (window.location.search.includes('debug=true')) {
    document.body.insertAdjacentHTML('beforeend', `
        <div style="position: fixed; bottom: 10px; right: 10px; background: red; color: white; padding: 10px; border-radius: 5px; z-index: 1000;">
            <strong>🔓 DEBUG MODE</strong><br>
            <button onclick="window.adminFunctions.deleteAllUsers()">Delete Users</button><br>
            <button onclick="window.adminFunctions.resetPasswords()">Reset Passwords</button><br>
            <button onclick="window.adminFunctions.exportData()">Export Data</button><br>
            <button onclick="console.log(window.currentSessionData)">Show Session</button>
        </div>
    `);
}

// Vulnerability 13: CSRF vulnerability
window.performAdminAction = function(action) {
    // No CSRF protection
    console.log(`Performing admin action: ${action}`);
    alert(`Admin action "${action}" executed! (No CSRF protection)`);
};

// Auto-expose vulnerabilities in console
console.log('🔓 Available vulnerability demonstrations:');
console.log('• Try: executeCommand() with XSS payload like <script>alert("XSS")</script>');
console.log('• Try: searchUsers() with SQL injection like \' OR 1=1 --');
console.log('• Try: window.adminFunctions.deleteAllUsers()');
console.log('• Try: Add ?debug=true to URL for debug panel');
console.log('• Check: window.currentSessionData for exposed session info');