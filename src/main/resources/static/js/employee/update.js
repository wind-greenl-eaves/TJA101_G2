/* =======================================================================================
 * 檔案: update.js (已復原)
 * 說明: 已移除所有照片相關的邏輯，並將表單提交方式復原為發送 application/json。
 * ======================================================================================= */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('update-form');
    const messageContainer = document.getElementById('message-container');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('password-toggle');
    
    // 【已移除】照片相關元素與事件監聽

    // 表單提交事件監聽
    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        clearAllErrors();
        messageContainer.classList.add('hidden');

        const url = form.getAttribute('action');
        
        // 【已復原】: 重新使用手動方式收集表單資料為 JSON 物件
        const formData = new FormData(form);
        const requestData = {};
        formData.forEach((value, key) => {
            requestData[key] = value;
        });

        // 如果密碼欄位為空，從 requestData 中移除
        if (!requestData.password) {
            delete requestData.password;
        }

        try {
            // 【已復原】: Fetch 請求改回發送 JSON
            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData) 
            });

            const responseData = await response.json();

            if (response.ok) {
                showMessage(`員工 "${responseData.username}" 的資料已成功更新！`, 'success');
            } else {
                if (response.status === 400 && responseData.errors) {
                    handleValidationErrors(responseData.errors);
                    showMessage(responseData.message || '資料驗證失敗，請檢查下方欄位。', 'error');
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

    /**
     * 處理從後端傳來的驗證錯誤，並顯示在對應的欄位下方。
     * @param {Object} errors - 後端傳來的錯誤物件 { fieldName: errorMessage }。
     */
    function handleValidationErrors(errors) {
        for (const field in errors) {
            const errorDiv = document.getElementById(`error-${field}`);
            if (errorDiv) {
                errorDiv.textContent = errors[field];
            }
        }
    }

    /**
     * 清除所有欄位下方的錯誤訊息。
     */
    function clearAllErrors() {
        const errorDivs = document.querySelectorAll('.error-message');
        errorDivs.forEach(div => div.textContent = '');
    }

    /**
     * 根據訊息類型，決定顯示橫幅錯誤提示，或彈出成功 Modal。
     * @param {string} message - 要顯示的訊息。
     * @param {'success' | 'error'} type - 訊息的類型。
     */
    function showMessage(message, type) {
        if (type === 'success') {
            showSuccessModal(message);
        } else {
            messageContainer.textContent = message;
            messageContainer.className = `p-4 text-sm rounded-lg bg-red-100 border border-red-400 text-red-700`;
            messageContainer.classList.remove('hidden');
        }
    }

    /**
     * 動態創建並顯示一個成功提示 Modal。
     * @param {string} message - 要顯示的成功訊息。
     */
    function showSuccessModal(message) {
        const oldModal = document.getElementById('success-modal');
        if (oldModal) {
            oldModal.remove();
        }

        const modal = document.createElement('div');
        modal.id = 'success-modal';
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4';
        modal.innerHTML = `
            <div class="bg-white p-8 rounded-2xl shadow-2xl text-center max-w-sm mx-auto transform transition-all scale-95 opacity-0 animate-fade-in-up">
                <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-5">
                    <i class="fas fa-check text-green-500 text-3xl"></i>
                </div>
                <h3 class="text-xl font-bold mb-2 text-gray-800">更新成功</h3>
                <p class="mb-6 text-gray-600">${message}</p>
                <button id="modal-close-btn" class="px-8 py-2 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-lg transition-colors w-full">
                    確認
                </button>
            </div>
        `;
        
        document.body.appendChild(modal);

        requestAnimationFrame(() => {
            modal.querySelector('.transform').classList.remove('scale-95', 'opacity-0');
        });

        document.getElementById('modal-close-btn').addEventListener('click', () => {
            closeModalAndRedirect(modal);
        });
        
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModalAndRedirect(modal);
            }
        });
    }
    
    /**
     * 關閉 Modal 並跳轉回列表頁面。
     */
    function closeModalAndRedirect(modal) {
        modal.querySelector('.transform').classList.add('scale-95', 'opacity-0');
        modal.addEventListener('transitionend', () => {
             modal.remove();
             window.location.href = '/employee/listAll';
        }, { once: true });
    }
    
    // 添加 CSS 動畫
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fade-in-up {
            from { opacity: 0; transform: translateY(20px) scale(0.95); }
            to { opacity: 1; transform: translateY(0) scale(1); }
        }
        .animate-fade-in-up { animation: fade-in-up 0.3s ease-out forwards; }
        .transform { transition: transform 0.2s ease-in-out, opacity 0.2s ease-in-out; }
    `;
    document.head.appendChild(style);
});
