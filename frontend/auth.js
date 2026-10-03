const tabLogin = document.getElementById('tab-login');
const tabRegister = document.getElementById('tab-register');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const alertBox = document.getElementById('alert-box');

// Already signed in? Skip straight to the dashboard.
if (Session.get()) {
  window.location.href = 'dashboard.html';
}

function showTab(which) {
  alertBox.innerHTML = '';
  const isLogin = which === 'login';
  tabLogin.classList.toggle('active', isLogin);
  tabRegister.classList.toggle('active', !isLogin);
  loginForm.hidden = !isLogin;
  registerForm.hidden = isLogin;
}
tabLogin.addEventListener('click', () => showTab('login'));
tabRegister.addEventListener('click', () => showTab('register'));

function showError(message) {
  alertBox.innerHTML = `<div class="alert alert-error">${message}</div>`;
}

loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  alertBox.innerHTML = '';
  const submitBtn = loginForm.querySelector('button');
  submitBtn.disabled = true;
  try {
    const user = await apiRequest('/api/login', {
      method: 'POST',
      body: JSON.stringify({
        email: document.getElementById('login-email').value.trim(),
        password: document.getElementById('login-password').value,
      }),
    });
    Session.save(user);
    window.location.href = 'dashboard.html';
  } catch (err) {
    showError(err.message);
    submitBtn.disabled = false;
  }
});

registerForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  alertBox.innerHTML = '';
  const submitBtn = registerForm.querySelector('button');
  submitBtn.disabled = true;
  try {
    await apiRequest('/api/register', {
      method: 'POST',
      body: JSON.stringify({
        firstName: document.getElementById('reg-first').value.trim(),
        lastName: document.getElementById('reg-last').value.trim(),
        email: document.getElementById('reg-email').value.trim(),
        phone: document.getElementById('reg-phone').value.trim(),
        address: document.getElementById('reg-address').value.trim(),
        password: document.getElementById('reg-password').value,
      }),
    });
    // Registered — log straight in rather than making them retype credentials.
    const user = await apiRequest('/api/login', {
      method: 'POST',
      body: JSON.stringify({
        email: document.getElementById('reg-email').value.trim(),
        password: document.getElementById('reg-password').value,
      }),
    });
    Session.save(user);
    window.location.href = 'dashboard.html';
  } catch (err) {
    showError(err.message);
    submitBtn.disabled = false;
  }
});
