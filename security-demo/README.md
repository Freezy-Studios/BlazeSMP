# Security Demonstration Website

⚠️ **WARNING: This website contains intentional security vulnerabilities for educational purposes only. DO NOT use any of these patterns in production applications!**

## Overview

This demonstration website shows common security vulnerabilities that can compromise login credentials and user data. It's designed to educate developers about what NOT to do when building web applications.

## How to Use

1. Open `index.html` in a web browser
2. Use the demo credentials: `admin` / `password123`
3. Explore the dashboard to see various vulnerabilities in action
4. Open browser developer tools (F12) to see console logs exposing sensitive data

## Security Vulnerabilities Demonstrated

### 1. Plain Text Password Storage
- **Location**: `login.js` and localStorage
- **Issue**: Passwords stored in plain text in localStorage and JavaScript variables
- **Real-world impact**: Anyone with access to the device can see passwords

### 2. Client-Side Authentication
- **Location**: `login.js` 
- **Issue**: Authentication logic runs entirely in the browser
- **Real-world impact**: Can be easily bypassed by modifying JavaScript

### 3. XSS (Cross-Site Scripting) Vulnerabilities
- **Location**: `dashboard.js` - command execution function
- **Issue**: User input directly inserted into HTML using innerHTML
- **Demo**: Try entering `<script>alert('XSS')</script>` in the command field
- **Real-world impact**: Attackers can execute malicious scripts

### 4. SQL Injection Simulation
- **Location**: `dashboard.js` - user search function
- **Issue**: User input directly concatenated into SQL query
- **Demo**: Try searching for `' OR 1=1 --`
- **Real-world impact**: Database compromise, data theft

### 5. Information Disclosure
- **Location**: Throughout the application
- **Issues**:
  - Credentials exposed in console logs
  - Session data visible in browser
  - Error messages reveal system information
  - Debug functions exposed globally

### 6. No Brute Force Protection
- **Location**: `login.js`
- **Issue**: No rate limiting or account lockout
- **Real-world impact**: Attackers can try unlimited password combinations

### 7. Insecure Data Transmission
- **Location**: URL parameters in dashboard redirect
- **Issue**: Credentials passed in URL parameters
- **Real-world impact**: Passwords visible in browser history, server logs

### 8. CSRF (Cross-Site Request Forgery) Vulnerability
- **Location**: `dashboard.js` - admin functions
- **Issue**: No CSRF tokens or verification
- **Real-world impact**: Malicious sites can perform actions on behalf of users

### 9. Eval() Injection
- **Location**: `dashboard.js` - command execution
- **Issue**: Using eval() with user input
- **Demo**: Try command `eval:alert('Code injection')`
- **Real-world impact**: Arbitrary code execution

### 10. Exposed Debug Functions
- **Location**: `dashboard.js` - global window functions
- **Issue**: Administrative functions accessible via browser console
- **Demo**: Try `window.adminFunctions.deleteAllUsers()` in console

## Educational Points

### What Developers Should Do Instead:

1. **Password Security**:
   - Hash passwords with strong algorithms (bcrypt, Argon2)
   - Never store plain text passwords
   - Use secure session management

2. **Authentication**:
   - Implement server-side authentication
   - Use secure session tokens
   - Implement proper logout procedures

3. **Input Validation**:
   - Sanitize all user inputs
   - Use parameterized queries
   - Implement proper output encoding

4. **Access Controls**:
   - Implement rate limiting
   - Use CSRF tokens
   - Require proper authorization for admin functions

5. **Data Protection**:
   - Use HTTPS for all communications
   - Implement proper error handling
   - Don't expose sensitive data in logs or console

## Testing the Vulnerabilities

### XSS Testing:
1. Go to the dashboard
2. In the "Execute Command" field, enter: `<img src=x onerror=alert('XSS')>`
3. Click Execute to see the XSS in action

### SQL Injection Testing:
1. In the "User Search" field, enter: `admin'; DROP TABLE users; --`
2. See how the vulnerable query construction is exposed

### Authentication Bypass:
1. Open browser console (F12)
2. Type: `window.bypassLogin()` and press Enter
3. See how client-side authentication can be bypassed

### Session Data Exposure:
1. Open browser console
2. Type: `localStorage` to see stored credentials
3. Type: `window.currentSessionData` to see exposed session information

## Files Structure

- `index.html` - Login page with vulnerable authentication
- `dashboard.html` - Admin dashboard with multiple vulnerabilities
- `styles.css` - Styling for the demonstration
- `login.js` - Vulnerable login logic
- `dashboard.js` - Vulnerable dashboard functionality
- `README.md` - This documentation

## Disclaimer

This code is provided for educational purposes only. The vulnerabilities demonstrated here are intentional and should never be implemented in production applications. Always follow security best practices when developing real applications.

## Learning Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Web Security Academy](https://portswigger.net/web-security)
- [Mozilla Developer Network Security](https://developer.mozilla.org/en-US/docs/Web/Security)