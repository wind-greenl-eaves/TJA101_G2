/* =======================================================================================
 * 檔案: update.js (已復原)
 * 說明: 已移除所有照片相關的邏輯，並將表單提交方式復原為發送 application/json。
 * ======================================================================================= */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('update-form');
    const messageContainer = document.getElementById('message-container');
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('password-toggle');
    const successModal = document.getElementById('success-modal');
    const successModalMessage = document.getElementById('success-modal-message');
    const successModalConfirm = document.getElementById('success-modal-confirm');
    
    // 添加即時驗證
    const inputs = form.querySelectorAll('input[required]');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            validateInput(this);
        });
        input.addEventListener('blur', function() {
            validateInput(this);
        });
    });

    function validateInput(input) {
        const errorDiv = document.getElementById(`error-${input.name}`);
        if (!input.checkValidity()) {
            errorDiv.textContent = input.title || '此欄位填寫有誤';
            input.classList.add('border-red-500');
        } else {
            errorDiv.textContent = '';
            input.classList.remove('border-red-500');
        }
    }

    // 照片上傳相關元素
    const photoInput = document.getElementById('photo');
    const photoUploadBtn = document.getElementById('photo-upload-btn');
    const photoPreview = document.getElementById('photo-preview');
    const photoPlaceholder = document.getElementById('photo-placeholder');
    const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 照片上傳按鈕點擊事件
    if (photoUploadBtn) {
        photoUploadBtn.addEventListener('click', function() {
            photoInput.click();
        });
    }

    // 照片選擇處理
    if (photoInput) {
        photoInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            const errorDiv = document.getElementById('error-photo');
            
            // 清除之前的錯誤訊息
            errorDiv.textContent = '';
            
            if (file) {
                // 檢查文件類型
                if (!['image/jpeg', 'image/png'].includes(file.type)) {
                    errorDiv.textContent = '只支援 JPG 或 PNG 格式的圖片';
                    this.value = '';
                    return;
                }
                
                // 檢查文件大小
                if (file.size > MAX_FILE_SIZE) {
                    errorDiv.textContent = '圖片大小不能超過 5MB';
                    this.value = '';
                    return;
                }

                // 預覽圖片
                const reader = new FileReader();
                reader.onload = function(e) {
                    if (photoPreview) {
                        photoPreview.src = e.target.result;
                        photoPreview.classList.remove('hidden');
                    }
                    if (photoPlaceholder) {
                        photoPlaceholder.classList.add('hidden');
                    }
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // 表單提交事件監聽
    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        clearAllErrors();
        messageContainer.classList.add('hidden');

        // 表單驗證
        const isValid = Array.from(inputs).every(input => {
            validateInput(input);
            return input.checkValidity();
        });

        if (!isValid) {
            showMessage('請修正表單中的錯誤後再試一次。', 'error');
            return;
        }

        const formData = new FormData(form);
        const url = form.getAttribute('action');

        try {
            // 如果沒有選擇新照片，則從 FormData 中移除 photo 欄位
            if (!photoInput.files.length) {
                formData.delete('photo');
            }

            const response = await fetch(url, {
                method: 'PUT',
                body: formData // 直接發送 FormData，不需要設置 Content-Type
            });

            const responseData = await response.json();

            if (response.ok) {
                showSuccessModal(`員工 "${responseData.username}" 的資料已成功更新！`);
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

    // Modal 相關函數
    function showSuccessModal(message) {
        successModalMessage.textContent = message;
        successModal.classList.remove('hidden');
        
        // 點擊確認按鈕時的處理
        successModalConfirm.onclick = function() {
            window.location.href = '/employee/listAll';
        };
        
        // 點擊背景遮罩時不關閉 Modal
        successModal.querySelector('.fixed.inset-0.bg-black').onclick = function(e) {
            e.stopPropagation();
        };
    }

    // 錯誤訊息顯示函數
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
});