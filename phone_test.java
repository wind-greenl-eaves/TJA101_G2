public class PhoneTest {
    public static void main(String[] args) {
        // 當前的正則表達式
        String currentPattern = "^(\\(0\\d{1,2}\\)|0\\d{1,2})[\\s-]?\\d{3,4}[\\s-]?\\d{3,4}$";
        
        // 測試案例
        String[] testCases = {
            "0912345678",      // 應該通過
            "0912-345-678",    // 應該通過 - 問題格式
            "09-12345678",     // 應該通過
            "02-12345678",     // 應該通過
            "(02)12345678",    // 應該通過
            "02 12345678"      // 應該通過
        };
        
        System.out.println("測試當前正則表達式: " + currentPattern);
        System.out.println("=".repeat(60));
        
        for (String testCase : testCases) {
            boolean matches = testCase.matches(currentPattern);
            System.out.println(String.format("%-15s -> %s", testCase, matches ? "✓ 通過" : "✗ 失敗"));
        }
        
        System.out.println("\n" + "=".repeat(60));
        
        // 改進的正則表達式
        String improvedPattern = "^0\\d{1,2}[\\s-]?\\d{3,4}[\\s-]?\\d{3,4}$|^\\(0\\d{1,2}\\)\\d{3,4}[\\s-]?\\d{3,4}$";
        
        System.out.println("測試改進後正則表達式: " + improvedPattern);
        System.out.println("=".repeat(60));
        
        for (String testCase : testCases) {
            boolean matches = testCase.matches(improvedPattern);
            System.out.println(String.format("%-15s -> %s", testCase, matches ? "✓ 通過" : "✗ 失敗"));
        }
    }
}