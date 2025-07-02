/* =======================================================================================
 * 檔案: add.js (已增加即時驗證)
 * 路徑: resources/static/js/employee/add.js
 * 說明: 【本次修改】:
 * 1. 新增 handleRealtimeValidation 函式，用於處理欄位的即時後端驗證。
 * 2. 為 account, email, nationalId 欄位綁定 'blur' (焦點離開) 事件監聽。
 * 3. 使用者在填寫完這些欄位並移開滑鼠後，會立刻向後端發送請求進行唯一性檢查。
 * ======================================================================================= */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('add-form');
    const messageContainer = document.getElementById('message-container');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('password-toggle');
    const successModal = document.getElementById('success-modal');
    const successModalMessage = document.getElementById('success-modal-message');
    const successModalConfirm = document.getElementById('success-modal-confirm');
    
    // --- 【新增】即時驗證相關 ---
    const fieldsToValidate = ['account', 'email', 'nationalId'];

    /**
     * 【新增函式】: 處理欄位的即時後端驗證
     * @param {Event} event - 觸發此函式的事件物件
     */
    async function handleRealtimeValidation(event) {
        const input = event.target;
        const field = input.name;
        const value = input.value.trim();
        const errorDiv = document.getElementById(`error-${field}`);
        const spinner = document.getElementById(`spinner-${field}`);

        // 步驟 1: 如果欄位為空，或不符合前端的基本格式，則不發送請求。
        if (!value || !input.checkValidity()) {
            if(spinner) spinner.classList.add('hidden');
            return;
        }

        if(spinner) spinner.classList.remove('hidden');
        errorDiv.textContent = ''; // 清除舊的錯誤訊息

        try {
            // 步驟 2: [API 路徑配對] - 呼叫後端驗證API (需在後端新增此端點)
            const response = await fetch('/api/v1/employees/validate-field', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ field, value })
            });

            if(spinner) spinner.classList.add('hidden');

            if (!response.ok) {
                // 如果API本身出錯，暫不提示，僅在控制台記錄，避免干擾使用者
                console.error(`Validation check for ${field} failed with status: ${response.status}`);
                return;
            }

            const result = await response.json();

            // 步驟 3: 根據後端回傳結果，更新UI
            if (!result.isAvailable) {
                errorDiv.textContent = result.message;
            }

        } catch (error) {
            if(spinner) spinner.classList.add('hidden');
            console.error('Real-time validation fetch error:', error);
        }
    }

    // --- 【新增】為需要驗證的欄位綁定事件監聽 ---
    fieldsToValidate.forEach(fieldId => {
        const inputElement = document.getElementById(fieldId);
        if (inputElement) {
            // 當使用者焦點離開輸入框時，觸發驗證
            inputElement.addEventListener('blur', handleRealtimeValidation);
        }
    });


    // 表單提交事件監聽 (維持原樣，但現在它會是最後一道防線)
    form.addEventListener('submit', async function (event) {
        event.preventDefault(); 
        clearAllErrors();
        messageContainer.classList.add('hidden');

        const formData = new FormData(form);
        const requestData = {};
        formData.forEach((value, key) => {
            requestData[key] = value;
        });

        const url = form.getAttribute('action');

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            const responseData = await response.json();

            if (response.status === 201) {
                showSuccessModal(`員工 "${responseData.username}" 已成功新增！`);
            } else {
                if (response.status === 400 && responseData.errors) {
                    handleValidationErrors(responseData.errors);
                    showMessage('資料驗證失敗，請檢查下方欄位。', 'error');
                } else {
                    showMessage(responseData.message || '發生未知錯誤，請稍後再試。', 'error');
                }
            }
        } catch (error) {
            console.error('Fetch Error:', error);
            showMessage('無法連接到伺服器，請檢查您的網路連線。', 'error');
        }
    });

    // 密碼可見性切換
    if(toggleIcon) {
        toggleIcon.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.classList.toggle('fa-eye');
            this.classList.toggle('fa-eye-slash');
        });
    }

    // 顯示成功後的 Modal 提示框
    function showSuccessModal(message) {
        successModalMessage.textContent = message;
        successModal.classList.remove('hidden');
        successModalConfirm.onclick = function() {
            window.location.href = '/employee/listAll';
        };
    }

    // 在頁面上方顯示錯誤訊息
    function showMessage(message, type) {
        messageContainer.textContent = message;
        messageContainer.className = `p-4 mx-8 mt-6 text-sm rounded-lg ${
            type === 'error' 
                ? 'bg-red-100 text-red-700 border border-red-400'
                : 'bg-green-100 text-green-700 border border-green-400'
        }`;
        messageContainer.classList.remove('hidden');
        messageContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    // 處理從後端傳來的欄位驗證錯誤
    function handleValidationErrors(errors) {
        for (const field in errors) {
            const errorDiv = document.getElementById(`error-${field}`);
            if (errorDiv) {
                errorDiv.textContent = errors[field];
            }
        }
    }

    // 清除所有欄位下方的錯誤訊息
    function clearAllErrors() {
        const errorDivs = document.querySelectorAll('.error-message');
        errorDivs.forEach(div => div.textContent = '');
    }
});
