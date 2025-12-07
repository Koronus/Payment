// Основные переменные
let countdownInterval;

// Инициализация при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    initializePaymentForm();
});

function initializePaymentForm() {
    const form = document.getElementById('paymentForm');
    const submitBtn = document.getElementById('submitBtn');
    const resultDiv = document.getElementById('resultMessage');
    const successPopup = document.getElementById('successPopup');
    const returnToSiteBtn = document.getElementById('returnToSiteBtn');

    // Автозаполнение полей из URL параметров
    autoFillFormFromURL();

    // Настройка форматирования полей
    setupFieldFormatting();

    // Обработчики событий
    form.addEventListener('submit', handleFormSubmit);
    returnToSiteBtn.addEventListener('click', handleReturnToSite);

    console.log('Форма оплаты инициализирована');
}

// Автозаполнение формы из URL параметров
function autoFillFormFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    console.log('🔍 Параметры URL:', Object.fromEntries(urlParams.entries()));

    // Заполняем поля
    document.getElementById('amount').value = urlParams.get('amount') || '';
    document.getElementById('purpose').value = urlParams.get('product') || '';
    document.getElementById('email').value = urlParams.get('email') || '';
    document.getElementById('surname').value = urlParams.get('surname') || '';
    document.getElementById('nameUser').value = urlParams.get('nameUser') || '';

    const returnUrl = urlParams.get('return_url');
    if (returnUrl) {
        document.getElementById('return_url').value = returnUrl;
        console.log('✅ Return URL установлен:', returnUrl);
    }

    console.log('📊 Заполненные поля:');
    console.log('amount:', document.getElementById('amount').value);
    console.log('purpose:', document.getElementById('purpose').value);
    console.log('email:', document.getElementById('email').value);
}

// Настройка форматирования полей ввода
function setupFieldFormatting() {
    // Форматирование номера карты
    document.getElementById('cardNumber').addEventListener('input', function(e) {
        let value = e.target.value.replace(/\s/g, '').replace(/\D/g, '');
        value = value.substring(0, 16);
        value = value.replace(/(\d{4})/g, '$1 ').trim();
        e.target.value = value;
    });

    // Форматирование срока действия
    document.getElementById('dateOfAction').addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length >= 2) {
            value = value.substring(0, 2) + '/' + value.substring(2, 4);
        }
        e.target.value = value.substring(0, 5);
        validateExpiryDate(e.target);
    });

    // Форматирование CVV
    document.getElementById('cvvCode').addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');
        e.target.value = value.substring(0, 3);
        validateCVVWithFeedback(e.target);
    });
}

// Валидация срока действия карты
function validateExpiryDate(input) {
    const value = input.value;
    input.style.borderColor = '';
    input.style.backgroundColor = '';

    if (!/^(0[1-9]|1[0-2])\/([0-9]{2})$/.test(value)) {
        if (value.length === 5) {
            input.style.borderColor = 'red';
            input.title = 'Формат должен быть ММ/ГГ (например: 12/25)';
        }
        return false;
    }

    const [monthStr, yearStr] = value.split('/');
    const month = parseInt(monthStr, 10);
    const year = 2000 + parseInt(yearStr, 10);
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth() + 1;

    if (month < 1 || month > 12) {
        input.style.borderColor = 'red';
        input.title = 'Месяц должен быть от 01 до 12';
        return false;
    }

    if (year < currentYear || (year === currentYear && month < currentMonth)) {
        input.style.borderColor = 'red';
        input.title = `Карта просрочена. Текущая дата: ${currentMonth.toString().padStart(2, '0')}/${currentYear.toString().slice(2)}`;
        return false;
    }

    const maxFutureYear = currentYear + 10;
    if (year > maxFutureYear) {
        input.style.borderColor = 'orange';
        input.title = `Срок действия слишком далекий. Максимум: ${maxFutureYear.toString().slice(2)}`;
        return false;
    }

    return true;
}

// Валидация CVV
function validateCVVWithFeedback(input) {
    const value = input.value;
    input.style.borderColor = '';
    input.style.backgroundColor = '';

    if (value.length === 0) {
        input.title = 'Введите CVV код (3 цифры на обратной стороне карты)';
        return;
    }

    if (value.length !== 3) {
        input.style.borderColor = 'red';
        input.style.backgroundColor = '#ffe6e6';
        input.title = `CVV должен содержать 3 цифры (сейчас: ${value.length})`;
    }
}

// Обработка отправки формы
async function handleFormSubmit(e) {
    e.preventDefault();

    try {
        const submitBtn = document.getElementById('submitBtn');
        const resultDiv = document.getElementById('resultMessage');

        submitBtn.disabled = true;
        submitBtn.textContent = 'Обработка...';
        resultDiv.style.display = 'none';

        const formData = new FormData(this);
        console.log('Отправляемые данные:');
        for (let [key, value] of formData.entries()) {
            console.log(key + ': ' + value);
        }

        // 🔥 ВРЕМЕННО: Тестовые сценарии
        const testRandom = Math.random();
        console.log('🔍 ТЕСТ - Случайное число:', testRandom);

        if (testRandom < 0.5) {
            console.log('🧪 ТЕСТ: Показываем УСПЕХ');
            showSuccessPopup();
        } else if (testRandom < 0.7) {
            console.log('🧪 ТЕСТ: Показываем ОШИБКУ 1');
            showErrorMessage('❌ ТЕСТ: Недостаточно средств на карте');
        } else {
            console.log('🧪 ТЕСТ: Показываем ОШИБКУ 2');
            showErrorMessage('❌ ТЕСТ: Карта заблокирована');
        }

    } catch (error) {
        console.error('Ошибка:', error);
        showErrorMessage('Ошибка соединения: ' + error.message);
    } finally {
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Оплатить';
    }
}

// Показ попапа успеха
function showSuccessPopup() {
    const successPopup = document.getElementById('successPopup');
    if (successPopup) {
        successPopup.style.display = 'flex';

        const returnBtn = document.getElementById('returnToSiteBtn');
        returnBtn.textContent = 'Вернуться в магазин';

        const countdownElement = document.createElement('div');
        countdownElement.style.marginTop = '15px';
        countdownElement.style.fontSize = '14px';
        countdownElement.style.color = '#666';

        const popupContent = successPopup.querySelector('.success-popup-content');
        popupContent.appendChild(countdownElement);

        let countdown = 5;
        countdownElement.textContent = `Автоматический возврат в магазин через ${countdown} секунд...`;

        countdownInterval = setInterval(() => {
            countdown--;
            countdownElement.textContent = `Автоматический возврат в магазин через ${countdown} секунд...`;

            if (countdown <= 0) {
                clearInterval(countdownInterval);
                returnToMerchantSite();
            }
        }, 1000);
    }
}

// Возврат на сайт мерчанта
function returnToMerchantSite() {
    console.log('Возврат на внешний сайт...');

    const urlParams = new URLSearchParams(window.location.search);
    let returnUrl = urlParams.get('return_url');

    if (!returnUrl) {
        returnUrl = "http://localhost:3000/demoshop.html";
    }

    console.log('Return URL:', returnUrl);

    const successUrl = new URL(returnUrl);
    const amount = document.getElementById('amount').value;
    const purpose = document.getElementById('purpose').value;

    successUrl.searchParams.append('payment_status', 'success');
    successUrl.searchParams.append('amount', amount);
    successUrl.searchParams.append('product', purpose);
    successUrl.searchParams.append('timestamp', Date.now());
    successUrl.searchParams.append('operation_id', 'OP-' + Date.now());

    const orderId = urlParams.get('order_id');
    if (orderId) {
        successUrl.searchParams.append('order_id', orderId);
    }

    console.log('Полный URL для возврата:', successUrl.toString());
    window.location.href = successUrl.toString();
}

// Обработчик возврата на сайт
function handleReturnToSite() {
    if (countdownInterval) {
        clearInterval(countdownInterval);
    }
    returnToMerchantSite();
}

// Показать сообщение об ошибке
function showErrorMessage(message) {
    const resultDiv = document.getElementById('resultMessage');
    if (resultDiv) {
        resultDiv.textContent = message;
        resultDiv.style.display = 'block';
        resultDiv.style.color = '#dc3545';
        resultDiv.style.backgroundColor = '#f8d7da';
        resultDiv.style.padding = '10px';
        resultDiv.style.borderRadius = '4px';
        resultDiv.style.marginTop = '15px';
    }
}

// Показать ошибку поля
function showFieldError(fieldName, errorMessage) {
    const field = document.querySelector(`[name="${fieldName}"]`);
    if (!field) return;

    field.style.borderColor = '#dc3545';
    field.style.backgroundColor = '#f8d7da';

    let errorElement = field.parentNode.querySelector('.field-error');
    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.className = 'field-error';
        errorElement.style.color = '#dc3545';
        errorElement.style.fontSize = '12px';
        errorElement.style.marginTop = '5px';
        field.parentNode.appendChild(errorElement);
    }

    errorElement.textContent = errorMessage;
}

// Очистить все ошибки
function clearAllErrors() {
    const fieldErrors = document.querySelectorAll('.field-error');
    fieldErrors.forEach(error => error.remove());

    const fields = document.querySelectorAll('input');
    fields.forEach(field => {
        field.style.borderColor = '';
        field.style.backgroundColor = '';
    });

    const resultDiv = document.getElementById('resultMessage');
    if (resultDiv) {
        resultDiv.style.display = 'none';
    }
}