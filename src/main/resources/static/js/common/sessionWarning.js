/**
 * Session 警告管理系統
 * 功能：30秒無動作後彈出警告，60秒後自動登出
 */
class SessionWarningManager {
    constructor() {
        this.warningTime = 30000; // 30秒警告
        this.logoutTime = 60000;  // 60秒登出
        this.warningTimer = null;
        this.logoutTimer = null;
        this.isWarningShown = false;
        this.lastActivityTime = Date.now();
        
        this.init();
    }

    init() {
        this.createWarningModal();
        this.bindEvents();
        this.startTimers();
        console.log('🔒 Session警告系統已啟動 - 30秒警告，60秒自動登出');
    }

    /**
     * 創建警告彈窗
     */
    createWarningModal() {
        // 檢查是否已存在警告彈窗
        if (document.getElementById('session-warning-modal')) {
            return;
        }

        const modalHtml = `
            <div id="session-warning-modal" class="fixed inset-0 z-[9999] hidden">
                <!-- 背景遮罩 -->
                <div class="fixed inset-0 bg-black bg-opacity-70"></div>
                
                <!-- Modal 內容 -->
                <div class="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-white rounded-lg shadow-2xl w-96 max-w-[90vw]">
                    <div class="p-6 text-center">
                        <!-- 警告圖示 -->
                        <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-yellow-100 mb-4">
                            <i class="fas fa-exclamation-triangle text-yellow-600 text-2xl"></i>
                        </div>
                        
                        <!-- 標題 -->
                        <h3 class="text-xl font-bold text-gray-900 mb-2">⚠️ Session 即將過期</h3>
                        
                        <!-- 警告訊息 -->
                        <p class="text-gray-600 mb-4">
                            您已經 <span class="font-semibold text-orange-600">30 秒</span> 沒有操作了<br>
                            系統將在 <span id="countdown-timer" class="font-bold text-red-600">30</span> 秒後自動登出
                        </p>
                        
                        <!-- 進度條 -->
                        <div class="w-full bg-gray-200 rounded-full h-2 mb-6">
                            <div id="countdown-progress" class="bg-gradient-to-r from-yellow-400 to-red-500 h-2 rounded-full transition-all duration-1000" style="width: 100%"></div>
                        </div>
                        
                        <!-- 按鈕區域 -->
                        <div class="flex space-x-3">
                            <button id="session-continue-btn" class="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors">
                                <i class="fas fa-hand-sparkles mr-2"></i>繼續工作
                            </button>
                            <button id="session-logout-btn" class="flex-1 px-4 py-2 bg-gray-500 text-white rounded-lg font-medium hover:bg-gray-600 transition-colors">
                                <i class="fas fa-sign-out-alt mr-2"></i>立即登出
                            </button>
                        </div>
                        
                        <p class="text-xs text-gray-500 mt-3">
                            <i class="fas fa-info-circle mr-1"></i>
                            請點擊「繼續工作」按鈕以繼續操作
                        </p>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        this.bindModalEvents();
    }

    /**
     * 綁定彈窗事件
     */
    bindModalEvents() {
        const modal = document.getElementById('session-warning-modal');
        const continueBtn = document.getElementById('session-continue-btn');
        const logoutBtn = document.getElementById('session-logout-btn');

        // 繼續工作按鈕
        continueBtn?.addEventListener('click', () => {
            this.resetActivity();
            this.hideWarning();
        });

        // 立即登出按鈕
        logoutBtn?.addEventListener('click', () => {
            this.performLogout();
        });

        // 完全阻止點擊背景關閉（強制用戶選擇按鈕）
        modal?.addEventListener('click', (e) => {
            e.stopPropagation();
            e.preventDefault();
        });
    }

    /**
     * 綁定用戶活動事件
     */
    bindEvents() {
        // 【修改】移除 mousemove 事件，防止滑鼠移動干擾警告
        const events = [
            'mousedown', 'keypress', 'scroll', 
            'touchstart', 'click', 'focus', 'blur'
        ];

        events.forEach(event => {
            document.addEventListener(event, this.handleUserActivity.bind(this), true);
        });

        // 特別處理表單提交和AJAX請求
        this.interceptAjaxRequests();
        this.interceptFormSubmissions();
    }

    /**
     * 處理用戶活動
     */
    handleUserActivity(event) {
        // 【新增】如果警告已顯示，忽略所有活動事件，強制用戶點擊按鈕
        if (this.isWarningShown) {
            console.log('🚫 警告已顯示，忽略用戶活動事件:', event.type);
            return;
        }

        // 排除某些不重要的事件
        if (this.shouldIgnoreEvent(event)) {
            return;
        }

        const now = Date.now();
        const timeSinceLastActivity = now - this.lastActivityTime;

        // 避免過於頻繁的重置（500ms內的活動視為同一次）
        if (timeSinceLastActivity < 500) {
            return;
        }

        this.resetActivity();
        
        console.log('🔄 檢測到有效用戶活動:', event.type);
    }

    /**
     * 判斷是否應該忽略某些事件
     */
    shouldIgnoreEvent(event) {
        // 【新增】在警告顯示期間，除了點擊繼續按鈕外，忽略所有事件
        if (this.isWarningShown) {
            const target = event.target;
            const continueBtn = document.getElementById('session-continue-btn');
            const logoutBtn = document.getElementById('session-logout-btn');
            
            // 只允許點擊警告彈窗內的按鈕
            if (target === continueBtn || target === logoutBtn) {
                return false; // 不忽略這些按鈕的點擊
            }
            return true; // 忽略其他所有事件
        }

        // 忽略警告彈窗內的其他事件（除了按鈕）
        const target = event.target;
        const modal = document.getElementById('session-warning-modal');
        if (modal && modal.contains(target)) {
            const continueBtn = document.getElementById('session-continue-btn');
            const logoutBtn = document.getElementById('session-logout-btn');
            if (target !== continueBtn && target !== logoutBtn) {
                return true; // 忽略彈窗內除按鈕外的其他事件
            }
        }

        return false;
    }

    /**
     * 攔截 AJAX 請求
     */
    interceptAjaxRequests() {
        const originalFetch = window.fetch;
        window.fetch = (...args) => {
            this.resetActivity();
            return originalFetch.apply(window, args);
        };

        // 攔截 XMLHttpRequest
        const originalOpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function() {
            this.addEventListener('loadstart', () => {
                if (window.sessionWarningManager) {
                    window.sessionWarningManager.resetActivity();
                }
            });
            return originalOpen.apply(this, arguments);
        };
    }

    /**
     * 攔截表單提交
     */
    interceptFormSubmissions() {
        document.addEventListener('submit', () => {
            this.resetActivity();
        }, true);
    }

    /**
     * 重置活動時間並重啟計時器
     */
    resetActivity() {
        // 【新增】如果警告已顯示，不允許重置活動時間
        if (this.isWarningShown) {
            console.log('🚫 警告顯示中，拒絕重置活動時間');
            return;
        }

        this.lastActivityTime = Date.now();
        this.clearTimers();
        this.startTimers();
        
        console.log('🔄 用戶活動檢測 - 重置Session計時器');
    }

    /**
     * 開始計時器
     */
    startTimers() {
        // 30秒後顯示警告
        this.warningTimer = setTimeout(() => {
            this.showWarning();
        }, this.warningTime);

        // 60秒後自動登出
        this.logoutTimer = setTimeout(() => {
            this.performLogout();
        }, this.logoutTime);
    }

    /**
     * 清除所有計時器
     */
    clearTimers() {
        if (this.warningTimer) {
            clearTimeout(this.warningTimer);
            this.warningTimer = null;
        }
        if (this.logoutTimer) {
            clearTimeout(this.logoutTimer);
            this.logoutTimer = null;
        }
        if (this.countdownInterval) {
            clearInterval(this.countdownInterval);
            this.countdownInterval = null;
        }
    }

    /**
     * 顯示警告彈窗
     */
    showWarning() {
        if (this.isWarningShown) return;

        const modal = document.getElementById('session-warning-modal');
        if (!modal) return;

        this.isWarningShown = true;
        modal.classList.remove('hidden');
        modal.classList.add('flex');

        // 【新增】禁用頁面滾動和其他交互
        document.body.style.overflow = 'hidden';
        
        // 開始倒數計時
        this.startCountdown();

        console.log('⚠️  Session警告 - 顯示30秒警告彈窗，用戶必須點擊按鈕');
    }

    /**
     * 隱藏警告彈窗
     */
    hideWarning() {
        const modal = document.getElementById('session-warning-modal');
        if (!modal) return;

        this.isWarningShown = false;
        modal.classList.add('hidden');
        modal.classList.remove('flex');

        // 【新增】恢復頁面滾動和交互
        document.body.style.overflow = '';

        // 停止倒數計時
        if (this.countdownInterval) {
            clearInterval(this.countdownInterval);
            this.countdownInterval = null;
        }

        console.log('✅ Session警告 - 隱藏警告彈窗，恢復正常操作');
    }

    /**
     * 開始倒數計時
     */
    startCountdown() {
        let remainingSeconds = 30;
        const timerElement = document.getElementById('countdown-timer');
        const progressElement = document.getElementById('countdown-progress');

        this.countdownInterval = setInterval(() => {
            remainingSeconds--;
            
            if (timerElement) {
                timerElement.textContent = remainingSeconds;
            }
            
            if (progressElement) {
                const percentage = (remainingSeconds / 30) * 100;
                progressElement.style.width = percentage + '%';
            }

            if (remainingSeconds <= 0) {
                clearInterval(this.countdownInterval);
                this.performLogout();
            }
        }, 1000);
    }

    /**
     * 執行登出操作
     */
    performLogout() {
        console.log('🚪 Session超時 - 執行自動登出');
        
        this.clearTimers();
        
        // 顯示登出訊息
        if (this.isWarningShown) {
            this.hideWarning();
        }

        // 執行登出
        try {
            // 嘗試發送登出請求到後端
            fetch('/employee/logout', {
                method: 'GET',
                credentials: 'same-origin'
            }).finally(() => {
                // 無論請求成功與否，都重定向到登入頁面
                window.location.href = '/employee/login?timeout=true';
            });
        } catch (error) {
            // 如果請求失敗，直接重定向
            console.error('登出請求失敗:', error);
            window.location.href = '/employee/login?timeout=true';
        }
    }

    /**
     * 銷毀警告管理器
     */
    destroy() {
        this.clearTimers();
        
        // 【新增】恢復頁面狀態
        document.body.style.overflow = '';
        
        const modal = document.getElementById('session-warning-modal');
        if (modal) {
            modal.remove();
        }
        console.log('🔒 Session警告系統已銷毀');
    }
}

// 自動初始化（僅在員工相關頁面）
document.addEventListener('DOMContentLoaded', function() {
    // 檢查是否在員工相關頁面
    const currentPath = window.location.pathname;
    const isEmployeePage = currentPath.includes('/employee') || 
                          currentPath.includes('/back-end/employee') ||
                          currentPath.includes('/member') ||
                          currentPath.includes('/orderlist');

    // 檢查是否已登入（存在員工Session資訊）
    const hasEmployeeSession = document.querySelector('body').dataset.hasEmployeeSession !== 'false';

    if (isEmployeePage && hasEmployeeSession) {
        // 延遲初始化，確保頁面完全載入
        setTimeout(() => {
            if (!window.sessionWarningManager) {
                window.sessionWarningManager = new SessionWarningManager();
            }
        }, 1000);
    }
});

// 頁面卸載時清理
window.addEventListener('beforeunload', function() {
    if (window.sessionWarningManager) {
        window.sessionWarningManager.destroy();
    }
});

// 導出供手動使用
window.SessionWarningManager = SessionWarningManager;