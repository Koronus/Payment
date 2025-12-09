const otpForm = document.getElementById('otp-form');
const otpInput = document.getElementById('otp-input');
const statusMsg = document.getElementById('statusMsg');

otpForm.addEventListener('submit', function (event) {
  event.preventDefault();

  const code = otpInput.value.trim();

  if (!/^\d{4}$/.test(code)) {
    statusMsg.textContent = 'Пожалуйста, введите 4-значный код.';
    statusMsg.className = 'status-message error';
    return;
  }

  fetch('/payment-form/verify-otp', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ otp: code })
  })
    .then(response => response.json())
    .then(data => {
      if (!data.otpValid) {
        statusMsg.textContent = 'Введён неверный код. Попробуйте ещё раз.';
        statusMsg.className = 'status-message error';
        otpInput.value = '';
        return;
      }

      // Код верный – только сообщение про код
      statusMsg.textContent =
        'Код успешно подтверждён. Сейчас вы увидите результат обработки платежа.';
      statusMsg.className = 'status-message success';

      setTimeout(() => {
        window.location.href = '/payment-form/result';
      }, 2000);
    })
    .catch(error => {
      console.error('Ошибка при проверке кода:', error);
      statusMsg.textContent = 'Произошла ошибка при проверке кода. Повторите попытку позже.';
      statusMsg.className = 'status-message error';
    });
});

otpInput.addEventListener('input', () => {
  otpInput.value = otpInput.value.replace(/\D/g, '').slice(0, 4);
});
